package com.notasapp.ui.materia.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notasapp.domain.model.Materia
import com.notasapp.domain.usecase.CalcularNotaNecesariaUseCase
import com.notasapp.domain.usecase.GetMateriaConPromedioUseCase
import com.notasapp.domain.repository.MateriaRepository
import com.notasapp.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel de la pantalla de detalle de una materia.
 *
 * Observa la materia completa (con componentes y sub-notas) como Flow reactivo.
 * Gestiona la actualización de sub-notas directamente desde la pantalla.
 */
@HiltViewModel
class MateriaDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getMateriaConPromedio: GetMateriaConPromedioUseCase,
    private val calcularNotaNecesaria: CalcularNotaNecesariaUseCase,
    private val materiaRepository: MateriaRepository
) : ViewModel() {

    private val materiaId: Long = checkNotNull(
        savedStateHandle[Screen.MateriaDetail.ARG_MATERIA_ID]
    )

    // ── Estado de la materia ────────────────────────────────────

    /** La materia completa con componentes y sub-notas, actualizada en tiempo real. */
    val materia: StateFlow<Materia?> = getMateriaConPromedio(materiaId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    // ── Calculadora "¿qué nota necesito?" ──────────────────────

    private val _notaNecesariaResult =
        MutableStateFlow<CalcularNotaNecesariaUseCase.Resultado?>(null)
    val notaNecesariaResult: StateFlow<CalcularNotaNecesariaUseCase.Resultado?> =
        _notaNecesariaResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ── Acciones ───────────────────────────────────────────────

    /**
     * Actualiza el valor de una sub-nota específica.
     *
     * @param subNotaId  ID de la [SubNotaEntity] a actualizar.
     * @param valor      Nueva nota. Null para borrar la nota.
     */
    fun actualizarSubNota(subNotaId: Long, valor: Float?) {
        viewModelScope.launch {
            try {
                materiaRepository.updateSubNotaValor(subNotaId, valor)
            } catch (e: Exception) {
                Timber.e(e, "Error al actualizar sub-nota $subNotaId")
                _error.value = "No se pudo guardar la nota"
            }
        }
    }

    /**
     * Calcula la nota necesaria en los componentes faltantes
     * para alcanzar [meta] en esta materia.
     */
    fun calcularNotaNecesaria(meta: Float) {
        val materiaActual = materia.value ?: return
        _notaNecesariaResult.value = calcularNotaNecesaria(
            componentes = materiaActual.componentes,
            metaFinal = meta,
            escalaMax = materiaActual.escalaMax
        )
    }

    fun clearError() { _error.value = null }
    fun clearCalculadora() { _notaNecesariaResult.value = null }
}
