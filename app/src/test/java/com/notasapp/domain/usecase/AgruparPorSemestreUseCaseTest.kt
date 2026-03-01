package com.notasapp.domain.usecase

import com.notasapp.domain.model.Materia
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests unitarios para [AgruparPorSemestreUseCase].
 */
class AgruparPorSemestreUseCaseTest {

    private val useCase = AgruparPorSemestreUseCase()

    private fun materia(nombre: String, periodo: String) = Materia(
        id = 0,
        usuarioId = "test",
        nombre = nombre,
        periodo = periodo
    )

    @Test
    fun `agrupa por periodo correctamente`() {
        val materias = listOf(
            materia("Mat 1", "2026-1"),
            materia("Mat 2", "2026-1"),
            materia("Mat 3", "2025-2"),
            materia("Mat 4", "2025-1")
        )
        val semestres = useCase(materias)
        assertEquals(3, semestres.size)
        // Ordenados desc
        assertEquals("2026-1", semestres[0].periodo)
        assertEquals("2025-2", semestres[1].periodo)
        assertEquals("2025-1", semestres[2].periodo)
        // Cantidad por grupo
        assertEquals(2, semestres[0].totalMaterias)
        assertEquals(1, semestres[1].totalMaterias)
        assertEquals(1, semestres[2].totalMaterias)
    }

    @Test
    fun `lista vacia retorna vacia`() {
        assertTrue(useCase(emptyList()).isEmpty())
    }

    @Test
    fun `un solo periodo`() {
        val materias = listOf(
            materia("Mat 1", "2026-1"),
            materia("Mat 2", "2026-1")
        )
        val semestres = useCase(materias)
        assertEquals(1, semestres.size)
        assertEquals(2, semestres[0].totalMaterias)
    }
}
