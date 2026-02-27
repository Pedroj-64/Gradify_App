package com.notasapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * Clase principal de la aplicación NotasApp.
 *
 * Inicializa Hilt (inyección de dependencias), Timber (logging),
 * y configura WorkManager para usar HiltWorkerFactory.
 */
@HiltAndroidApp
class NotasApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        initTimber()
    }

    /**
     * Configura WorkManager con HiltWorkerFactory para que los Workers
     * puedan recibir dependencias inyectadas por Hilt.
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    /**
     * Inicializa Timber únicamente en builds de debug.
     * En release no se registra ningún árbol para evitar logs en producción.
     */
    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
