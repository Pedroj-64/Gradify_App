package com.notasapp.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.notasapp.R
import com.notasapp.data.mapper.toDomain
import com.notasapp.di.WidgetEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * AppWidgetProvider para el widget de promedio del semestre.
 *
 * Muestra en la pantalla de inicio del dispositivo:
 * - El promedio ponderado de todas las materias del usuario activo.
 * - La cantidad de materias registradas.
 *
 * Usa [WidgetEntryPoint] para acceder al [AppDatabase] del proceso
 * (singleton Hilt) sin crear instancias adicionales.
 *
 * El widget se actualiza automáticamente cada hora (configurado en
 * `res/xml/widget_promedio_info.xml`) y también cuando el sistema
 * lo solicita tras agregar o redimensionar el widget.
 */
class PromedioWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    companion object {

        /**
         * Recalcula los datos desde Room y actualiza las [RemoteViews] del widget.
         *
         * Corre en [Dispatchers.IO] para no bloquear el hilo principal.
         * Si el usuario no ha iniciado sesión, muestra "– –" y "Inicia sesión".
         */
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val entryPoint = EntryPointAccessors.fromApplication(
                        context.applicationContext,
                        WidgetEntryPoint::class.java
                    )
                    val db = entryPoint.database()

                    val usuario = db.usuarioDao().getUsuarioActivoOnce()

                    val (promedioText, subtitleText) = if (usuario != null) {
                        val materias = db.materiaDao()
                            .getMateriasConComponentesOnce(usuario.googleId)
                            .map { it.toDomain() }

                        val promedios = materias.mapNotNull { it.promedio }

                        val promedioStr = if (promedios.isNotEmpty()) {
                            "%.2f".format(promedios.average())
                        } else {
                            "– –"
                        }
                        promedioStr to "${materias.size} materia${if (materias.size != 1) "s" else ""}"
                    } else {
                        "– –" to "Inicia sesión"
                    }

                    val views = RemoteViews(context.packageName, R.layout.widget_promedio)
                    views.setTextViewText(R.id.tv_widget_promedio, promedioText)
                    views.setTextViewText(R.id.tv_widget_materias, subtitleText)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                    Timber.d("Widget actualizado: promedio=$promedioText")

                } catch (e: Exception) {
                    Timber.e(e, "Error al actualizar widget de promedio")
                }
            }
        }
    }
}
