package com.notasapp.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room para un detalle (sub-nivel) de una sub-nota.
 *
 * Permite que una [SubNotaEntity] sea "compuesta": en lugar de tener un valor
 * directo, contiene múltiples detalles con sus propios pesos y valores.
 *
 * La tabla usa ON DELETE CASCADE para borrar los detalles automáticamente
 * cuando se elimina la sub-nota padre.
 */
@Entity(
    tableName = "sub_nota_details",
    foreignKeys = [
        ForeignKey(
            entity = SubNotaEntity::class,
            parentColumns = ["id"],
            childColumns = ["subNotaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subNotaId")]
)
data class SubNotaDetailEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** ID de la sub-nota padre (FK → sub_notas.id). */
    val subNotaId: Long,

    /** Nombre del detalle, ej: "Primer intento". */
    val descripcion: String,

    /** Peso dentro de la sub-nota (0.0 a 1.0). */
    val porcentaje: Float,

    /** Nota ingresada. Null si aún no se ha registrado. */
    val valor: Float? = null
)
