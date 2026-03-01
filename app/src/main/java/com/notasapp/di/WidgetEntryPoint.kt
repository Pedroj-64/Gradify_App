package com.notasapp.di

import com.notasapp.data.local.AppDatabase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt EntryPoint para componentes no-estándar que no soportan @AndroidEntryPoint
 * (como AppWidgetProvider).
 *
 * Permite obtener el singleton [AppDatabase] desde el widget de la pantalla de
 * inicio sin instanciar una nueva base de datos.
 *
 * Uso:
 * ```kotlin
 * val entryPoint = EntryPointAccessors.fromApplication(
 *     context.applicationContext, WidgetEntryPoint::class.java
 * )
 * val db = entryPoint.database()
 * ```
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun database(): AppDatabase
}
