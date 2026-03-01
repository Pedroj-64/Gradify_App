package com.notasapp.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests unitarios para [ConversorLetras].
 */
class ConversorLetrasTest {

    @Test
    fun `nota alta es A`() {
        assertEquals("A", ConversorLetras.notaALetra(4.5f, 0f, 5f))
        assertEquals("A", ConversorLetras.notaALetra(95f, 0f, 100f))
    }

    @Test
    fun `nota media es C`() {
        assertEquals("C", ConversorLetras.notaALetra(3.5f, 0f, 5f))
        assertEquals("C", ConversorLetras.notaALetra(75f, 0f, 100f))
    }

    @Test
    fun `nota baja es F`() {
        assertEquals("F", ConversorLetras.notaALetra(1.0f, 0f, 5f))
        assertEquals("F", ConversorLetras.notaALetra(50f, 0f, 100f))
    }

    @Test
    fun `nota aprobacion es D`() {
        assertEquals("D", ConversorLetras.notaALetra(3.0f, 0f, 5f))
    }

    @Test
    fun `letra a puntos GPA`() {
        assertEquals(4.0f, ConversorLetras.letraAPuntos("A"), 0.01f)
        assertEquals(3.0f, ConversorLetras.letraAPuntos("B"), 0.01f)
        assertEquals(2.0f, ConversorLetras.letraAPuntos("C"), 0.01f)
        assertEquals(1.0f, ConversorLetras.letraAPuntos("D"), 0.01f)
        assertEquals(0.0f, ConversorLetras.letraAPuntos("F"), 0.01f)
    }

    @Test
    fun `letra a puntos case insensitive`() {
        assertEquals(4.0f, ConversorLetras.letraAPuntos("a"), 0.01f)
    }

    @Test
    fun `letra invalida retorna 0`() {
        assertEquals(0.0f, ConversorLetras.letraAPuntos("Z"), 0.01f)
    }

    @Test
    fun `letras disponibles`() {
        val letras = ConversorLetras.letrasDisponibles()
        assertEquals(5, letras.size)
        assertTrue(letras.contains("A"))
        assertTrue(letras.contains("F"))
    }

    @Test
    fun `display con letra`() {
        val display = ConversorLetras.displayConLetra(4.5f, 0f, 5f)
        assertTrue(display.contains("A"))
        assertTrue(display.contains("4.50"))
    }

    @Test
    fun `rango invalido retorna F`() {
        assertEquals("F", ConversorLetras.notaALetra(3f, 5f, 5f)) // rango = 0
    }
}
