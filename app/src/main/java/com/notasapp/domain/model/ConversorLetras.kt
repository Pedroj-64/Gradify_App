package com.notasapp.domain.model

/**
 * Conversor entre notas numéricas y letras (A–F).
 *
 * Escala estándar GPA 4.0:
 *  A  = 4.0 (90–100% de la escala)
 *  B  = 3.0 (80–89%)
 *  C  = 2.0 (70–79%)
 *  D  = 1.0 (60–69%)
 *  F  = 0.0 (< 60%)
 *
 * Se adapta a cualquier escala numérica usando porcentajes del rango.
 */
object ConversorLetras {

    data class LetraGrade(
        val letra: String,
        val puntos: Float,
        val rangoMin: Float,  // % mínimo del rango (inclusive)
        val rangoMax: Float   // % máximo del rango (exclusive, excepto A)
    )

    private val ESCALA = listOf(
        LetraGrade("A", 4.0f, 0.90f, 1.01f),
        LetraGrade("B", 3.0f, 0.80f, 0.90f),
        LetraGrade("C", 2.0f, 0.70f, 0.80f),
        LetraGrade("D", 1.0f, 0.60f, 0.70f),
        LetraGrade("F", 0.0f, 0.00f, 0.60f)
    )

    /**
     * Convierte una nota numérica a su letra equivalente.
     *
     * @param nota       Valor numérico de la nota.
     * @param escalaMin  Mínimo de la escala (ej: 0).
     * @param escalaMax  Máximo de la escala (ej: 5).
     * @return           Letra correspondiente ("A"–"F").
     */
    fun notaALetra(nota: Float, escalaMin: Float, escalaMax: Float): String {
        val rango = escalaMax - escalaMin
        if (rango <= 0f) return "F"
        val porcentaje = ((nota - escalaMin) / rango).coerceIn(0f, 1f)
        return ESCALA.firstOrNull { porcentaje >= it.rangoMin } ?.letra ?: "F"
    }

    /**
     * Convierte una letra a su valor GPA (puntos en escala 4.0).
     */
    fun letraAPuntos(letra: String): Float =
        ESCALA.firstOrNull { it.letra == letra.uppercase() }?.puntos ?: 0f

    /**
     * Convierte una letra a su nota numérica equivalente en la escala dada.
     * Retorna el punto medio del rango de esa letra.
     */
    fun letraANota(letra: String, escalaMin: Float, escalaMax: Float): Float {
        val grade = ESCALA.firstOrNull { it.letra == letra.uppercase() } ?: return escalaMin
        val rango = escalaMax - escalaMin
        val midPct = (grade.rangoMin + grade.rangoMax.coerceAtMost(1f)) / 2f
        return escalaMin + (rango * midPct)
    }

    /**
     * Display de la nota con su letra equivalente.
     * Ej: "4.20 (B)"
     */
    fun displayConLetra(nota: Float, escalaMin: Float, escalaMax: Float): String {
        val letra = notaALetra(nota, escalaMin, escalaMax)
        return "${"%.2f".format(nota)} ($letra)"
    }

    /**
     * Lista de todas las letras disponibles (para selectores en la UI).
     */
    fun letrasDisponibles(): List<String> = ESCALA.map { it.letra }
}
