package com.notasapp.domain.repository

import com.notasapp.domain.model.Componente
import com.notasapp.domain.model.Materia
import com.notasapp.domain.model.SubNota
import kotlinx.coroutines.flow.Flow

/**
 * Contrato del repositorio de materias.
 *
 * Define las operaciones de datos disponibles para la capa de dominio.
 * La implementación concreta ([MateriaRepositoryImpl]) vive en la capa de datos
 * y es provista por Hilt vía [RepositoryModule].
 */
interface MateriaRepository {

    // ── Materias ────────────────────────────────────────────────

    fun getMateriasByUsuario(usuarioId: String): Flow<List<Materia>>

    fun getMateriaConComponentes(materiaId: Long): Flow<Materia?>

    suspend fun insertMateria(materia: Materia): Long

    suspend fun updateMateria(materia: Materia)

    suspend fun deleteMateria(materiaId: Long)

    // ── Componentes ─────────────────────────────────────────────

    suspend fun insertComponentes(componentes: List<Componente>)

    suspend fun updateComponentes(componentes: List<Componente>)

    suspend fun deleteComponentesByMateria(materiaId: Long)

    // ── Sub-Notas ────────────────────────────────────────────────

    suspend fun insertSubNotas(subNotas: List<SubNota>)

    suspend fun updateSubNotaValor(subNotaId: Long, valor: Float?)

    suspend fun deleteSubNotasByComponente(componenteId: Long)

    // ── Sheets ID ────────────────────────────────────────────────

    suspend fun updateSheetsId(materiaId: Long, sheetsId: String?)
}
