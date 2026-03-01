package com.notasapp.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa un componente/corte de evaluación de una materia.
 *
 * Ejemplos: "Primer corte (30%)", "Examen final (40%)".
 * Cada componente puede tener múltiples [SubNotaEntity].
 */
@Entity(
    tableName = "componentes",
    foreignKeys = [
        ForeignKey(
            entity = MateriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["materiaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["materiaId"])]
)
data class ComponenteEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** ID de la materia a la que pertenece este componente. */
    val materiaId: Long,

    /** Nombre descriptivo, ej: "Primer corte", "Examen final". */
    val nombre: String,

    /**
     * Peso de este componente en la nota final (0.0 a 1.0).
     * Ejemplo: 0.30 = 30% de la nota final.
     * La suma de todos los componentes de una materia debe ser 1.0.
     */
    val porcentaje: Float,

    /**
     * Posición en la lista (0-indexed).
     * Se usa para mantener el orden cuando el usuario hace drag & drop.
     */
    val orden: Int = 0,

    /**
     * Timestamp epoch (ms) de la fecha límite de este componente.
     * Null si no se ha definido fecha. Usado por ReminderWorker para
     * enviar recordatorios cuando faltan ≤ 7 días.
     */
    @ColumnInfo(defaultValue = "NULL")
    val fechaLimite: Long? = null
)
