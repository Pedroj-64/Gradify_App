package com.notasapp.di

import com.notasapp.data.remote.sheets.SheetsRepositoryImpl
import com.notasapp.domain.repository.SheetsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para el subsistema de Google Sheets.
 *
 * Vincula la interfaz [SheetsRepository] con su implementación concreta
 * [SheetsRepositoryImpl]. [SheetsService] es `@Singleton` y se inyecta
 * directamente vía constructor, por lo que no requiere @Provides manual.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SheetsModule {

    @Binds
    @Singleton
    abstract fun bindSheetsRepository(
        impl: SheetsRepositoryImpl
    ): SheetsRepository
}
