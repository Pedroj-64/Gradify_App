package com.notasapp.data.worker

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.notasapp.data.local.AppDatabase
import com.notasapp.data.local.UserPreferencesRepository
import com.notasapp.data.local.dao.UsuarioDao
import com.notasapp.utils.BackupFolder
import com.notasapp.utils.BackupManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Respaldo automático periódico en la carpeta que eligió el usuario.
 *
 * - Usa el mismo JSON que "Exportar backup" ([BackupManager.buildJsonOrNull]), por lo que
 *   cualquier respaldo automático se puede restaurar desde Ajustes.
 * - Sin carpeta elegida no hace nada (nunca guarda dentro de la app, donde se perdería al desinstalar).
 * - Sin materias no escribe ni rota archivos: así un respaldo vacío no reemplaza copias buenas.
 */
@HiltWorker
class AutoBackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val db: AppDatabase,
    private val usuarioDao: UsuarioDao,
    private val backupManager: BackupManager,
    private val userPrefsRepository: UserPreferencesRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "AutoBackupWorker"
        const val FILE_PREFIX = "gradify_backup_"
        private const val MAX_BACKUPS = 5
        private const val MAX_ATTEMPTS = 3
    }

    override suspend fun doWork(): Result {
        val folder = userPrefsRepository.backupFolderUri.first()?.let(Uri::parse)
        if (folder == null) {
            Timber.d("$TAG: sin carpeta de respaldos, nada que hacer")
            return Result.success()
        }
        val usuario = usuarioDao.getUsuarioActivoOnce() ?: return Result.success()

        return try {
            val json = backupManager.buildJsonOrNull(db, usuario.googleId)
            if (json == null) {
                Timber.d("$TAG: sin materias, se omite el respaldo")
                return Result.success()
            }
            val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            BackupFolder.write(applicationContext, folder, "$FILE_PREFIX$stamp.json", json)
            BackupFolder.prune(applicationContext, folder, FILE_PREFIX, MAX_BACKUPS)
            userPrefsRepository.touchLastBackup()
            Timber.i("$TAG: respaldo automático completado")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "$TAG: error al crear respaldo automático")
            // Un fallo permanente (carpeta borrada, permiso revocado) no debe reintentarse para siempre.
            if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure()
        }
    }
}
