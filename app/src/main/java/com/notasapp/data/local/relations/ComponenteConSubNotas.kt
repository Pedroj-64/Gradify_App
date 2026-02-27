package com.notasapp.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.notasapp.data.local.entities.ComponenteEntity
import com.notasapp.data.local.entities.SubNotaEntity

/**
 * Relación Room: un [ComponenteEntity] con todas sus [SubNotaEntity].
 *
 * Room genera la JOIN automáticamente; solo se usa en @Transaction queries.
 */
data class ComponenteConSubNotas(

    @Embedded
    val componente: ComponenteEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "componenteId"
    )
    val subNotas: List<SubNotaEntity>
)
