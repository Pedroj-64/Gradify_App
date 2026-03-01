package com.notasapp.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.notasapp.data.local.entities.SubNotaDetailEntity
import com.notasapp.data.local.entities.SubNotaEntity

/**
 * Relación Room: una [SubNotaEntity] con todos sus [SubNotaDetailEntity].
 *
 * Cuando [detalles] es no vacío, la sub-nota es "compuesta" y su valor
 * efectivo se calcula como la suma ponderada de los detalles.
 *
 * Room genera la JOIN automáticamente; solo se usa en @Transaction queries.
 */
data class SubNotaConDetalles(

    @Embedded
    val subNota: SubNotaEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "subNotaId"
    )
    val detalles: List<SubNotaDetailEntity>
)
