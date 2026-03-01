package com.notasapp.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.WorkerParameters
import com.notasapp.data.local.UserPreferencesRepository
import com.notasapp.data.remote.NetworkResult
import com.notasapp.domain.repository.SheetsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * Worker de sincronización periódica con Google Sheets.
 *
 * Corre en background (en el thread pool de WorkManager) con restricción
 * de red activa. Sincroniza todas las materias del usuario activo.
 *
 * Para programarlo desde [NotasApp]:
 * ```kotlin
 * val request = PeriodicWorkRequestBuilder<SheetsSyncWorker>(24, TimeUnit.HOURS)
 *     .setConstraints(
 *         Constraints.Builder()
 *             .setRequiredNetworkType(NetworkType.CONNECTED)
 *             .build()
 *     )
 *     .build()
 * WorkManager.getInstance(context)
 *     .enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.KEEP, request)
 * ```
 *
 * **Hilt**: recibe dependencias vía `@AssistedInject` gracias a `@HiltWorker`.
 */
@HiltWorker
class SheetsSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val sheetsRepository: SheetsRepository,
    private val userPrefsRepository: UserPreferencesRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        /** Tag único para `enqueueUniquePeriodicWork`. */
        const val TAG = "SheetsSyncWorker"

        /** Constraints recomendados para el Worker. */
        val NETWORK_CONSTRAINTS: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }

    override suspend fun doWork(): Result {
        Timber.d("$TAG: iniciando sincronización periódica")

        // Obtener email del usuario activo
        val userEmail = userPrefsRepository.userEmail.first()
        if (userEmail.isNullOrBlank()) {
            Timber.w("$TAG: sin usuario activo, saltando sync")
            return Result.success()         // No es un error — el usuario no ha iniciado sesión
        }

        return when (val result = sheetsRepository.syncAllMaterias(userEmail)) {
            is NetworkResult.Success -> {
                userPrefsRepository.touchLastSync()
                Timber.i("$TAG: sync completada — ${result.data} materia(s) sincronizada(s)")
                Result.success()
            }

            is NetworkResult.Error -> {
                Timber.e(result.cause, "$TAG: error de sync — ${result.message}")

                if (result.needsUserRecovery) {
                    // El usuario necesita conceder permisos OAuth2.
                    // No reintentamos: el usuario debe actuar primero.
                    Timber.w("$TAG: se requiere acción del usuario para OAuth2, abortando sync")
                    Result.failure()
                } else {
                    // Error de red transitorio: reintentar con back-off
                    Result.retry()
                }
            }

            is NetworkResult.Loading -> Result.retry() // No debería ocurrir
        }
    }
}
