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
 * Widget compacto que muestra la materia con mejor/peor desempeño.
 *
 * Muestra: nombre de la materia, nota actual, barra de progreso y estado.
 * Si hay materias en riesgo, muestra la peor; si todas van bien, muestra la mejor.
 * Esto da un "vistazo rápido" al panorama académico.
 */
class MateriaWidgetProvider : AppWidgetProvider() {

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

                    val views = RemoteViews(context.packageName, R.layout.widget_materia)
                    var abrirId = -1L

                    if (usuario != null) {
                        val materias = db.materiaDao()
                            .getMateriasConComponentesOnce(usuario.googleId)
                            .map { it.toDomain() }
                            .filter { !it.archivada }
                            .filter { it.promedio != null }

                        if (materias.isNotEmpty()) {
                            // Si hay materias en riesgo, mostramos la peor para alertar.
                            // Si todas van bien, mostramos la mejor para motivar.
                            val enRiesgo = materias.filter { !it.aprobado }
                            val destacada = if (enRiesgo.isNotEmpty()) {
                                enRiesgo.minByOrNull { it.promedio ?: 0f }!!
                            } else {
                                materias.maxByOrNull { it.promedio ?: 0f }!!
                            }

                            val nota = destacada.promedioDisplay
                            val progreso = kotlin.math.round(destacada.porcentajeEvaluado * 100).toInt()

                            val estado = when {
                                destacada.yaAprobo -> context.getString(R.string.wm_passed)
                                destacada.aprobado -> context.getString(R.string.wm_passing)
                                destacada.notaNecesariaParaAprobar != null ->
                                    context.getString(R.string.wm_needs, destacada.notaNecesariaParaAprobar)
                                else -> context.getString(R.string.wm_at_risk)
                            }

                            views.setTextViewText(R.id.tv_materia_nombre, destacada.nombre)
                            abrirId = destacada.id
                            views.setTextViewText(R.id.tv_materia_nota, nota)
                            views.setTextViewText(R.id.tv_materia_estado, estado)
                            views.setProgressBar(R.id.pb_materia_progreso, 100, progreso, false)
                            views.setTextViewText(R.id.tv_materia_progreso_label, context.getString(R.string.wr_evaluated, progreso))
                        } else {
                            views.setTextViewText(R.id.tv_materia_nombre, context.getString(R.string.wm_no_grades))
                            views.setTextViewText(R.id.tv_materia_nota, "–")
                            views.setTextViewText(R.id.tv_materia_estado, context.getString(R.string.wr_msg_empty))
                            views.setProgressBar(R.id.pb_materia_progreso, 100, 0, false)
                            views.setTextViewText(R.id.tv_materia_progreso_label, "")
                        }
                    } else {
                        views.setTextViewText(R.id.tv_materia_nombre, "Gradify")
                        views.setTextViewText(R.id.tv_materia_nota, "– –")
                        views.setTextViewText(R.id.tv_materia_estado, context.getString(R.string.w_open_app))
                        views.setProgressBar(R.id.pb_materia_progreso, 100, 0, false)
                        views.setTextViewText(R.id.tv_materia_progreso_label, "")
                    }

                    // Click en el widget abre la app
                    // Tocar el widget abre esa materia (antes solo abría la app en Inicio).
                    val launchIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        if (abrirId > 0) putExtra(MainActivity.EXTRA_OPEN_MATERIA, abrirId)
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context, 1, launchIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_materia_root, pendingIntent)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                    Timber.d("Widget Materia actualizado")

                } catch (e: Exception) {
                    Timber.e(e, "Error al actualizar widget de materia")
                }
            }
        }
    }
}
