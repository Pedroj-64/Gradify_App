package com.notasapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
     * Inserta o reemplaza al usuario. Se usa al hacer login exitoso
     * para actualizar nombre/foto si cambiaron en Google.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
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
