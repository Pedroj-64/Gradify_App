package com.notasapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notasapp.data.local.dao.UsuarioDao
import com.notasapp.domain.model.Materia
import com.notasapp.domain.repository.MateriaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Orden de listado para materias en Home.
 */
enum class OrdenMateria(val label: String) {
    NOMBRE("Nombre"),
    PROMEDIO_ASC("Promedio ↑"),
    PROMEDIO_DESC("Promedio ↓"),
    PERIODO("Semestre")
}

/**
 * ViewModel de la pantalla Home (lista de materias).
 *
 * Observa al usuario activo y carga sus materias como Flow reactivo.
 * Soporta búsqueda, filtro por semestre, ordenamiento y métricas de riesgo.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val usuarioDao: UsuarioDao,
    private val materiaRepository: MateriaRepository
) : ViewModel() {

    // ── Filtros y búsqueda ─────────────────────────────────────

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filtroSemestre = MutableStateFlow<String?>(null)
    val filtroSemestre: StateFlow<String?> = _filtroSemestre.asStateFlow()

    private val _orden = MutableStateFlow(OrdenMateria.NOMBRE)
    private val _hiddenIds = MutableStateFlow<Set<Long>>(emptySet())
    val orden: StateFlow<OrdenMateria> = _orden.asStateFlow()

    // ── Datos base ─────────────────────────────────────────────

    /** Todas las materias del usuario, también las de semestres cerrados. */
    private val todas: StateFlow<List<Materia>> = usuarioDao
        .getUsuarioActivo()
        .flatMapLatest { usuario ->
            if (usuario == null) flowOf(emptyList())
            else materiaRepository.getMateriasByUsuario(usuario.googleId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _verArchivadas = MutableStateFlow(false)
    /** true = Inicio muestra los semestres cerrados en lugar de los activos. */
    val verArchivadas: StateFlow<Boolean> = _verArchivadas.asStateFlow()

    /** Hay algún semestre cerrado (para mostrar el chip "Archivados"). */
    val hayArchivadas: StateFlow<Boolean> = todas
        .map { lista -> lista.any { it.archivada } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Semestres activos: base de las métricas del resumen (los cerrados no cuentan como "en riesgo"). */
    private val allMaterias: StateFlow<List<Materia>> = todas
        .map { lista -> lista.filter { !it.archivada } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Lo que se está viendo: activos o cerrados. */
    private val visibles: StateFlow<List<Materia>> = combine(todas, _verArchivadas) { lista, archivadas ->
        lista.filter { it.archivada == archivadas }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Materias filtradas, buscadas y ordenadas. */
    val materias: StateFlow<List<Materia>> = combine(
        visibles, _searchQuery, _filtroSemestre, _orden, _hiddenIds
    ) { all, query, semestre, sort, hidden ->
        var result = all.filter { it.id !in hidden }

        // Filtro por semestre
        if (!semestre.isNullOrBlank()) {
            result = result.filter { it.periodo == semestre }
        }

        // Búsqueda por nombre/profesor
        if (query.isNotBlank()) {
            val q = query.lowercase()
            result = result.filter {
                it.nombre.lowercase().contains(q) ||
                        (it.profesor?.lowercase()?.contains(q) == true)
            }
        }

        // Ordenamiento
        when (sort) {
            OrdenMateria.NOMBRE -> result.sortedBy { it.nombre.lowercase() }
            OrdenMateria.PROMEDIO_ASC -> result.sortedBy { it.promedio ?: Float.MAX_VALUE }
            OrdenMateria.PROMEDIO_DESC -> result.sortedByDescending { it.promedio ?: -1f }
            OrdenMateria.PERIODO -> result.sortedByDescending { it.periodo }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** Semestres disponibles para el filtro. */
    val semestresDisponibles: StateFlow<List<String>> = visibles
        .map { materias -> materias.map { it.periodo }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Materias en riesgo académico (nota proyectada < mínima). */
    val materiasEnRiesgo: StateFlow<List<Materia>> = allMaterias
        .map { materias -> materias.filter { m -> m.promedio != null && !m.aprobado } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Promedio general ponderado rápido para el dashboard. */
    val promedioGeneral: StateFlow<Float?> = allMaterias
        .map { materias ->
            val conNotas = materias.filter { it.promedio != null }
            if (conNotas.isEmpty()) null
            else {
                val totalCred = conNotas.sumOf { it.creditos }
                if (totalCred > 0)
                    conNotas.sumOf { (it.promedio!! * it.creditos).toDouble() }.toFloat() / totalCred
                else
                    conNotas.map { it.promedio!! }.average().toFloat()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ── Semestres cerrados ─────────────────────────────────────

    fun toggleArchivadas() {
        _verArchivadas.update { !it }
        _filtroSemestre.value = null
    }

    /** Cierra (archivado = true) o reabre un semestre completo. */
    fun setSemestreArchivado(periodo: String, archivado: Boolean) {
        viewModelScope.launch {
            val usuario = usuarioDao.getUsuarioActivoOnce() ?: return@launch
            try {
                materiaRepository.setPeriodoArchivado(usuario.googleId, periodo, archivado)
                _filtroSemestre.value = null
                if (!archivado) _verArchivadas.value = false
            } catch (e: Exception) {
                Timber.e(e, "Error al cambiar el estado del semestre")
                _error.value = "No se pudo actualizar el semestre"
            }
        }
    }

    // ── Acciones de filtro ─────────────────────────────────────

    fun updateSearchQuery(query: String) = _searchQuery.update { query }
    fun updateFiltroSemestre(semestre: String?) = _filtroSemestre.update { semestre }
    fun updateOrden(orden: OrdenMateria) = _orden.update { orden }

    // ── Borrado con "Deshacer" ─────────────────────────────────
    // La materia se oculta al instante y se borra de verdad tras UNDO_WINDOW_MS,
    // salvo que el usuario pulse Deshacer.
    private val deleteJobs = mutableMapOf<Long, Job>()

    fun softDelete(materiaId: Long) {
        _hiddenIds.update { it + materiaId }
        deleteJobs[materiaId] = viewModelScope.launch {
            delay(UNDO_WINDOW_MS)
            try {
                materiaRepository.deleteMateria(materiaId)
                Timber.i("Materia $materiaId eliminada")
            } catch (e: Exception) {
                Timber.e(e, "Error al eliminar materia")
                _error.value = "No se pudo eliminar la materia"
            } finally {
                _hiddenIds.update { it - materiaId }
                deleteJobs.remove(materiaId)
            }
        }
    }

    fun undoDelete(materiaId: Long) {
        deleteJobs.remove(materiaId)?.cancel()
        _hiddenIds.update { it - materiaId }
    }

    fun clearError() {
        _error.value = null
    }

    private companion object {
        const val UNDO_WINDOW_MS = 5_000L
    }
}
