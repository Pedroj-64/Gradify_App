package com.notasapp.utils

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.notasapp.data.local.AppDatabase
import com.notasapp.data.local.entities.ComponenteEntity
import com.notasapp.data.local.entities.ExamenEventEntity
import com.notasapp.data.local.entities.MateriaEntity
import com.notasapp.data.local.entities.SubNotaDetailEntity
import com.notasapp.data.local.entities.SubNotaEntity
import com.notasapp.data.local.entities.UsuarioEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/** Ida y vuelta del backup, idempotencia y reversión ante un archivo defectuoso. */
class BackupManagerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val manager = BackupManager(context)
    private lateinit var origen: AppDatabase
    private lateinit var destino: AppDatabase

    private fun newDb() = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
        .allowMainThreadQueries().build()

    @Before fun setUp() = runBlocking {
        origen = newDb(); destino = newDb()
        listOf(origen, destino).forEach {
            it.usuarioDao().insertOrUpdate(UsuarioEntity(googleId = "u1", nombre = "T", email = "", ultimaSyncMs = 0))
        }
    }

    @After fun tearDown() { origen.close(); destino.close() }

    /** Nombre hostil a propósito: comillas, barra invertida, salto de línea y acentos. */
    private val nombreHostil = "A \"B\" \\ C\nñandú"

    private suspend fun sembrar(db: AppDatabase) {
        val mid = db.materiaDao().insert(
            MateriaEntity(usuarioId = "u1", nombre = nombreHostil, periodo = "2026-2", profesor = "Dra. \"X\"",
                creditos = 4, notaMeta = 4.5f, notas = "línea 1\nlínea 2", archivada = true)
        )
        val cid = db.componenteDao().insert(ComponenteEntity(materiaId = mid, nombre = "Corte \"1\"", porcentaje = 0.4f, orden = 0))
        val sid = db.subNotaDao().insert(SubNotaEntity(componenteId = cid, descripcion = "Taller", porcentajeDelComponente = 1f, valor = null))
        db.subNotaDetailDao().insert(SubNotaDetailEntity(subNotaId = sid, descripcion = "Parte 1", porcentaje = 1f, valor = 0f))
        db.examenEventDao().insert(
            ExamenEventEntity(materiaId = mid, titulo = "Parcial \"1\"", fechaEpochMs = 1_800_000_000_000, recordatorioMinutos = 60)
        )
    }

    @Test fun sinMaterias_noGeneraRespaldo() = runBlocking {
        assertNull(manager.buildJsonOrNull(origen, "u1"))
    }

    @Test fun idaYVuelta_conservaTodo_incluidoNombreHostilYNotaCero() = runBlocking {
        sembrar(origen)
        val json = manager.buildJsonOrNull(origen, "u1")
        assertNotNull(json)

        assertEquals(1, manager.parseAndInsert(json!!, destino, "u1"))

        val m = destino.materiaDao().getMateriasConComponentesOnce("u1").single()
        assertEquals(nombreHostil, m.materia.nombre)
        assertEquals("Dra. \"X\"", m.materia.profesor)
        assertEquals(4.5f, m.materia.notaMeta)
        assertTrue(m.materia.archivada)                   // semestre cerrado se conserva
        assertEquals("línea 1\nlínea 2", m.materia.notas)
        val sub = m.componentesConSubNotas.single().subNotas.single()
        assertNull(sub.subNota.valor)
        assertEquals(0f, sub.detalles.single().valor)     // un 0 real no se pierde
        assertEquals("Parcial \"1\"", destino.examenEventDao().getByMateriaOnce(m.materia.id).single().titulo)
    }

    @Test fun importarDosVeces_noDuplica() = runBlocking {
        sembrar(origen)
        val json = manager.buildJsonOrNull(origen, "u1")!!
        assertEquals(1, manager.parseAndInsert(json, destino, "u1"))
        assertEquals(0, manager.parseAndInsert(json, destino, "u1"))
        assertEquals(1, destino.materiaDao().getMateriasConComponentesOnce("u1").size)
    }

    @Test fun archivoDefectuoso_noDejaMateriasAMedias() = runBlocking {
        val malo = """
            {"version":2,"materias":[
              {"nombre":"Buena","periodo":"2026-1","componentes":[{"nombre":"C","porcentaje":1.0,"subNotas":[]}]},
              {"nombre":"Mala","periodo":"2026-1","componentes":[{"nombre":"C","porcentaje":9.0,"subNotas":[]}]}
            ]}
        """.trimIndent()
        try {
            manager.parseAndInsert(malo, destino, "u1")
            fail("debía rechazar el porcentaje 9.0")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("inválido"))
        }
        assertEquals(0, destino.materiaDao().getMateriasConComponentesOnce("u1").size)   // 'Buena' revertida
    }

    @Test fun camposOpcionales_usanValoresPorDefecto() = runBlocking {
        val minimo = """{"version":2,"materias":[{"nombre":"Mín","periodo":"2026-1"}]}"""
        assertEquals(1, manager.parseAndInsert(minimo, destino, "u1"))
        val m = destino.materiaDao().getMateriasConComponentesOnce("u1").single().materia
        assertEquals(5f, m.escalaMax)
        assertEquals("NUMERICO_5", m.tipoEscala)
    }
}
