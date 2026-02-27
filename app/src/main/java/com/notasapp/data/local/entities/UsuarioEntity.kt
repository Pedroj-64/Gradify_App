package com.notasapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa al usuario autenticado con Google.
 *
 * Un solo usuario puede tener muchas [MateriaEntity]. Su googleId
 * actúa como clave foránea en las materias.
 */
@Entity(tableName = "usuarios")
data class UsuarioEntity(

    /** ID único de Google (sub del JWT). Nunca cambia. */
    @PrimaryKey
    val googleId: String,

    val nombre: String,

    val email: String,

    /** URL del avatar de Google. Puede ser null si no hay foto. */
    val fotoUrl: String? = null,

    /** Timestamp unix (ms) de la última sincronización con Sheets. */
    val ultimaSyncMs: Long = 0L
)
