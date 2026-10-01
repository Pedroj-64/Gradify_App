package com.notasapp.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.notasapp.data.local.entities.MateriaEntity
import com.notasapp.data.local.entities.UsuarioEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ArchivarSemestreTest {

    private lateinit var db: AppDatabase

    @Before fun setUp() = runBlocking {
        val ctx: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java).allowMainThreadQueries().build()
        db.usuarioDao().insertOrUpdate(UsuarioEntity(googleId = "u1", nombre = "T", email = "", ultimaSyncMs = 0))
        db.usuarioDao().insertOrUpdate(UsuarioEntity(googleId = "u2", nombre = "Otro", email = "", ultimaSyncMs = 0))
        listOf("2026-1" to "u1", "2026-1" to "u1", "2026-2" to "u1", "2026-1" to "u2").forEachIndexed { i, (p, u) ->
            db.materiaDao().insert(MateriaEntity(usuarioId = u, nombre = "M$i", periodo = p))
        }
    }

    @After fun tearDown() = db.close()

    private suspend fun archivadas(u: String) =
        db.materiaDao().getMateriasConComponentesOnce(u).count { it.materia.archivada }

    @Test fun cerrarSemestre_soloAfectaEsePeriodoYEseUsuario() = runBlocking {
        db.materiaDao().setPeriodoArchivado("u1", "2026-1", true)
        assertEquals(2, archivadas("u1"))   // las dos de 2026-1
        assertEquals(0, archivadas("u2"))   // otro usuario intacto
    }

    @Test fun reabrir_devuelveLasMateriasAlSemestreActivo() = runBlocking {
        db.materiaDao().setPeriodoArchivado("u1", "2026-1", true)
        db.materiaDao().setPeriodoArchivado("u1", "2026-1", false)
        assertEquals(0, archivadas("u1"))
    }
}
