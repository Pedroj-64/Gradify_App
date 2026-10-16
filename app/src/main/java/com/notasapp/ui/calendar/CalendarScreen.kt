package com.notasapp.ui.calendar

import androidx.compose.ui.semantics.Role
import androidx.compose.material3.minimumInteractiveComponentSize
import com.notasapp.ui.components.GradifyDialog
import com.notasapp.ui.components.MenuAction
import com.notasapp.ui.components.RowMenu
import com.notasapp.ui.components.ScreenHeader
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.notasapp.R
import com.notasapp.domain.model.ExamenEvent
import com.notasapp.domain.model.Materia
import com.notasapp.domain.model.TipoEvento
import com.notasapp.ui.components.NotificationPermissionFlow
import com.notasapp.utils.getDisplayName
import com.notasapp.utils.getRecordatorioDisplay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Pantalla de calendario académico con vista mensual.
 *
 * Muestra un grid de días con indicadores de eventos (exámenes, tareas, etc.),
 * permite crear, editar y eliminar eventos, y programa recordatorios.
 */
@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val materias by viewModel.materias.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val calendarAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.onGoogleCalendarAuthResult(result.resultCode == Activity.RESULT_OK)
    }

    LaunchedEffect(uiState.googleCalendarRecoveryIntent) {
        val intent = uiState.googleCalendarRecoveryIntent ?: return@LaunchedEffect
        viewModel.onGoogleCalendarRecoveryIntentConsumed()
        calendarAuthLauncher.launch(intent)
    }

    val msgContext = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(uiState.successMessage, uiState.error) {
        val msg = uiState.successMessage ?: uiState.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg.asString(msgContext))
        viewModel.clearMessages()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.calendar_add_event))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Header: mes + navegación ────────────────────────────────
            MonthHeader(
                currentMonth = uiState.currentMonth,
                importing = uiState.isImportingGoogleCalendar,
                onPrevious = viewModel::goToPreviousMonth,
                onNext = viewModel::goToNextMonth,
                onToday = viewModel::goToToday,
                onImport = viewModel::showGoogleCalendarDialog
            )

            // ── Grid del calendario ─────────────────────────────────────
            CalendarGrid(
                currentMonth = uiState.currentMonth,
                selectedDate = uiState.selectedDate,
                events = uiState.events,
                onDateSelected = viewModel::selectDate
            )

            // ── Eventos del día seleccionado ────────────────────────────
            DayEventsList(
                selectedDate = uiState.selectedDate,
                events = uiState.eventsForSelectedDate,
                onEdit = viewModel::showEditDialog,
                onDelete = viewModel::deleteEvent,
                onAddEvent = { viewModel.showCreateDialog(uiState.selectedDate) },
                modifier = Modifier.weight(1f)
            )
        }
    }

    // ── Diálogos ────────────────────────────────────────────────────────
    if (uiState.showCreateDialog || uiState.showEditDialog) {
        EventFormDialog(
            isEdit = uiState.showEditDialog,
            formState = formState,
            materias = materias,
            onUpdateTitulo = { t -> viewModel.updateForm { copy(titulo = t) } },
            onUpdateDescripcion = { d -> viewModel.updateForm { copy(descripcion = d) } },
            onUpdateTipo = { t -> viewModel.updateForm { copy(tipoEvento = t) } },
            onUpdateMateria = { id -> viewModel.updateForm { copy(materiaId = id) } },
            onUpdateRecordatorio = { r -> viewModel.onReminderSelected(r) },
            onUpdateFecha = { f -> viewModel.updateForm { copy(fecha = f) } },
            onUpdateHora = { h -> viewModel.updateForm { copy(hora = h) } },
            onSave = viewModel::saveEvent,
            onDismiss = viewModel::dismissDialog
        )
    }

    // ── Diálogo de explicación de permiso de notificaciones ──────────
    if (uiState.showNotificationPermissionFlow) {
        NotificationPermissionFlow(
            onResult = viewModel::onNotificationPermissionResult,
            onDismiss = viewModel::dismissNotificationPermission
        )
    }

    // ── Diálogo de importación de Google Calendar ──────────────────
    if (uiState.showGoogleCalendarDialog) {
        GoogleCalendarImportDialog(
            materias = materias,
            onImport = { materiaId -> viewModel.importFromGoogleCalendar(materiaId) },
            onDismiss = viewModel::dismissGoogleCalendarDialog
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  Componentes internos
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun MonthHeader(
    currentMonth: YearMonth,
    importing: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    onImport: () -> Unit
) {
    val mes = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        .replaceFirstChar { it.uppercase() }
    ScreenHeader(
        title = mes,
        subtitle = currentMonth.year.toString(),
        actions = {
            IconButton(onClick = onPrevious) {
                Icon(Icons.Default.ChevronLeft, stringResource(R.string.calendar_prev_month))
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Default.ChevronRight, stringResource(R.string.calendar_next_month))
            }
            if (importing) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                RowMenu(
                    items = listOf(
                        MenuAction(stringResource(R.string.calendar_go_today)) { onToday() },
                        MenuAction(stringResource(R.string.calendar_import_gcal)) { onImport() }
                    )
                )
            }
        }
    )
}

