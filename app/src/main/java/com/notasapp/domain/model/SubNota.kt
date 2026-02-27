package com.notasapp.domain.model

/**
 * Modelo de dominio de una sub-nota (nota individual dentro de un componente).
 *
 * Es la versión "limpia" de [SubNotaEntity]; no depende de Room ni de Android.
 * Usada en la lógica de negocio y en los ViewModels.
 *
 * @property id                       ID en Room. 0 si aún no ha sido persistida.
 * @property componenteId             ID del componente padre.
 * @property descripcion              Nombre de la actividad, ej: "Taller 1".
 * @property porcentajeDelComponente  Peso dentro del componente (0.0 a 1.0).
 * @property valor                    Nota ingresada. Null si no se ha registrado.
 */
data class SubNota(
    val id: Long = 0,
    val componenteId: Long,
    val descripcion: String,
    val porcentajeDelComponente: Float,
    val valor: Float? = null
) {
    /**
     * Indica si la nota ya fue ingresada.
     */
    val ingresada: Boolean get() = valor != null

    /**
     * Aporte de esta sub-nota al promedio del componente.
     * Null si aún no tiene valor.
     */
    val aporteAlComponente: Float? get() = valor?.let { it * porcentajeDelComponente }
}
