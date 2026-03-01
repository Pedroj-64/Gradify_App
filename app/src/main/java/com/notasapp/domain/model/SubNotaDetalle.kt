package com.notasapp.domain.model

/**
 * Modelo de dominio de un detalle (sub-nivel) de una sub-nota.
 *
 * Permite modelar notas compuestas: una sub-nota puede contener múltiples detalles
 * con sus propios pesos y valores, y el valor efectivo de la sub-nota se calcula
 * como la suma ponderada de sus detalles.
 *
 * @property id          ID en Room. 0 si aún no ha sido persistido.
 * @property subNotaId   ID de la sub-nota padre.
 * @property descripcion Nombre de la actividad, ej: "Primer intento".
 * @property porcentaje  Peso dentro de la sub-nota (0.0 a 1.0).
 * @property valor       Nota ingresada. Null si aún no se ha registrado.
 */
data class SubNotaDetalle(
    val id: Long = 0,
    val subNotaId: Long,
    val descripcion: String,
    val porcentaje: Float,
    val valor: Float? = null
) {
    val ingresada: Boolean get() = valor != null

    /** Aporte de este detalle al valor de la sub-nota padre. */
    val aporteAlSubNota: Float? get() = valor?.let { it * porcentaje }
}
