package com.notasapp.ui.calendar

import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import com.notasapp.utils.IcsParser
import com.notasapp.R
import android.net.Uri
import android.content.Intent
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notasapp.data.local.UserPreferencesRepository
import com.notasapp.data.local.dao.UsuarioDao
import com.notasapp.data.receiver.ExamAlarmScheduler
import com.notasapp.domain.model.ExamenEvent
import com.notasapp.domain.model.Materia
import com.notasapp.domain.model.TipoEvento
import com.notasapp.domain.repository.CalendarRepository
import com.notasapp.domain.repository.MateriaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

/**
 * Estado de la pantalla de calendario.
 */
data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val events: List<ExamenEvent> = emptyList(),
    val eventsForSelectedDate: List<ExamenEvent> = emptyList(),
    val upcomingEvents: List<ExamenEvent> = emptyList(),
    val isLoading: Boolean = false,
    val showCreateDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val editingEvent: ExamenEvent? = null,
    val showNotificationPermissionFlow: Boolean = false,
    val isImporting: Boolean = false,
    val showImportDialog: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * Estado del formulario de creación/edición de evento.
 */
data class EventFormState(
    val titulo: String = "",
    val descripcion: String = "",
    val tipoEvento: TipoEvento = TipoEvento.PARCIAL,
    val materiaId: Long? = null,
    val fecha: LocalDate = LocalDate.now().plusDays(1),
    val hora: LocalTime = LocalTime.of(8, 0),
    val recordatorioMinutos: Int = 60
) {
    val isValid: Boolean
        get() = titulo.isNotBlank() && materiaId != null
}

