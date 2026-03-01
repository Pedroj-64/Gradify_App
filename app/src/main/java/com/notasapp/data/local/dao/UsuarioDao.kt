package com.notasapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.notasapp.data.local.entities.UsuarioEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones CRUD del usuario autenticado.
 *
 * Se espera que solo haya un usuario activo a la vez en la app.
 */
@Dao
interface UsuarioDao {

    /**
     * Inserta o actualiza el usuario sin borrarlo.
     * @Upsert usa INSERT ... ON CONFLICT DO UPDATE, evitando el
     * DELETE+INSERT de OnConflictStrategy.REPLACE que dispararía
     * el ON DELETE CASCADE y eliminaría todas las materias.
     */
    @Upsert
    suspend fun insertOrUpdate(usuario: UsuarioEntity)

    @Update
    suspend fun update(usuario: UsuarioEntity)

    @Delete
    suspend fun delete(usuario: UsuarioEntity)

    /** Retorna el usuario activo como Flow (reactivo). */
    @Query("SELECT * FROM usuarios LIMIT 1")
    fun getUsuarioActivo(): Flow<UsuarioEntity?>

    /** Retorna el usuario activo una sola vez (para operaciones de escritura). */
    @Query("SELECT * FROM usuarios LIMIT 1")
    suspend fun getUsuarioActivoOnce(): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE googleId = :googleId")
    suspend fun getUsuarioById(googleId: String): UsuarioEntity?

    /** Elimina todos los usuarios (para logout). */
    @Query("DELETE FROM usuarios")
    suspend fun deleteAll()

    /** Actualiza el timestamp de la última sincronización. */
    @Query("UPDATE usuarios SET ultimaSyncMs = :timestampMs WHERE googleId = :googleId")
    suspend fun updateUltimaSync(googleId: String, timestampMs: Long)
}
