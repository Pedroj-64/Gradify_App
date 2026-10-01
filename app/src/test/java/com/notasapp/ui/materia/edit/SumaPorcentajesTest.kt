package com.notasapp.ui.materia.edit

import com.notasapp.domain.model.Componente
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SumaPorcentajesTest {
    private fun c(p: Float) = Componente(id = 0, materiaId = 1, nombre = "C", porcentaje = p, orden = 0)

    @Test fun `exactamente 100 es valido, incluso con ruido de decimales`() {
        assertTrue(EditPorcentajesViewModel.sumaEs100(listOf(c(0.3f), c(0.3f), c(0.4f))))
        assertTrue(EditPorcentajesViewModel.sumaEs100(listOf(c(0.33f), c(0.33f), c(0.34f))))
    }

    @Test fun `90 y 110 por ciento se rechazan`() {
        assertFalse(EditPorcentajesViewModel.sumaEs100(listOf(c(0.5f), c(0.4f))))
        assertFalse(EditPorcentajesViewModel.sumaEs100(listOf(c(0.6f), c(0.5f))))
    }

    @Test fun `lista vacia no suma 100`() {
        assertFalse(EditPorcentajesViewModel.sumaEs100(emptyList()))
    }
}