@Composable
private fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    events: List<ExamenEvent>,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysOfWeek = listOf(
        stringResource(R.string.calendar_day_mon),
        stringResource(R.string.calendar_day_tue),
        stringResource(R.string.calendar_day_wed),
        stringResource(R.string.calendar_day_thu),
        stringResource(R.string.calendar_day_fri),
        stringResource(R.string.calendar_day_sat),
        stringResource(R.string.calendar_day_sun)
    )
    val today = LocalDate.now()

    // Calcular celdas del grid
    val firstDay = currentMonth.atDay(1)
    val firstDayOfWeek = firstDay.dayOfWeek.value // 1=Monday ... 7=Sunday
    val daysInMonth = currentMonth.lengthOfMonth()
    val leadingEmptyDays = firstDayOfWeek - 1 // Monday = 0 leading days
    val totalCells = leadingEmptyDays + daysInMonth

    // Map de fechas con eventos
    val eventDates = events.groupBy { it.fecha }

    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
        // Días de la semana header
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // Grid de días — render week by week
        val weeks = (0 until totalCells).chunked(7)
        weeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { cellIndex ->
                    val dayNumber = cellIndex - leadingEmptyDays + 1
                    if (dayNumber < 1 || dayNumber > daysInMonth) {
                        // Empty cell
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    } else {
                        val date = currentMonth.atDay(dayNumber)
                        val isToday = date == today
                        val isSelected = date == selectedDate
                        val dayEvents = eventDates[date] ?: emptyList()

                        DayCell(
                            day = dayNumber,
                            isToday = isToday,
                            isSelected = isSelected,
                            eventCount = dayEvents.size,
                            eventTypes = dayEvents.map { it.tipoEvento }.distinct(),
                            onClick = { onDateSelected(date) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                // Pad remaining cells in the last week
                val remaining = 7 - week.size
                repeat(remaining) {
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    eventCount: Int,
    eventTypes: List<TipoEvento>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(
                onClickLabel = stringResource(R.string.cal_select_day),
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                fontSize = 14.sp
            )
            if (eventCount > 0) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 1.dp)
                ) {
                    val dots = eventTypes.take(3)
                    dots.forEach { tipo ->
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .padding(horizontal = 0.5.dp)
                                .clip(CircleShape)
                                .background(eventTypeColor(tipo))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayEventsList(
    selectedDate: LocalDate,
    events: List<ExamenEvent>,
    onEdit: (ExamenEvent) -> Unit,
    onDelete: (ExamenEvent) -> Unit,
    onAddEvent: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedDate.format(
                    DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault())
                ).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (events.isEmpty()) {
                TextButton(onClick = onAddEvent) {
                    Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.detail_add))
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (events.isEmpty()) {
            Text(
                text = stringResource(R.string.calendar_no_events),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events, key = { it.id }) { event ->
                    EventCard(
                        event = event,
                        onEdit = { onEdit(event) },
                        onDelete = { onDelete(event) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EventCard(
    event: ExamenEvent,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .minimumInteractiveComponentSize()
            .clickable(
                onClickLabel = stringResource(R.string.cal_edit_event),
                role = Role.Button,
                onClick = onEdit
            )
            .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(44.dp)
                .clip(CircleShape)
                .background(eventTypeColor(event.tipoEvento))
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = buildString {
                    append(event.tipoEvento.name.lowercase().replaceFirstChar { it.uppercase() })
                    append(" · ")
                    append(event.horaDisplay)
                    if (event.materiaNombre.isNotBlank()) append(" · ${event.materiaNombre}")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (event.descripcion.isNotBlank()) {
                Text(
                    text = event.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = getRecordatorioDisplay(context, event.recordatorioMinutos),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        RowMenu(
            items = listOf(
                MenuAction(stringResource(R.string.calendar_edit)) { onEdit() },
                MenuAction(stringResource(R.string.btn_delete), destructive = true) { showDeleteConfirm = true }
            )
        )
    }

    if (showDeleteConfirm) {
        GradifyDialog(
            title = stringResource(R.string.calendar_delete_title),
            onDismiss = { showDeleteConfirm = false },
            confirmText = stringResource(R.string.btn_delete),
            destructive = true,
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            }
        ) { Text(stringResource(R.string.calendar_delete_confirm)) }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  Diálogo de formulario
// ═════════════════════════════════════════════════════════════════════════════

// ═════════════════════════════════════════════════════════════════════════════
//  Google Calendar Import Dialog
// ═════════════════════════════════════════════════════════════════════════════

// ═════════════════════════════════════════════════════════════════════════════
//  Helpers
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun eventTypeColor(tipo: TipoEvento): Color = when (tipo) {
    TipoEvento.PARCIAL, TipoEvento.FINAL -> MaterialTheme.colorScheme.error
    TipoEvento.QUIZ -> MaterialTheme.colorScheme.tertiary
    TipoEvento.TAREA, TipoEvento.PROYECTO, TipoEvento.EXPOSICION -> MaterialTheme.colorScheme.primary
    TipoEvento.OTRO -> MaterialTheme.colorScheme.outline
}
