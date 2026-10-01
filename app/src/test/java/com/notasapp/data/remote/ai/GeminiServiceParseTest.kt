package com.notasapp.data.remote.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class GeminiServiceParseTest {

    private val item =
        """{"tipo":"LIBRO","titulo":"Cálculo","descripcion":"Clásico","url":"https://x.co","autor":"Stewart"}"""

    @Test
    fun `parsea array limpio`() {
        val r = GeminiService.parseRecomendaciones("[$item]")
        assertEquals(1, r.size)
        assertEquals(TipoRecomendacion.LIBRO, r[0].tipo)
        assertEquals("Stewart", r[0].autor)
    }

    @Test
    fun `ignora fences de markdown y texto alrededor`() {
        val r = GeminiService.parseRecomendaciones("Aquí tienes:\n```json\n[$item]\n```\nSuerte")
        assertEquals(1, r.size)
    }

    @Test
    fun `tipo desconocido cae en RECURSO y autor null se descarta`() {
        val raw = """[{"tipo":"PODCAST","titulo":"t","descripcion":"d","url":"u","autor":null}]"""
        val r = GeminiService.parseRecomendaciones(raw)
        assertEquals(TipoRecomendacion.RECURSO, r[0].tipo)
        assertNull(r[0].autor)
    }

    @Test
    fun `array vacio o texto sin JSON falla`() {
        for (bad in listOf("[]", "no hay json", "")) {
            try {
                GeminiService.parseRecomendaciones(bad)
                fail("debió fallar con: '$bad'")
            } catch (e: Exception) {
                assertTrue(e.message!!.isNotBlank())
            }
        }
    }
}
