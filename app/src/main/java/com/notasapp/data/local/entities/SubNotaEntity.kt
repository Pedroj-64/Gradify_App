package com.notasapp.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa una nota individual dentro de un [ComponenteEntity].
 *
 * Ejemplos: "Taller 1 (20% del corte) → nota: 4.0",
 *           "Parcial (80% del corte) → nota: 3.3".
 *
 * Si [valor] es null significa que la nota aún no ha sido ingresada.
 */
@Entity(
    tableName = "sub_notas",
    foreignKeys = [
        ForeignKey(
            entity = ComponenteEntity::class,
            parentColumns = ["id"],
            childColumns = ["componenteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["componenteId"])]
)
data class SubNotaEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** ID del componente al que pertenece esta sub-nota. */
    val componenteId: Long,

    /** Descripción, ej: "Taller 1", "Parcial escrito". */
    val descripcion: String,

    /**
     * Peso de esta sub-nota dentro del componente (0.0 a 1.0).
     * Ejemplo: 0.20 = representa el 20% del componente.
     * La suma de todas las sub-notas de un componente debe ser 1.0.
     */
    val porcentajeDelComponente: Float,

    /**
     * Valor numérico de la nota.
     * Null si todavía no se ha ingresado.
     */
    val valor: Float? = null,

    /** Timestamp (ms) de la última modificación. */
    val ultimaModificacionMs: Long = System.currentTimeMillis()
)
