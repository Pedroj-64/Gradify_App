package com.notasapp.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa una materia/asignatura del usuario.
 *
 * Cada materia pertenece a un [UsuarioEntity] y contiene su configuración
 * de escala de notas. Las notas se guardan en [ComponenteEntity] -> [SubNotaEntity].
 */
@Entity(
    tableName = "materias",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["googleId"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["usuarioId"])]
)
data class MateriaEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** ID del usuario propietario de esta materia. */
    val usuarioId: String,

    val nombre: String,

    /** Período académico, ej: "2026-1", "Semestre I 2026". */
    val periodo: String,

    /** Nombre del profesor. Opcional. */
    val profesor: String? = null,

    // ── Escala de calificación ──────────────────────────────────

    /** Valor mínimo de la escala, ej: 0.0. */
    val escalaMin: Float = 0f,

    /** Valor máximo de la escala, ej: 5.0. */
    val escalaMax: Float = 5f,

    /** Nota mínima para aprobar la materia, ej: 3.0. */
    val notaAprobacion: Float = 3f,

    /** Créditos académicos de la materia (para promedio ponderado). */
    val creditos: Int = 0,

    /**
     * Tipo de escala:
     * - "NUMERICO_5"   → 0-5 (tipo Colombia)
     * - "NUMERICO_10"  → 0-10
     * - "NUMERICO_100" → 0-100
     * - "LETRAS"       → A, B, C, D
     * - "PERSONALIZADO"→ el usuario define min/max
     */
    val tipoEscala: String = "NUMERICO_5",

    // ── Integración con Google Sheets ─────────────────────────

    /** ID del Spreadsheet en Google Drive. Null si no está sincronizado. */
    val googleSheetsId: String? = null,

    /** Timestamp (ms) de la última vez que se actualizó la materia. */
    val ultimaModificacionMs: Long = System.currentTimeMillis(),

    /** Timestamp (ms) de creación. */
    val creadoEnMs: Long = System.currentTimeMillis()
)
