package com.notasapp.domain.model

/**
 * Modelo de dominio de una sub-nota (nota individual dentro de un componente).
 *
 * Es la versión "limpia" de [SubNotaEntity]; no depende de Room ni de Android.
 * Usada en la lógica de negocio y en los ViewModels.
 *
 * Una sub-nota puede ser **simple** (tiene un [valor] directo) o **compuesta**
 * (tiene una lista [detalles] con sus propios pesos; el valor se calcula
 * como suma ponderada de los detalles).
 *
 * @property id                       ID en Room. 0 si aún no ha sido persistida.
 * @property componenteId             ID del componente padre.
 * @property descripcion              Nombre de la actividad, ej: "Taller 1".
 * @property porcentajeDelComponente  Peso dentro del componente (0.0 a 1.0).
 * @property valor                    Nota directa. Null si no se ha registrado (solo para sub-notas simples).
 * @property detalles                 Detalles internos. Vacío = sub-nota simple.
 */
data class SubNota(
    val id: Long = 0,
    val componenteId: Long,
    val descripcion: String,
    val porcentajeDelComponente: Float,
    val valor: Float? = null,
    val detalles: List<SubNotaDetalle> = emptyList()
) {
    /** True si la sub-nota tiene detalles internos. */
    val esCompuesta: Boolean get() = detalles.isNotEmpty()

    /**
     * Valor efectivo de la sub-nota:
     * - Si es compuesta: suma ponderada de los detalles ingresados (null si alguno falta).
     * - Si es simple: el [valor] directo.
     */
    val valorEfectivo: Float?
        get() = when {
            esCompuesta -> {
                if (detalles.all { it.valor != null }) {
                    val totalPct = detalles.sumOf { it.porcentaje.toDouble() }
                    if (totalPct > 0)
                        detalles.sumOf { ((it.valor!! * it.porcentaje) / totalPct).toDouble() }.toFloat()
                    else null
                } else null
            }
            else -> valor
        }

    /**
     * Indica si la nota ya fue ingresada (ya sea directa o todos los detalles completos).
     */
    val ingresada: Boolean get() = valorEfectivo != null

    /**
     * Aporte de esta sub-nota al promedio del componente.
     * Null si aún no tiene valor efectivo.
     */
    val aporteAlComponente: Float? get() = valorEfectivo?.let { it * porcentajeDelComponente }
}
