package com.notasapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.notasapp.data.local.entities.ComponenteEntity
import com.notasapp.data.local.relations.ComponenteConSubNotas
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones CRUD de componentes/cortes de evaluación.
 */
@Dao
interface ComponenteDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(componente: ComponenteEntity): Long

    /** Inserta una lista completa de componentes (usado en el Wizard). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(componentes: List<ComponenteEntity>)

    @Update
    suspend fun update(componente: ComponenteEntity)

    /** Actualiza varios componentes a la vez (para guardar nuevos porcentajes). */
    @Update
    suspend fun updateAll(componentes: List<ComponenteEntity>)

    @Delete
    suspend fun delete(componente: ComponenteEntity)

    @Query("SELECT * FROM componentes WHERE materiaId = :materiaId ORDER BY orden ASC")
    fun getComponentesByMateria(materiaId: Long): Flow<List<ComponenteEntity>>

    @Query("SELECT * FROM componentes WHERE materiaId = :materiaId ORDER BY orden ASC")
    suspend fun getComponentesByMateriaOnce(materiaId: Long): List<ComponenteEntity>

    @Query("SELECT * FROM componentes WHERE id = :id")
    suspend fun getComponenteById(id: Long): ComponenteEntity?

    /**
     * Retorna cada componente junto con sus sub-notas.
     * Se usa en el cálculo de promedio y en la vista detalle.
     */
    @Transaction
    @Query("SELECT * FROM componentes WHERE materiaId = :materiaId ORDER BY orden ASC")
    fun getComponentesConSubNotas(materiaId: Long): Flow<List<ComponenteConSubNotas>>

    @Transaction
    @Query("SELECT * FROM componentes WHERE materiaId = :materiaId ORDER BY orden ASC")
    suspend fun getComponentesConSubNotasOnce(materiaId: Long): List<ComponenteConSubNotas>

    /** Elimina todos los componentes de una materia. */
    @Query("DELETE FROM componentes WHERE materiaId = :materiaId")
    suspend fun deleteByMateria(materiaId: Long)

    /**
     * Actualiza el orden de un componente específico.
     * Se llama después de un drag & drop.
     */
    @Query("UPDATE componentes SET orden = :nuevoOrden WHERE id = :id")
    suspend fun updateOrden(id: Long, nuevoOrden: Int)
}
