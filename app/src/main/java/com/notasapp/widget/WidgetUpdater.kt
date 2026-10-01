package com.notasapp.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import timber.log.Timber

/**
 * Pide a los tres widgets que se redibujen. Se llama desde [com.notasapp.NotasApp] cada vez que
 * cambian las tablas de notas, así que el widget refleja una nota recién guardada al instante
 * (antes solo se refrescaba cada hora).
 */
object WidgetUpdater {

    private val handler = Handler(Looper.getMainLooper())
    private const val DEBOUNCE_MS = 400L

    private val providers = listOf(
        PromedioWidgetProvider::class.java,
        MateriaWidgetProvider::class.java,
        ResumenSemestreWidgetProvider::class.java
    )

    /** Agrupa ráfagas de escrituras (p. ej. una importación) en un solo refresco. */
    fun refreshSoon(context: Context) {
        val app = context.applicationContext
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ refreshAll(app) }, DEBOUNCE_MS)
    }

    fun refreshAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        providers.forEach { cls ->
            val ids = manager.getAppWidgetIds(ComponentName(context, cls))
            Timber.d("WidgetUpdater: ${cls.simpleName} -> ${ids.size} widget(s)")
            if (ids.isNotEmpty()) {
                context.sendBroadcast(
                    Intent(context, cls).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    }
                )
            }
        }
    }
}
