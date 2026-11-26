package com.notasapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.notasapp.MainActivity
import com.notasapp.R
import com.notasapp.data.mapper.toDomain
import com.notasapp.di.WidgetEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Widget de resumen del semestre con estadísticas completas.
 *
 * Muestra: promedio general, total de materias, cuántas aprobando,
 * cuántas en riesgo, barra de progreso del semestre y un mensaje
 * motivacional.
 */
class ResumenSemestreWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // goAsync: sin esto el sistema puede matar el proceso antes de que el widget termine de dibujarse.
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                appWidgetIds.forEach { updateWidget(context, appWidgetManager, it) }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {

        suspend fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            run {
                try {
                    val entryPoint = EntryPointAccessors.fromApplication(
                        context.applicationContext,
                        WidgetEntryPoint::class.java
                    )
                    val db = entryPoint.database()
                    val usuario = db.usuarioDao().getUsuarioActivoOnce()

                    val views = RemoteViews(context.packageName, R.layout.widget_resumen_semestre)

                    if (usuario != null) {
                        val materias = db.materiaDao()
                            .getMateriasConComponentesOnce(usuario.googleId)
                            .map { it.toDomain() }
                            .filter { !it.archivada }

                        // Promedio general
                        val promedios = materias.mapNotNull { it.promedio }
                        val promedioStr = if (promedios.isNotEmpty()) {
                            "%.2f".format(promedios.average())
                        } else "– –"

                        // Conteos
                        val total = materias.size
                        val aprobando = materias.count { it.aprobado }
                        val enRiesgo = materias.count { !it.aprobado && it.promedio != null }

                        // Progreso del semestre (promedio de porcentajeEvaluado)
                        val progresoSemestre = if (materias.isNotEmpty()) {
                            kotlin.math.round(materias.sumOf { it.porcentajeEvaluado.toDouble() }.toFloat() / materias.size * 100).toInt()
                        } else 0

                        // Periodo
                        val periodo = materias.firstOrNull()?.periodo ?: ""

                        // Mensaje motivacional
                        // Sobre la fracción de la escala de cada materia: antes los umbrales eran fijos
                        // de 0–5 (4.5, 4.0…) y en escalas 0–10 o 0–20 siempre decía "excepcional".
                        val fracciones = materias.mapNotNull { m -> m.promedio?.let { m.fraccionEscala(it) } }
                        val f = fracciones.average()
                        val mensaje = context.getString(when {
                            fracciones.isEmpty() -> R.string.wr_msg_empty
                            f >= 0.9 -> R.string.wr_msg_top
                            f >= 0.8 -> R.string.wr_msg_great
                            f >= 0.7 -> R.string.wr_msg_good
                            f >= 0.6 -> R.string.wr_msg_ok
                            else -> R.string.wr_msg_push
                        })

                        views.setTextViewText(R.id.tv_resumen_promedio, promedioStr)
                        views.setTextViewText(R.id.tv_resumen_materias, "$total")
                        views.setTextViewText(R.id.tv_resumen_aprobadas, "$aprobando")
                        views.setTextViewText(R.id.tv_resumen_riesgo, "$enRiesgo")
                        views.setProgressBar(R.id.pb_resumen_progreso, 100, progresoSemestre, false)
                        views.setTextViewText(R.id.tv_resumen_progreso_label, context.getString(R.string.wr_progress, progresoSemestre))
                        views.setTextViewText(R.id.tv_widget_periodo, periodo)
                        views.setTextViewText(R.id.tv_resumen_mensaje, mensaje)
                    } else {
                        views.setTextViewText(R.id.tv_resumen_promedio, "– –")
                        views.setTextViewText(R.id.tv_resumen_materias, "0")
                        views.setTextViewText(R.id.tv_resumen_aprobadas, "0")
                        views.setTextViewText(R.id.tv_resumen_riesgo, "0")
                        views.setProgressBar(R.id.pb_resumen_progreso, 100, 0, false)
                        views.setTextViewText(R.id.tv_resumen_progreso_label, context.getString(R.string.w_open_app))
                        views.setTextViewText(R.id.tv_resumen_mensaje, "")
                    }

                    // Click en el widget abre la app
                    val launchIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context, 2, launchIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_resumen_root, pendingIntent)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                    Timber.d("Widget Resumen actualizado")

                } catch (e: Exception) {
                    Timber.e(e, "Error al actualizar widget de resumen")
                }
            }
        }
    }
}
