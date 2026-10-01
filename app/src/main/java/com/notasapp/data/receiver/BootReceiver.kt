package com.notasapp.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.notasapp.data.local.AppDatabase
import com.notasapp.domain.model.TipoEvento
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

/** Reprograma las alarmas de exámenes tras reiniciar el teléfono o actualizar la app. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val now = System.currentTimeMillis()
                val scheduler = ExamAlarmScheduler(context)
                AppDatabase.getInstance(context).examenEventDao()
                    .getFutureEventsWithReminder(now)
                    .forEach { e ->
                        scheduler.scheduleAlarm(
                            eventId = e.id,
                            title = e.titulo,
                            description = e.descripcion,
                            tipoEvento = e.tipoEvento,
                            triggerAtMs = e.fechaEpochMs - e.recordatorioMinutos * 60_000L,
                            materiaId = e.materiaId,
                            reminderMinutes = e.recordatorioMinutos
                        )
                    }
            } catch (ex: Exception) {
                Timber.e(ex, "BootReceiver: no se pudieron reprogramar las alarmas")
            } finally {
                pending.finish()
            }
        }
    }
}
