package com.notasapp.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.notasapp.data.local.entities.ComponenteEntity
import com.notasapp.data.local.entities.MateriaEntity

/**
 * Relación Room: una [MateriaEntity] con todos sus [ComponenteEntity]
 * y las sub-notas anidadas dentro de cada componente.
 */
data class MateriaConComponentes(

    @Embedded
    val materia: MateriaEntity,

    @Relation(
        entity = ComponenteEntity::class,
        parentColumn = "id",
        entityColumn = "materiaId"
    )
    val componentesConSubNotas: List<ComponenteConSubNotas>
)
