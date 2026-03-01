package com.notasapp.domain.usecase

import com.notasapp.domain.model.Materia
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests unitarios para [CalcularPromedioPonderadoUseCase].
 */
class CalcularPromedioPonderadoUseCaseTest {

    private val useCase = CalcularPromedioPonderadoUseCase()

    private fun materia(
        promedio: Float?,
        creditos: Int = 3,
        notaAprobacion: Float = 3.0f
    ): Materia {
        return Materia(
            id = 0,
            usuarioId = "test",
            nombre = "Test",
            periodo = "2026-1",
            creditos = creditos,
            notaAprobacion = notaAprobacion,
            // Simulamos un promedio creando un componente con sub-nota
            componentes = if (promedio != null) listOf(
                com.notasapp.domain.model.Componente(
                    id = 1, materiaId = 0, nombre = "C1", porcentaje = 1.0f, orden = 0,
                    subNotas = listOf(
                        com.notasapp.domain.model.SubNota(1, 1, "N1", 1.0f, promedio)
                    )
                )
            ) else emptyList()
        )
    }

    @Test
    fun `promedio ponderado calcula correctamente`() {
        val materias = listOf(
            materia(promedio = 4.0f, creditos = 3),  // 4.0 * 3 = 12
            materia(promedio = 3.0f, creditos = 4),  // 3.0 * 4 = 12
            materia(promedio = 5.0f, creditos = 2)   // 5.0 * 2 = 10
        )
        val result = useCase(materias)
        // (12+12+10) / (3+4+2) = 34/9 = 3.78
        assertNotNull(result.promedioPonderado)
        assertEquals(3.78f, result.promedioPonderado!!, 0.01f)
    }

    @Test
    fun `promedio simple cuando no hay creditos`() {
        val materias = listOf(
            materia(promedio = 4.0f, creditos = 0),
            materia(promedio = 3.0f, creditos = 0)
        )
        val result = useCase(materias)
        assertNull(result.promedioPonderado)
        assertNotNull(result.promedioSimple)
        assertEquals(3.5f, result.promedioSimple!!, 0.01f)
    }

    @Test
    fun `sin materias con notas`() {
        val materias = listOf(
            materia(promedio = null, creditos = 3),
            materia(promedio = null, creditos = 4)
        )
        val result = useCase(materias)
        assertNull(result.promedioSimple)
        assertNull(result.promedioPonderado)
        assertEquals(0, result.materiasAprobadas)
    }

    @Test
    fun `cuenta aprobadas y reprobadas`() {
        val materias = listOf(
            materia(promedio = 4.0f, creditos = 3, notaAprobacion = 3.0f),  // aprobada
            materia(promedio = 2.0f, creditos = 4, notaAprobacion = 3.0f),  // reprobada
            materia(promedio = 3.5f, creditos = 2, notaAprobacion = 3.0f)   // aprobada
        )
        val result = useCase(materias)
        assertEquals(2, result.materiasAprobadas)
        assertEquals(1, result.materiasReprobadas)
        assertEquals(5, result.creditosAprobados) // 3 + 2
        assertEquals(9, result.totalCreditos)
    }

    @Test
    fun `display muestra ponderado si disponible`() {
        val materias = listOf(
            materia(promedio = 4.0f, creditos = 3),
            materia(promedio = 3.0f, creditos = 4)
        )
        val result = useCase(materias)
        assertNotEquals("--", result.promedioDisplay)
    }

    @Test
    fun `display muestra -- sin datos`() {
        val result = useCase(emptyList())
        assertEquals("--", result.promedioDisplay)
    }
}
