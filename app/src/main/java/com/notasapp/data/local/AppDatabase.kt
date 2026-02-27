package com.notasapp.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.notasapp.data.local.dao.ComponenteDao
import com.notasapp.data.local.dao.MateriaDao
import com.notasapp.data.local.dao.SubNotaDao
import com.notasapp.data.local.dao.UsuarioDao
import com.notasapp.data.local.entities.ComponenteEntity
import com.notasapp.data.local.entities.MateriaEntity
import com.notasapp.data.local.entities.SubNotaEntity
import com.notasapp.data.local.entities.UsuarioEntity

/**
 * Base de datos local de NotasApp (Room / SQLite).
 *
 * Centraliza el acceso a todas las tablas.
 * La instancia es singleton y se provee mediante Hilt (ver [DatabaseModule]).
 *
 * **Versioning:** al agregar columnas o tablas, incrementar [version]
 * y agregar una [androidx.room.migration.Migration].
 */
@Database(
    entities = [
        UsuarioEntity::class,
        MateriaEntity::class,
        ComponenteEntity::class,
        SubNotaEntity::class
    ],
    version = 1,
    exportSchema = true     // genera JSON en /schemas para historial de migraciones
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun materiaDao(): MateriaDao
    abstract fun componenteDao(): ComponenteDao
    abstract fun subNotaDao(): SubNotaDao

    companion object {

        const val DATABASE_NAME = "notas_app.db"

        /**
         * Crea la instancia de Room.
         * Llamado únicamente desde [DatabaseModule] (Hilt).
         * No llamar directamente desde código de producto.
         */
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration() // solo en desarrollo; reemplazar por Migrations en producción
                .build()
    }
}
