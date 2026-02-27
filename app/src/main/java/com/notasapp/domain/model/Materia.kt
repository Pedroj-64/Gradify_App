package com.notasapp.domain.model

/**
 * Modelo de dominio de una materia/asignatura.
 *
 * Agrega todos los componentes con sus sub-notas y provee cálculos de promedio.
 *
 * @property id              ID en Room.
 * @property usuarioId       ID del usuario propietario.
 * @property nombre          Nombre de la materia, ej: "Matemáticas".
 * @property periodo         Período académico, ej: "2026-1".
 * @property profesor        Nombre del profesor (opcional).
 * @property escalaMin       Valor mínimo de la escala de notas.
 * @property escalaMax       Valor máximo de la escala de notas.
 * @property notaAprobacion  Nota mínima para aprobar.
 * @property tipoEscala      Tipo de escala de la materia.
 * @property googleSheetsId  ID de la hoja de Sheets vinculada (null si no sincronizado).
 * @property componentes     Componentes de evaluación con sus sub-notas.
 */
data class Materia(
    val id: Long = 0,
    val usuarioId: String,
    val nombre: String,
    val periodo: String,
    val profesor: String? = null,
    val escalaMin: Float = 0f,
    val escalaMax: Float = 5f,
    val notaAprobacion: Float = 3f,
    val tipoEscala: TipoEscala = TipoEscala.NUMERICO_5,
    val googleSheetsId: String? = null,
    val componentes: List<Componente> = emptyList()
) {
    /**
     * Promedio ponderado actual de la materia.
     *
     * Suma los aportes de los componentes que ya tienen nota.
     * Retorna null si ningún componente tiene notas ingresadas.
     */
    val promedio: Float?
        get() {
            val conNota = componentes.filter { it.aporteAlFinal != null }
            if (conNota.isEmpty()) return null
            return conNota.sumOf { it.aporteAlFinal!!.toDouble() }.toFloat()
        }

    /**
     * Promedio redondeado a 2 decimales, para mostrar en la UI.
     */
    val promedioDisplay: String
        get() = promedio?.let { "%.2f".format(it) } ?: "--"

    /**
     * True si el promedio actual supera la nota mínima de aprobación.
     */
    val aprobado: Boolean get() = (promedio ?: 0f) >= notaAprobacion

    /**
     * True si la materia tiene vinculación con Google Sheets.
     */
    val sincronizadaConSheets: Boolean get() = googleSheetsId != null

    /**
     * Suma total de los porcentajes de los componentes.
     * Debería ser siempre 1.0 (100%).
     */
    val sumaPorcentajes: Float
        get() = componentes.sumOf { it.porcentaje.toDouble() }.toFloat()
}
