package com.notasapp.domain.usecase

import com.notasapp.domain.model.Componente
import com.notasapp.domain.model.SubNota
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests unitarios para [SimularNotaUseCase].
 */
class SimularNotaUseCaseTest {

    private val useCase = SimularNotaUseCase()

    private fun componente(
        id: Long,
        porcentaje: Float,
        conNota: Float? = null
    ) = Componente(
        id = id,
        materiaId = 1,
        nombre = "Comp $id",
        porcentaje = porcentaje,
        orden = 0,
        subNotas = if (conNota != null) listOf(
            SubNota(1, id, "Nota", 1.0f, conNota)
        ) else listOf(
            SubNota(1, id, "Nota", 1.0f, null)
        )
    )

    @Test
    fun `simular con componentes faltantes`() {
        val comps = listOf(
            componente(1, 0.3f, conNota = 4.0f),  // aporte = 4.0*0.3 = 1.2
            componente(2, 0.3f),                    // faltante
            componente(3, 0.4f)                     // faltante
        )
        val hipoteticas = mapOf(2L to 3.5f, 3L to 4.5f)
        val result = useCase(comps, hipoteticas)

        // 1.2 + (3.5*0.3) + (4.5*0.4) = 1.2 + 1.05 + 1.8 = 4.05
        assertFalse(result.completamenteEvaluada)
        assertEquals(4.05f, result.notaFinalProyectada, 0.1f)
        assertEquals(2, result.componentesSimulados.size)
    }

    @Test
    fun `simular cuando todo esta evaluado`() {
        val comps = listOf(
            componente(1, 0.5f, conNota = 4.0f),
            componente(2, 0.5f, conNota = 3.0f)
        )
        val result = useCase(comps, emptyMap())
        assertTrue(result.completamenteEvaluada)
        // 4.0*0.5 + 3.0*0.5 = 3.5
        assertEquals(3.5f, result.notaFinalProyectada, 0.01f)
    }

    @Test
    fun `simular con notas hipoteticas en 0 por defecto`() {
        val comps = listOf(
            componente(1, 0.5f, conNota = 4.0f),
            componente(2, 0.5f)  // faltante, sin hipotética proporcionada
        )
        val result = useCase(comps, emptyMap())  // no envía hipotéticas
        // 4.0*0.5 + 0*0.5 = 2.0
        assertEquals(2.0f, result.notaFinalProyectada, 0.01f)
    }
}
