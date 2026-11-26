package com.notasapp.ui.materia.create

import kotlinx.coroutines.flow.first
import com.notasapp.domain.util.GradeCalculator
import com.notasapp.domain.model.ScaleTemplate
import androidx.lifecycle.SavedStateHandle
import com.notasapp.ui.savedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.channels.Channel
import com.notasapp.ui.UiEvent
import com.notasapp.ui.UiText
import com.notasapp.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notasapp.data.local.dao.UsuarioDao
import com.notasapp.domain.model.Componente
import com.notasapp.domain.model.Materia
import com.notasapp.domain.model.SubNota
import com.notasapp.domain.model.TipoEscala
import com.notasapp.domain.repository.MateriaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel del Wizard de 3 pasos para crear una nueva materia.
 *
 * Mantiene el estado acumulado entre pasos sin navegar a nuevas pantallas:
 * el Wizard se implementa con un único Composable y un estado de paso.
 */
@HiltViewModel
class CreateMateriaViewModel @Inject constructor(
    private val usuarioDao: UsuarioDao,
    private val materiaRepository: MateriaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // ── Estado del Wizard ──────────────────────────────────────

    private val _wizardState = savedFlow(savedStateHandle, "wizard", WizardState())
    val wizardState: StateFlow<WizardState> = _wizardState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // ── Crear varias seguidas (típico al empezar semestre) ─────

    /** Materias ya creadas: para "Copiar estructura de…" en el paso 3. */
    private val _otras = MutableStateFlow<List<Materia>>(emptyList())
    val otrasMaterias: StateFlow<List<Materia>> = _otras.asStateFlow()

    init {
        viewModelScope.launch {
            val usuario = usuarioDao.getUsuarioActivoOnce() ?: return@launch
            val todas = materiaRepository.getMateriasByUsuario(usuario.googleId).first()
            _otras.value = todas.filter { it.componentes.isNotEmpty() }.sortedByDescending { it.id }
            // Recordar periodo y escala de la última materia, salvo que el usuario ya haya escrito algo.
            val ultima = todas.maxByOrNull { it.id } ?: return@launch
            _wizardState.update {
                if (it.nombre.isNotEmpty() || it.periodo.isNotEmpty()) it
                else it.conBaseDe(ultima)
            }
        }
    }

    /** Copia nombres y pesos de los componentes de [materia] (sin notas). */
    fun copiarEstructura(materia: Materia) = _wizardState.update { state ->
        state.copy(componentes = materia.componentes.sortedBy { it.orden }.map { ComponenteInput(it.nombre, it.porcentaje) })
    }

    // ── Paso 1: Información básica ─────────────────────────────

    fun updateNombre(nombre: String) = _wizardState.update { it.copy(nombre = nombre) }
    fun updatePeriodo(periodo: String) = _wizardState.update { it.copy(periodo = periodo) }
    fun updateProfesor(profesor: String) = _wizardState.update { it.copy(profesor = profesor) }
    fun updateCreditos(creditos: Int) = _wizardState.update { it.copy(creditos = creditos) }
    fun goToStep2() = _wizardState.update { it.copy(currentStep = 2) }

    // ── Paso 2: Escala de calificación ─────────────────────────

    fun updateTipoEscala(tipo: TipoEscala) = _wizardState.update {
        it.copy(
            tipoEscala = tipo,
            escalaMin = tipo.escalaMin,
            escalaMax = tipo.escalaMax,
            notaAprobacion = tipo.notaAprobacion
        )
    }
    fun applyTemplate(t: ScaleTemplate) = _wizardState.update {
        it.copy(tipoEscala = t.tipo, escalaMin = t.min, escalaMax = t.max, notaAprobacion = t.aprobacion)
    }
    fun updateEscalaMin(min: Float) = _wizardState.update { it.copy(escalaMin = min) }
    fun updateEscalaMax(max: Float) = _wizardState.update { it.copy(escalaMax = max) }
    fun updateNotaAprobacion(nota: Float) = _wizardState.update { it.copy(notaAprobacion = nota) }
    fun goToStep3() = _wizardState.update { it.copy(currentStep = 3) }

    // ── Paso 3: Componentes ────────────────────────────────────

    /** [nombre] llega traducido desde la UI ("Componente 4" / "Component 4"). */
    fun addComponente(nombre: String) = _wizardState.update { state ->
        state.copy(componentes = state.componentes + ComponenteInput(nombre = nombre, porcentaje = 0f))
    }

    /** Estructura típica 30/30/40 con nombres traducidos; solo si aún no hay componentes. */
    fun ponerComponentesPorDefecto(nombres: List<String>) = _wizardState.update { state ->
        if (state.componentes.isNotEmpty()) state
        else state.copy(componentes = nombres.zip(listOf(0.30f, 0.30f, 0.40f)) { n, p -> ComponenteInput(n, p) })
    }

    fun updateComponenteNombre(index: Int, nombre: String) = _wizardState.update { state ->
        state.copy(
            componentes = state.componentes.toMutableList().apply {
                this[index] = this[index].copy(nombre = nombre)
            }
        )
    }

    fun updateComponentePorcentaje(index: Int, porcentaje: Float) = _wizardState.update { state ->
        val snapped = GradeCalculator.snapPct(porcentaje.toDouble())
        state.copy(
            componentes = state.componentes.toMutableList().apply {
                this[index] = this[index].copy(porcentaje = snapped)
            }
        )
    }

    fun repartirRestante() = _wizardState.update { state ->
        val nuevos = GradeCalculator.repartirRestante(state.componentes.map { it.porcentaje })
        state.copy(componentes = state.componentes.zip(nuevos) { c, p -> c.copy(porcentaje = p) })
    }

    fun removeComponente(index: Int) = _wizardState.update { state ->
        state.copy(componentes = state.componentes.toMutableList().apply { removeAt(index) })
    }

    // ── Guardar Materia ────────────────────────────────────────

    /** [otra] = "Guardar y crear otra": vuelve al paso 1 con el mismo periodo, escala y estructura. */
    fun guardarMateria(otra: Boolean = false) {
        viewModelScope.launch {
            _saveState.value = SaveState.Loading
            val state = _wizardState.value

            // Validaciones
            if (state.nombre.isBlank()) {
                fail(UiText(R.string.error_nombre_obligatorio))
                return@launch
            }
            val sumaPorcentajes = state.componentes.sumOf { it.porcentaje.toDouble() }.toFloat()
            if (state.componentes.isNotEmpty() && kotlin.math.abs(sumaPorcentajes - 1f) > 0.01f) {
                fail(UiText(R.string.error_porcentajes))
                return@launch
            }

            try {
                val usuario = usuarioDao.getUsuarioActivoOnce()
                    ?: throw IllegalStateException("No hay usuario activo")

                // Crear materia
                val materia = Materia(
                    usuarioId = usuario.googleId,
                    nombre = state.nombre.trim(),
                    periodo = state.periodo.trim(),
                    profesor = state.profesor.takeIf { it.isNotBlank() },
                    escalaMin = state.escalaMin,
                    escalaMax = state.escalaMax,
                    notaAprobacion = state.notaAprobacion,
                    creditos = state.creditos,
                    tipoEscala = state.tipoEscala
                )
                val materiaId = materiaRepository.insertMateria(materia)

                // Crear componentes, cada uno con una sub-nota por defecto (100% del corte)
                state.componentes.forEachIndexed { index, input ->
                    val componenteId = materiaRepository.insertComponente(
                        Componente(
                            materiaId = materiaId,
                            nombre = input.nombre,
                            porcentaje = input.porcentaje,
                            orden = index
                        )
                    )
                    // Sub-nota por defecto: representa el 100% del componente
                    materiaRepository.insertSubNota(
                        SubNota(
                            componenteId = componenteId,
                            descripcion = input.nombre,
                            porcentajeDelComponente = 1.0f
                        )
                    )
                }

                Timber.i("Materia creada: ${state.nombre} (id=$materiaId)")
                _saveState.value = SaveState.Idle
                if (otra) {
                    val guardada = state
                    _wizardState.value = WizardState(componentes = guardada.componentes).conBaseDe(guardada)
                    _otras.update { listOf(materia.copy(id = materiaId, componentes = guardada.componentes.mapIndexed { i, c ->
                        Componente(materiaId = materiaId, nombre = c.nombre, porcentaje = c.porcentaje, orden = i) })) + it }
                    _events.trySend(UiEvent.Message(UiText(R.string.cw_saved_next, guardada.nombre.trim())))
                } else {
                    _events.trySend(UiEvent.Done)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al guardar materia")
                fail(UiText(R.string.error_generic))
            }
        }
    }

    fun goBack() = _wizardState.update { state ->
        if (state.currentStep > 1) state.copy(currentStep = state.currentStep - 1)
        else state
    }

    private fun fail(text: UiText) {
        _saveState.value = SaveState.Idle
        _events.trySend(UiEvent.Message(text))
    }
}

// ── Modelos de estado del Wizard ───────────────────────────────

data class WizardState(
    val currentStep: Int = 1,
    // Paso 1
    val nombre: String = "",
    val periodo: String = "",
    val profesor: String = "",
    val creditos: Int = 3,
    // Paso 2
    val tipoEscala: TipoEscala = TipoEscala.NUMERICO_5,
    val escalaMin: Float = 0f,
    val escalaMax: Float = 5f,
    val notaAprobacion: Float = 3f,
    // Paso 3
    val componentes: List<ComponenteInput> = emptyList()   // los pone la UI ya traducidos
) : java.io.Serializable {
    val sumaPorcentajes: Float get() = componentes.sumOf { it.porcentaje.toDouble() }.toFloat()

    /** Mismo periodo, créditos y escala que [m] (para la siguiente materia del semestre). */
    fun conBaseDe(m: Materia) = copy(
        periodo = m.periodo, creditos = m.creditos, tipoEscala = m.tipoEscala,
        escalaMin = m.escalaMin, escalaMax = m.escalaMax, notaAprobacion = m.notaAprobacion
    )

    fun conBaseDe(w: WizardState) = copy(
        periodo = w.periodo, creditos = w.creditos, tipoEscala = w.tipoEscala,
        escalaMin = w.escalaMin, escalaMax = w.escalaMax, notaAprobacion = w.notaAprobacion
    )
    val porcentajesValidos: Boolean get() = kotlin.math.abs(sumaPorcentajes - 1f) <= 0.01f
}

data class ComponenteInput(
    val nombre: String = "",
    val porcentaje: Float = 0f
) : java.io.Serializable {
}

sealed class SaveState {
    data object Idle : SaveState()
    data object Loading : SaveState()
}
