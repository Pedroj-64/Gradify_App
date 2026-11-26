package com.notasapp.ui.calendar

import androidx.compose.material.icons.filled.Schedule
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventFormDialog(
    isEdit: Boolean,
    formState: EventFormState,
    materias: List<Materia>,
    onUpdateTitulo: (String) -> Unit,
    onUpdateDescripcion: (String) -> Unit,
    onUpdateTipo: (TipoEvento) -> Unit,
    onUpdateMateria: (Long) -> Unit,
    onUpdateRecordatorio: (Int) -> Unit,
    onUpdateFecha: (LocalDate) -> Unit,
    onUpdateHora: (java.time.LocalTime) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var tipoExpanded by remember { mutableStateOf(false) }
    var materiaExpanded by remember { mutableStateOf(false) }
    var recordatorioExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) stringResource(R.string.calendar_edit_event) else stringResource(R.string.calendar_new_event)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Título
                OutlinedTextField(
                    value = formState.titulo,
                    onValueChange = onUpdateTitulo,
                    label = { Text(stringResource(R.string.calendar_event_title_hint)) },
                    placeholder = { Text(stringResource(R.string.calendar_event_title_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Tipo de evento
                ExposedDropdownMenuBox(
                    expanded = tipoExpanded,
                    onExpandedChange = { tipoExpanded = it }
                ) {
                    OutlinedTextField(
                        value = "${formState.tipoEvento.emoji} ${formState.tipoEvento.getDisplayName(context)}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.calendar_event_type)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(tipoExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = tipoExpanded,
                        onDismissRequest = { tipoExpanded = false }
                    ) {
                        TipoEvento.entries.forEach { tipo ->
                            DropdownMenuItem(
                                text = { Text("${tipo.emoji} ${tipo.getDisplayName(context)}") },
                                onClick = {
                                    onUpdateTipo(tipo)
                                    tipoExpanded = false
                                }
                            )
                        }
                    }
                }

                // Materia
                ExposedDropdownMenuBox(
                    expanded = materiaExpanded,
                    onExpandedChange = { materiaExpanded = it }
                ) {
                    val selectedMateria = materias.find { it.id == formState.materiaId }
                    OutlinedTextField(
                        value = selectedMateria?.nombre ?: stringResource(R.string.calendar_select_materia),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.calendar_event_materia)) },
                        isError = formState.materiaId == null && formState.titulo.isNotBlank(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(materiaExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = materiaExpanded,
                        onDismissRequest = { materiaExpanded = false }
                    ) {
                        materias.forEach { materia ->
                            DropdownMenuItem(
                                text = { Text(materia.nombre) },
                                onClick = {
                                    onUpdateMateria(materia.id)
                                    materiaExpanded = false
                                }
                            )
                        }
                    }
                }

                // Fecha y hora con los selectores nativos (idioma y 12/24 h del teléfono).
                // Antes eran botones ±1 día / ±1 h: 21 toques para un parcial en 3 semanas y sin minutos.
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            val f = formState.fecha
                            android.app.DatePickerDialog(context, { _, y, m, d -> onUpdateFecha(LocalDate.of(y, m + 1, d)) },
                                f.year, f.monthValue - 1, f.dayOfMonth).show()
                        },
                        modifier = Modifier.weight(1.4f)
                    ) {
                        Icon(Icons.Default.Event, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(formState.fecha.format(java.time.format.DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM)))
                    }
                    OutlinedButton(
                        onClick = {
                            val h = formState.hora
                            android.app.TimePickerDialog(context, { _, hh, mm -> onUpdateHora(java.time.LocalTime.of(hh, mm)) },
                                h.hour, h.minute, android.text.format.DateFormat.is24HourFormat(context)).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Schedule, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(formState.hora.format(java.time.format.DateTimeFormatter.ofLocalizedTime(java.time.format.FormatStyle.SHORT)))
                    }
                }

                // Recordatorio
                ExposedDropdownMenuBox(
                    expanded = recordatorioExpanded,
                    onExpandedChange = { recordatorioExpanded = it }
                ) {
                    val recordatorioLabels = mapOf(
                        0 to stringResource(R.string.calendar_no_reminder),
                        15 to stringResource(R.string.calendar_15min),
                        30 to stringResource(R.string.calendar_30min),
                        60 to stringResource(R.string.calendar_1h),
                        120 to stringResource(R.string.calendar_2h),
                        1440 to stringResource(R.string.calendar_1d),
                        2880 to stringResource(R.string.calendar_2d)
                    )
                    OutlinedTextField(
                        value = recordatorioLabels[formState.recordatorioMinutos]
                            ?: stringResource(R.string.calendar_n_min_before, formState.recordatorioMinutos),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.calendar_event_reminder)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(recordatorioExpanded)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = recordatorioExpanded,
                        onDismissRequest = { recordatorioExpanded = false }
                    ) {
                        recordatorioLabels.forEach { (mins, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    onUpdateRecordatorio(mins)
                                    recordatorioExpanded = false
                                }
                            )
                        }
                    }
                }

                // Descripción
                OutlinedTextField(
                    value = formState.descripcion,
                    onValueChange = onUpdateDescripcion,
                    label = { Text(stringResource(R.string.calendar_description_label)) },
                    placeholder = { Text(stringResource(R.string.calendar_description_placeholder)) },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = formState.isValid
            ) {
                Text(if (isEdit) stringResource(R.string.calendar_update) else stringResource(R.string.calendar_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.calendar_cancel)) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GoogleCalendarImportDialog(
    materias: List<Materia>,
    onImport: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMateriaId by remember { mutableStateOf(materias.firstOrNull()?.id) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.CloudDownload, null) },
        title = { Text(stringResource(R.string.gcal_import_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.gcal_import_description),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    stringResource(R.string.gcal_import_select_materia),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    val selected = materias.find { it.id == selectedMateriaId }
                    OutlinedTextField(
                        value = selected?.nombre ?: stringResource(R.string.calendar_select_materia),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.calendar_event_materia)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        materias.forEach { materia ->
                            DropdownMenuItem(
                                text = { Text(materia.nombre) },
                                onClick = {
                                    selectedMateriaId = materia.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedMateriaId?.let { onImport(it) } },
                enabled = selectedMateriaId != null
            ) {
                Text(stringResource(R.string.gcal_import_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) }
        }
    )
}
