package com.notasapp.domain.model

/**
 * Modelo de dominio de un componente de evaluación.
 *
 * Agrega las sub-notas y provee propiedades calculadas.
 *
 * @property id        ID en Room.
 * @property materiaId ID de la materia padre.
 * @property nombre    Nombre del corte, ej: "Primer corte".
 * @property porcentaje Peso en la nota final (0.0 a 1.0), ej: 0.30 = 30%.
 * @property orden     Posición en la lista (para drag & drop).
 * @property subNotas  Lista de sub-notas que componen este corte.
 */
data class Componente(
    val id: Long = 0,
    val materiaId: Long,
    val nombre: String,
    val porcentaje: Float,
    val orden: Int = 0,
    val subNotas: List<SubNota> = emptyList()
) {
    /**
     * Promedio ponderado de las sub-notas **ingresadas** de este componente.
     *
     * Calcula: Σ(subNota.valor * subNota.porcentajeDelComponente) sobre las ingresadas.
     * Si ninguna sub-nota tiene valor, retorna null.
     */
    val promedio: Float?
        get() {
            val ingresadas = subNotas.filter { it.ingresada }
            if (ingresadas.isEmpty()) return null
            return ingresadas.sumOf {
                (it.valor!! * it.porcentajeDelComponente).toDouble()
            }.toFloat()
        }

    /**
     * Aporte ponderado de este componente a la nota final de la materia.
     * Null si el componente no tiene ninguna sub-nota ingresada.
     */
    val aporteAlFinal: Float? get() = promedio?.times(porcentaje)

    /**
     * True si todas las sub-notas del componente fueron ingresadas.
     */
    val completo: Boolean get() = subNotas.isNotEmpty() && subNotas.all { it.ingresada }

    /**
     * Porcentaje (0–100) del componente expresado en entero, para la UI.
     */
    val porcentajeDisplay: Int get() = (porcentaje * 100).toInt()
}
