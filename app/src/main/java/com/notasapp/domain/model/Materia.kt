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
    val creditos: Int = 0,
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
     * Puntos acumulados hasta ahora hacia la nota final.
     * A diferencia de [promedio], solo suma lo que ya fue evaluado (sin proyectar).
     */
    val acumulado: Float
        get() = componentes
            .mapNotNull { it.aporteAlFinal }
            .sumOf { it.toDouble() }
            .toFloat()

    /**
     * Porcentaje del curso que ya fue evaluado (0.0 – 1.0).
     * Considera un componente "evaluado" si tiene al menos una sub-nota ingresada.
     */
    val porcentajeEvaluado: Float
        get() = componentes
            .filter { it.promedio != null }
            .sumOf { it.porcentaje.toDouble() }
            .toFloat()

    /**
     * Acumulado redondeado a 2 decimales, para la UI.
     */
    val acumuladoDisplay: String
        get() = if (acumulado > 0f) "%.2f".format(acumulado) else "--"

    /**
     * True si los puntos acumulados **ya** superan la nota mínima de
     * aprobación, independientemente de cuánto falta por evaluar.
     * Sirve para felicitar al estudiante.
     */
    val yaAprobo: Boolean
        get() = acumulado >= notaAprobacion

    /**
     * Nota que el estudiante necesitaría promediar en lo restante
     * para alcanzar justo la nota de aprobación.  Null si ya aprobó
     * o no queda porcentaje por evaluar.
     */
    val notaNecesariaParaAprobar: Float?
        get() {
            if (yaAprobo) return null
            val restante = 1f - porcentajeEvaluado
            if (restante <= 0f) return null
            val necesita = (notaAprobacion - acumulado) / restante
            return if (necesita in 0f..escalaMax) necesita else null
        }

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

    /**
     * True si todos los componentes están completos (todas las sub-notas ingresadas).
     */
    val completa: Boolean
        get() = componentes.isNotEmpty() && componentes.all { it.completo }
}
