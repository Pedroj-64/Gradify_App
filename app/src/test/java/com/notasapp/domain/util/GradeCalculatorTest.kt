package com.notasapp.domain.util

import com.notasapp.domain.model.ConfiguracionNota
import com.notasapp.domain.model.ModoRedondeo
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests unitarios para [GradeCalculator].
 * Verifica aritmética BigDecimal, redondeo y simulaciones.
 */
class GradeCalculatorTest {

    // ── Redondeo ────────────────────────────────────────────────

    @Test
    fun `redondear matematico 2 decimales`() {
        val config = ConfiguracionNota(decimales = 2, modoRedondeo = ModoRedondeo.MATEMATICO)
        assertEquals(3.46f, GradeCalculator.redondear(3.456f, config), 0.001f)
        assertEquals(3.45f, GradeCalculator.redondear(3.454f, config), 0.001f)
        assertEquals(4.00f, GradeCalculator.redondear(3.999f, config), 0.001f)
    }

    @Test
    fun `redondear truncar 2 decimales`() {
        val config = ConfiguracionNota(decimales = 2, modoRedondeo = ModoRedondeo.TRUNCAR)
        assertEquals(3.45f, GradeCalculator.redondear(3.456f, config), 0.001f)
        assertEquals(3.45f, GradeCalculator.redondear(3.459f, config), 0.001f)
        assertEquals(3.99f, GradeCalculator.redondear(3.999f, config), 0.001f)
    }

    @Test
    fun `redondear academico 1 decimal`() {
        val config = ConfiguracionNota(decimales = 1, modoRedondeo = ModoRedondeo.ACADEMICO)
        assertEquals(3.0f, GradeCalculator.redondear(2.95f, config), 0.01f)
        assertEquals(2.9f, GradeCalculator.redondear(2.94f, config), 0.01f)
    }

    @Test
    fun `redondear con 1 decimal`() {
        val config = ConfiguracionNota(decimales = 1, modoRedondeo = ModoRedondeo.MATEMATICO)
        assertEquals(3.5f, GradeCalculator.redondear(3.45f, config), 0.01f)
        assertEquals(3.4f, GradeCalculator.redondear(3.44f, config), 0.01f)
    }

    // ── Display ─────────────────────────────────────────────────

    @Test
    fun `display formatea correctamente`() {
        val config = ConfiguracionNota(decimales = 2)
        assertEquals("3.46", GradeCalculator.display(3.456f, config))
    }

    @Test
    fun `displayOrDash con null retorna --`() {
        assertEquals("--", GradeCalculator.displayOrDash(null))
    }

    @Test
    fun `displayOrDash con 0 retorna --`() {
        assertEquals("--", GradeCalculator.displayOrDash(0f))
    }

    @Test
    fun `displayOrDash con valor retorna formateado`() {
        assertNotEquals("--", GradeCalculator.displayOrDash(3.5f))
    }

    // ── Promedio ponderado ──────────────────────────────────────

    @Test
    fun `promedio ponderado simple`() {
        val pares = listOf(4.0f to 0.3f, 3.0f to 0.3f, 5.0f to 0.4f)
        val resultado = GradeCalculator.promedioPonderado(pares)
        assertNotNull(resultado)
        // (4*0.3 + 3*0.3 + 5*0.4) / (0.3+0.3+0.4) = (1.2+0.9+2.0)/1.0 = 4.1
        assertEquals(4.1f, resultado!!, 0.01f)
    }

    @Test
    fun `promedio ponderado lista vacia retorna null`() {
        assertNull(GradeCalculator.promedioPonderado(emptyList()))
    }

    @Test
    fun `promedio simple`() {
        val resultado = GradeCalculator.promedioSimple(listOf(4.0f, 3.0f, 5.0f))
        assertNotNull(resultado)
        assertEquals(4.0f, resultado!!, 0.01f)
    }

    @Test
    fun `promedio simple lista vacia retorna null`() {
        assertNull(GradeCalculator.promedioSimple(emptyList()))
    }

    // ── Nota necesaria ─────────────────────────────────────────

    @Test
    fun `nota necesaria calculo correcto`() {
        // Llevo 1.2 (de 30%), necesito 3.0 de nota final, queda 70%
        val resultado = GradeCalculator.notaNecesaria(
            aporteExistente = 1.2f,
            porcentajeRestante = 0.7f,
            meta = 3.0f,
            escalaMax = 5.0f
        )
        assertNotNull(resultado)
        // (3.0 - 1.2) / 0.7 = 2.571...
        assertEquals(2.57f, resultado!!, 0.01f)
    }

    @Test
    fun `nota necesaria sin porcentaje restante retorna null`() {
        assertNull(GradeCalculator.notaNecesaria(1.2f, 0f, 3.0f, 5.0f))
    }

    @Test
    fun `nota necesaria imposible retorna null`() {
        // Llevo 0, necesito 5.0 de nota final en solo 10% restante → 50.0 > escalaMax
        assertNull(GradeCalculator.notaNecesaria(0f, 0.1f, 5.0f, 5.0f))
    }

    // ── Simulación ──────────────────────────────────────────────

    @Test
    fun `simular nota final`() {
        val aportesExistentes = listOf(1.2f, 0.9f) // 30% cada uno
        val hipoteticas = listOf(4.5f to 0.4f) // 40% restante
        val resultado = GradeCalculator.simularNotaFinal(aportesExistentes, hipoteticas)
        // 1.2 + 0.9 + (4.5 * 0.4) = 1.2 + 0.9 + 1.8 = 3.9
        assertEquals(3.9f, resultado, 0.01f)
    }

    // ── Promedio ponderado por créditos ──────────────────────────

    @Test
    fun `promedio ponderado creditos`() {
        val datos = listOf(4.0f to 3, 3.0f to 4, 5.0f to 2)
        val resultado = GradeCalculator.promedioPonderadoCreditos(datos)
        assertNotNull(resultado)
        // (4*3 + 3*4 + 5*2) / (3+4+2) = (12+12+10) / 9 = 3.78
        assertEquals(3.78f, resultado!!, 0.01f)
    }

    @Test
    fun `promedio ponderado creditos sin creditos retorna null`() {
        val datos = listOf(4.0f to 0, 3.0f to 0)
        assertNull(GradeCalculator.promedioPonderadoCreditos(datos))
    }

    @Test
    fun `promedio ponderado creditos lista vacia retorna null`() {
        assertNull(GradeCalculator.promedioPonderadoCreditos(emptyList()))
    }
}
