package com.notasapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notasapp.data.local.dao.UsuarioDao
import com.notasapp.domain.model.Materia
import com.notasapp.domain.repository.MateriaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel de la pantalla Home (lista de materias).
 *
 * Observa al usuario activo y carga sus materias como Flow reactivo.
 * La UI se actualiza automáticamente al agregar, editar o eliminar materias.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val usuarioDao: UsuarioDao,
    private val materiaRepository: MateriaRepository
) : ViewModel() {

    // ── Estado de UI ───────────────────────────────────────────

    /** Lista de materias del usuario activo. */
    val materias: StateFlow<List<Materia>> = usuarioDao
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ── Acciones ───────────────────────────────────────────────

    /**
     * Elimina una materia de Room.
     * La lista de materias se actualiza automáticamente vía Flow.
     */
    fun deleteMateria(materiaId: Long) {
        viewModelScope.launch {
            try {
                materiaRepository.deleteMateria(materiaId)
                Timber.i("Materia $materiaId eliminada")
            } catch (e: Exception) {
                Timber.e(e, "Error al eliminar materia")
                _error.value = "No se pudo eliminar la materia"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
