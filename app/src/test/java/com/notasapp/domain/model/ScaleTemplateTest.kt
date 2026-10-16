package com.notasapp.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScaleTemplateTest {

    @Test
    fun `toda plantilla tiene aprobacion dentro de su rango`() {
        ScaleTemplate.entries.forEach {
            assertTrue("${it.name} rango", it.max > it.min)
            assertTrue("${it.name} aprobación", it.aprobacion in it.min..it.max)
        }
    }

    @Test
    fun `fraccionEscala respeta escalas que no empiezan en cero`() {
        val chile = Materia(usuarioId = "u", nombre = "X", periodo = "1", escalaMin = 1f, escalaMax = 7f, notaAprobacion = 4f)
        assertEquals(0f, chile.fraccionEscala(1f), 0.001f)
        assertEquals(0.5f, chile.fraccionEscala(4f), 0.001f)
        assertEquals(1f, chile.fraccionEscala(7f), 0.001f)
    }
}
