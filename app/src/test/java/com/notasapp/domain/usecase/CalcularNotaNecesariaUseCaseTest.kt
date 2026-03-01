package com.notasapp.domain.usecase

import com.notasapp.domain.model.Componente
import com.notasapp.domain.model.SubNota
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests unitarios para [CalcularNotaNecesariaUseCase].
 */
class CalcularNotaNecesariaUseCaseTest {

    private val useCase = CalcularNotaNecesariaUseCase()

    private fun componente(
        id: Long = 1,
        porcentaje: Float = 0.3f,
        subNotas: List<SubNota> = emptyList()
    ) = Componente(
        id = id,
        materiaId = 1,
        nombre = "Comp $id",
        porcentaje = porcentaje,
        orden = 0,
        subNotas = subNotas
    )

    private fun subNotaConValor(valor: Float?) = SubNota(
        id = 1,
        componenteId = 1,
        descripcion = "Test",
        porcentajeDelComponente = 1.0f,
        valor = valor
    )

    @Test
    fun `meta ya alcanzada cuando todos los componentes tienen nota`() {
        val comps = listOf(
            componente(1, 0.5f, listOf(subNotaConValor(4.0f))),
            componente(2, 0.5f, listOf(subNotaConValor(4.0f)))
        )
        val result = useCase(comps, metaFinal = 3.0f, escalaMax = 5.0f)
        assertTrue(result is CalcularNotaNecesariaUseCase.Resultado.MetaYaAlcanzada)
    }

    @Test
    fun `posible cuando la nota necesaria esta dentro de la escala`() {
        val comps = listOf(
            componente(1, 0.3f, listOf(subNotaConValor(4.0f))),
            componente(2, 0.3f),  // sin nota
            componente(3, 0.4f)   // sin nota
        )
        val result = useCase(comps, metaFinal = 3.0f, escalaMax = 5.0f)
        assertTrue(result is CalcularNotaNecesariaUseCase.Resultado.Posible)
        val posible = result as CalcularNotaNecesariaUseCase.Resultado.Posible
        // Aporte existente: 4.0 * 0.3 = 1.2
        // Necesita: (3.0 - 1.2) / 0.7 = 2.571...
        assertEquals(2.57f, posible.notaNecesaria, 0.01f)
        assertEquals(2, posible.componentesFaltantes.size)
    }

    @Test
    fun `imposible cuando nota necesaria supera escala max`() {
        val comps = listOf(
            componente(1, 0.5f, listOf(subNotaConValor(1.0f))),
            componente(2, 0.5f)  // sin nota
        )
        // Aporte: 1.0 * 0.5 = 0.5. Necesita: (5.0 - 0.5) / 0.5 = 9.0 > 5.0
        val result = useCase(comps, metaFinal = 5.0f, escalaMax = 5.0f)
        assertTrue(result is CalcularNotaNecesariaUseCase.Resultado.Imposible)
    }

    @Test
    fun `meta ya alcanzada cuando nota necesaria es negativa`() {
        val comps = listOf(
            componente(1, 0.6f, listOf(subNotaConValor(5.0f))),
            componente(2, 0.4f)  // sin nota
        )
        // Aporte: 5.0 * 0.6 = 3.0. Meta 3.0 → necesita (3.0-3.0)/0.4 = 0.0 → ya alcanzada
        val result = useCase(comps, metaFinal = 3.0f, escalaMax = 5.0f)
        assertTrue(result is CalcularNotaNecesariaUseCase.Resultado.MetaYaAlcanzada)
    }
}
