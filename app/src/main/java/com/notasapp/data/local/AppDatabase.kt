package com.notasapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.notasapp.data.local.dao.ComponenteDao
import com.notasapp.data.local.dao.MateriaDao
import com.notasapp.data.local.dao.SubNotaDao
import com.notasapp.data.local.dao.SubNotaDetailDao
import com.notasapp.data.local.dao.UsuarioDao
import com.notasapp.data.local.entities.ComponenteEntity
import com.notasapp.data.local.entities.MateriaEntity
import com.notasapp.data.local.entities.SubNotaDetailEntity
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
        SubNotaEntity::class,
        SubNotaDetailEntity::class
    ],
    version = 3,
    exportSchema = true     // genera JSON en /schemas para historial de migraciones
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun materiaDao(): MateriaDao
    abstract fun componenteDao(): ComponenteDao
    abstract fun subNotaDao(): SubNotaDao
    abstract fun subNotaDetailDao(): SubNotaDetailDao

    companion object {

        const val DATABASE_NAME = "notas_app.db"

        /**
         * Migración v1 → v2: agrega la columna `fechaLimite` a la tabla `componentes`.
         * Permite guardar fechas de evaluación para el sistema de recordatorios.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE componentes ADD COLUMN fechaLimite INTEGER DEFAULT NULL"
                )
            }
        }

        /**
         * Migración v2 → v3: crea la tabla `sub_nota_details` para sub-notas compuestas.
         * Permite que una sub-nota contenga múltiples detalles con pesos propios.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `sub_nota_details` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `subNotaId` INTEGER NOT NULL,
                        `descripcion` TEXT NOT NULL,
                        `porcentaje` REAL NOT NULL,
                        `valor` REAL,
                        FOREIGN KEY(`subNotaId`) REFERENCES `sub_notas`(`id`) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_sub_nota_details_subNotaId` ON `sub_nota_details` (`subNotaId`)"
                )
            }
        }

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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigration() // fallback de seguridad para dev
                .build()
    }
}