/**
 * ViewModel para la pantalla de calendario académico.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val materiaRepository: MateriaRepository,
    private val usuarioDao: UsuarioDao,
    private val userPrefsRepository: UserPreferencesRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val alarmScheduler = ExamAlarmScheduler(appContext)

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(EventFormState())
    val formState: StateFlow<EventFormState> = _formState.asStateFlow()

    /** Materias disponibles para asociar eventos. */
    val materias: StateFlow<List<Materia>> = usuarioDao
        .getUsuarioActivo()
        .flatMapLatest { usuario ->
            if (usuario == null) flowOf(emptyList())
            else materiaRepository.getMateriasByUsuario(usuario.googleId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            val usuario = usuarioDao.getUsuarioActivo().first() ?: return@launch

            // Observar eventos del mes actual
            launch {
                combine(
                    _uiState,
                    calendarRepository.getEventsByUsuario(usuario.googleId)
                ) { state, allEvents ->
                    val monthStart = state.currentMonth.atDay(1)
                        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val monthEnd = state.currentMonth.plusMonths(1).atDay(1)
                        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                    val monthEvents = allEvents.filter {
                        it.fechaEpochMs in monthStart until monthEnd
                    }
                    val selectedDateEvents = allEvents.filter {
                        it.fecha == state.selectedDate
                    }

                    state.copy(
                        events = monthEvents,
                        eventsForSelectedDate = selectedDateEvents,
                        upcomingEvents = allEvents.filter { !it.yaPaso }.take(5)
                    )
                }.collect { newState ->
                    _uiState.value = newState
                }
            }
        }
    }

    // ── Navegación del calendario ───────────────────────────────────

    fun goToPreviousMonth() {
        _uiState.update { it.copy(currentMonth = it.currentMonth.minusMonths(1)) }
    }

    fun goToNextMonth() {
        _uiState.update { it.copy(currentMonth = it.currentMonth.plusMonths(1)) }
    }

    fun goToToday() {
        _uiState.update {
            it.copy(
                currentMonth = YearMonth.now(),
                selectedDate = LocalDate.now()
            )
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update {
            it.copy(
                selectedDate = date,
                currentMonth = YearMonth.from(date)
            )
        }
    }

    // ── Crear / Editar evento ───────────────────────────────────────

    fun showCreateDialog(date: LocalDate? = null) {
        _formState.value = EventFormState(
            fecha = date ?: _uiState.value.selectedDate.let {
                if (it.isBefore(LocalDate.now())) LocalDate.now().plusDays(1) else it
            }
        )
        _uiState.update { it.copy(showCreateDialog = true, showEditDialog = false) }
    }

    fun showEditDialog(event: ExamenEvent) {
        _formState.value = EventFormState(
            titulo = event.titulo,
            descripcion = event.descripcion,
            tipoEvento = event.tipoEvento,
            materiaId = event.materiaId,
            fecha = event.fecha,
            hora = event.hora,
            recordatorioMinutos = event.recordatorioMinutos
        )
        _uiState.update {
            it.copy(showEditDialog = true, showCreateDialog = false, editingEvent = event)
        }
    }

    fun dismissDialog() {
        _uiState.update {
            it.copy(showCreateDialog = false, showEditDialog = false, editingEvent = null)
        }
    }

    fun updateForm(transform: EventFormState.() -> EventFormState) {
        _formState.update { it.transform() }
    }

    fun saveEvent() {
        val form = _formState.value
        if (!form.isValid) return

        viewModelScope.launch {
            doSaveEvent(form)
        }
    }

    /**
     * Llamado cuando el usuario selecciona un valor de recordatorio.
     * Si selecciona un recordatorio > 0 y no hemos explicado notificaciones, pedir permiso.
     */
    fun onReminderSelected(minutes: Int) {
        _formState.update { it.copy(recordatorioMinutos = minutes) }
        if (minutes > 0) {
            viewModelScope.launch {
                val hasExplained = userPrefsRepository.hasExplainedNotifications.first()
                if (!hasExplained) {
                    _uiState.update { it.copy(showNotificationPermissionFlow = true) }
                }
            }
        }
    }

    /** Llamado después de que el usuario respondió al diálogo de permiso de notificaciones. */
    fun onNotificationPermissionResult(granted: Boolean) {
        viewModelScope.launch {
            userPrefsRepository.setHasExplainedNotifications()
            _uiState.update { it.copy(showNotificationPermissionFlow = false) }
            if (granted) {
                Timber.i("Permiso de notificaciones concedido")
            } else {
                Timber.i("Permiso de notificaciones denegado")
                // Si denegó, quitar el recordatorio
                _formState.update { it.copy(recordatorioMinutos = 0) }
            }
        }
    }

    fun dismissNotificationPermission() {
        viewModelScope.launch {
            userPrefsRepository.setHasExplainedNotifications()
            _uiState.update { it.copy(showNotificationPermissionFlow = false) }
        }
    }

    private suspend fun doSaveEvent(form: EventFormState) {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val fechaEpochMs = LocalDateTime.of(form.fecha, form.hora)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val event = ExamenEvent(
                id = _uiState.value.editingEvent?.id ?: 0,
                materiaId = form.materiaId!!,
                titulo = form.titulo.trim(),
                descripcion = form.descripcion.trim(),
                tipoEvento = form.tipoEvento,
                fechaEpochMs = fechaEpochMs,
                recordatorioMinutos = form.recordatorioMinutos
            )

            val savedId = calendarRepository.saveEvent(event)
            val isEdit = _uiState.value.editingEvent != null

            // Programar alarma con ID real del evento
            if (form.recordatorioMinutos > 0) {
                val triggerMs = fechaEpochMs - (form.recordatorioMinutos * 60_000L)
                alarmScheduler.scheduleAlarm(
                    eventId = savedId,
                    title = event.titulo,
                    description = event.descripcion,
                    tipoEvento = event.tipoEvento.name,
                    triggerAtMs = triggerMs,
                    materiaId = event.materiaId,
                    reminderMinutes = form.recordatorioMinutos
                )
            } else {
                // Si cambió de con-recordatorio a sin-recordatorio, cancelar alarma
                alarmScheduler.cancelAlarm(savedId)
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    showCreateDialog = false,
                    showEditDialog = false,
                    editingEvent = null,
                    successMessage = if (isEdit) "Evento actualizado" else "Evento creado"
                )
            }
            Timber.i("Evento guardado: ${event.titulo}")
        } catch (e: Exception) {
            Timber.e(e, "Error al guardar evento")
            _uiState.update {
                it.copy(isLoading = false, error = "No se pudo guardar el evento")
            }
        }
    }

    fun deleteEvent(event: ExamenEvent) {
        viewModelScope.launch {
            try {
                calendarRepository.deleteEvent(event.id)
                alarmScheduler.cancelAlarm(event.id)
                _uiState.update {
                    it.copy(successMessage = "Evento eliminado")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al eliminar evento")
                _uiState.update { it.copy(error = "No se pudo eliminar el evento") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    // ── Importar calendario (.ics) ─────────────────────────────────

    fun showImportDialog() {
        _uiState.update { it.copy(showImportDialog = true) }
    }

    fun dismissImportDialog() {
        _uiState.update { it.copy(showImportDialog = false) }
    }

    /**
     * Lee un archivo .ics y guarda sus eventos futuros en [materiaId]. Sin cuenta ni permisos de Google:
     * sirve con archivos de Google Calendar, Outlook, Apple o el calendario de la universidad.
     */
    fun importIcs(uri: Uri, materiaId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, showImportDialog = false, error = null) }
            try {
                val texto = withContext(Dispatchers.IO) { leerTexto(uri) }
                val resultado = IcsParser.parse(texto)

                // No duplicar si el mismo archivo se importa dos veces.
                val existentes = calendarRepository.getEventsByMateria(materiaId).first()
                    .map { it.titulo.trim().lowercase() to it.fechaEpochMs }.toSet()
                var importados = 0
                var yaEstaban = 0
                for (e in resultado.eventos) {
                    if ((e.titulo.trim().lowercase() to e.inicioEpochMs) in existentes) { yaEstaban++; continue }
                    calendarRepository.saveEvent(
                        ExamenEvent(
                            materiaId = materiaId,
                            titulo = e.titulo,
                            descripcion = e.descripcion,
                            tipoEvento = e.tipo,
                            fechaEpochMs = e.inicioEpochMs,
                            recordatorioMinutos = 60
                        )
                    )
                    importados++
                }

                val mensaje = if (resultado.eventos.isEmpty() && resultado.recurrentes == 0 && resultado.pasados == 0)
                    appContext.getString(R.string.ics_empty)
                else
                    appContext.getString(R.string.ics_result, importados, yaEstaban, resultado.recurrentes, resultado.pasados)
                _uiState.update { it.copy(isImporting = false, successMessage = mensaje) }
                Timber.i("ICS: $importados importados, $yaEstaban repetidos, ${resultado.recurrentes} recurrentes, ${resultado.pasados} pasados")
            } catch (e: Exception) {
                Timber.e(e, "Error al importar .ics")
                _uiState.update { it.copy(isImporting = false, error = appContext.getString(R.string.ics_error)) }
            }
        }
    }

    private fun leerTexto(uri: Uri): String {
        val max = 2 * 1024 * 1024   // 2 MB: un calendario normal pesa mucho menos
        val bytes = appContext.contentResolver.openInputStream(uri)?.use { input ->
            val out = java.io.ByteArrayOutputStream()
            val buf = ByteArray(8 * 1024)
            while (true) {
                val n = input.read(buf)
                if (n < 0) break
                out.write(buf, 0, n)
                check(out.size() <= max) { "Archivo demasiado grande" }
            }
            out.toByteArray()
        } ?: error("No se pudo leer el archivo")
        return String(bytes, Charsets.UTF_8)
    }
}
