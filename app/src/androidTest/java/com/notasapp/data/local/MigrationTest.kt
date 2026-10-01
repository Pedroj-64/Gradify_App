package com.notasapp.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    /** Parte del esquema más viejo exportado (v2) y valida contra el esquema v7. */
    @Test
    fun migra_de_v2_a_v7_conservando_datos() {
        helper.createDatabase("migration-test", 2).apply {
            execSQL("INSERT INTO usuarios (googleId, nombre, email, ultimaSyncMs) VALUES ('u1','Ana','a@a.com',0)")
            close()
        }
        helper.runMigrationsAndValidate(
            "migration-test", 7, true,
            AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4,
            AppDatabase.MIGRATION_4_5, AppDatabase.MIGRATION_5_6,
            AppDatabase.MIGRATION_6_7
        )
    }
}
