package com.notasapp.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MateriaPromedioTest {
    private fun sub(pct: Float, valor: Float?) = SubNota(componenteId = 1, descripcion = "n", porcentajeDelComponente = pct, valor = valor)
    private fun comp(pct: Float, vararg subs: SubNota) = Componente(materiaId = 1, nombre = "c", porcentaje = pct, subNotas = subs.toList())
    private fun materia(vararg c: Componente) = Materia(usuarioId = "u", nombre = "m", periodo = "p", componentes = c.toList())

    @Test fun `el promedio es el rendimiento en lo evaluado, no el acumulado`() {
        // 4.5 en el 20 % y 2.0 en el 30 %: acumulado 1.5, pero va en 3.0
        val m = materia(comp(0.2f, sub(1f, 4.5f)), comp(0.3f, sub(1f, 2f)), comp(0.5f, sub(1f, null)))
        assertEquals(1.5f, m.acumulado, 0.001f)
        assertEquals(3.0f, m.promedio!!, 0.001f)
        assertTrue(m.aprobado)          // va pasando: no es "en riesgo"
        assertFalse(m.yaAprobo)         // pero aún no lo tiene asegurado
    }

    @Test fun `un corte a medias cuenta a medias`() {
        val m = materia(comp(0.5f, sub(0.5f, 4f), sub(0.5f, null)), comp(0.5f, sub(1f, null)))
        assertEquals(0.25f, m.porcentajeEvaluado, 0.001f)
        assertEquals(4f, m.promedio!!, 0.001f)
    }

    @Test fun `porcentajes con medio punto se respetan`() {
        val m = materia(comp(0.205f, sub(1f, 5f)), comp(0.795f, sub(1f, null)))
        assertEquals(0.205f, m.porcentajeEvaluado, 0.0001f)
        assertEquals(5f, m.promedio!!, 0.001f)
    }

    @Test fun `sin notas no hay promedio y completa es igual al acumulado`() {
        assertEquals(null, materia(comp(1f, sub(1f, null))).promedio)
        val m = materia(comp(0.4f, sub(1f, 3f)), comp(0.6f, sub(1f, 4f)))
        assertEquals(m.acumulado, m.promedio!!, 0.001f)
    }
}
