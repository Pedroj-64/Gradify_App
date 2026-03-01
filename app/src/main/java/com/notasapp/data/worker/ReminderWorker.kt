package com.notasapp.data.worker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.notasapp.data.local.dao.ComponenteDao
import com.notasapp.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Worker diario que comprueba si hay componentes de evaluación con fecha
 * límite en los próximos 7 días y envía un recordatorio por cada uno.
 *
 * Requiere el permiso [Manifest.permission.POST_NOTIFICATIONS] en Android 13+
 * (API 33). Si el permiso no está concedido, el worker termina silenciosamente.
 *
 * El campo [ComponenteEntity.fechaLimite] debe estar poblado para que se
 * genere alguna notificación; materias sin fecha son ignoradas.
 */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val ctx: android.content.Context,
    @Assisted params: WorkerParameters,
    private val componenteDao: ComponenteDao,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(ctx, params) {

    companion object {
        /** Tag único para referenciar este worker en WorkManager. */
        const val TAG = "ReminderWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            // Android 13+: respetar el permiso de notificaciones
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(
                    ctx, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted) {
                    Timber.d("ReminderWorker: permiso POST_NOTIFICATIONS no concedido, saltando")
                    return Result.success()
                }
            }

            val nowMs   = System.currentTimeMillis()
            val limitMs = nowMs + TimeUnit.DAYS.toMillis(7)

            val proximos = componenteDao.getComponentesConFechaProxima(nowMs, limitMs)

            proximos.forEachIndexed { index, componente ->
                val diasRestantes = TimeUnit.MILLISECONDS.toDays(
                    componente.fechaLimite!! - nowMs
                )
                val mensaje = when (diasRestantes) {
                    0L   -> "¡Hoy es la evaluación: ${componente.nombre}!"
                    1L   -> "Mañana tienes: ${componente.nombre}"
                    else -> "En $diasRestantes días: ${componente.nombre}"
                }
                notificationHelper.sendReminder(
                    title          = "📚 Recordatorio de evaluación",
                    message        = mensaje,
                    notificationId = (componente.id % 10_000L).toInt() + index
                )
            }

            Timber.i("ReminderWorker: ${proximos.size} recordatorio(s) enviado(s)")
            Result.success()

        } catch (e: Exception) {
            Timber.e(e, "Error en ReminderWorker")
            Result.failure()
        }
    }
}
