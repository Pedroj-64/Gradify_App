package com.notasapp.ui.materia.detail

import com.notasapp.ui.components.GradifySheet
import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notasapp.R
import com.notasapp.domain.model.Componente
import com.notasapp.domain.model.EstadoMeta
import com.notasapp.domain.model.Materia
import com.notasapp.domain.model.SubNota
import com.notasapp.domain.model.SubNotaDetalle
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.notasapp.ui.components.AnimatedText
import com.notasapp.ui.components.EstadoBadge
import com.notasapp.ui.components.GradeLinearIndicator
import com.notasapp.ui.components.MateriaDetailShimmer
import kotlinx.coroutines.launch

@Composable
internal fun AgregarDetalleDialog(
    subNotaNombre: String,
    porcentajeYaUsado: Float = 0f,
    onDismiss: () -> Unit,
    onAgregar: (descripcion: String, porcentaje: Float) -> Unit
) {
    var descripcion by remember { mutableStateOf("") }
    val disponible = (100f - porcentajeYaUsado * 100f).coerceIn(0f, 100f)
    var porcentajeText by remember { mutableStateOf(disponible.toInt().toString()) }
    val pct = porcentajeText.parseGrade()
    val valido = descripcion.isNotBlank() && pct != null && pct in 1f..disponible.coerceAtLeast(1f)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dd_items_of, subNotaNombre)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text(stringResource(R.string.detail_description)) },
                    placeholder = { Text(stringResource(R.string.detail_example_first_attempt)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = porcentajeText,
                    onValueChange = { porcentajeText = it },
                    label = { Text(stringResource(R.string.dd_weight_activity)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Text(
                            text = stringResource(R.string.dd_available, disponible.toInt()),
                            color = if (pct != null && pct > disponible)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    isError = pct != null && pct > disponible
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = pct ?: return@Button
                    onAgregar(descripcion.trim(), p / 100f)
                },
                enabled = valido
            ) { Text(stringResource(R.string.detail_add)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) }
        }
    )
}

// ── Quick Entry Bottom Sheet ──────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuickEntryBottomSheet(
    materia: Materia,
    onValueChange: (subNotaId: Long, valor: Float?) -> Unit,
    onDismiss: () -> Unit,
    sheetState: androidx.compose.material3.SheetState
) {
    // Collect all simple (non-compound) sub-notas without a value
    data class PendingEntry(
        val componenteNombre: String,
        val subNota: SubNota,
        val escalaMax: Float,
        val escalaMin: Float
    )

    val pending = materia.componentes.flatMap { comp ->
        comp.subNotas
            .filter { !it.esCompuesta && it.valor == null }
            .map { PendingEntry(comp.nombre, it, materia.escalaMax, materia.escalaMin) }
    }

    GradifySheet(onDismiss = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.detail_quick_entry),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.dd_pending, pending.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            if (pending.isEmpty()) {
                Text(
                    text = stringResource(R.string.detail_all_grades_entered),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(pending, key = { it.subNota.id }) { entry ->
                        QuickEntryRow(
                            componenteNombre = entry.componenteNombre,
                            subNota = entry.subNota,
                            escalaMax = entry.escalaMax,
                            escalaMin = entry.escalaMin,
                            onValueChange = { valor -> onValueChange(entry.subNota.id, valor) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
internal fun QuickEntryRow(
    componenteNombre: String,
    subNota: SubNota,
    escalaMax: Float,
    escalaMin: Float,
    onValueChange: (Float?) -> Unit
) {
    var textValue by remember(subNota.valor) {
        mutableStateOf(subNota.valor?.toInputString() ?: "")
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subNota.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = componenteNombre,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedTextField(
                value = textValue,
                onValueChange = { input ->
                    textValue = input
                    val parsed = input.parseGrade()
                    if (parsed != null && parsed in escalaMin..escalaMax) {
                        onValueChange(parsed)
                    } else if (input.isEmpty()) {
                        onValueChange(null)
                    }
                },
                modifier = Modifier.width(90.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                placeholder = {
                    Text("${escalaMin.toInt()}-${escalaMax.toInt()}", style = MaterialTheme.typography.bodySmall)
                }
            )
        }
    }
}

// ── Diálogos de creación ──────────────────────────────────────

@Composable
internal fun AgregarSubNotaDialog(
    componenteNombre: String,
    porcentajeYaUsado: Float = 0f,
    onDismiss: () -> Unit,
    onAgregar: (descripcion: String, porcentaje: Float) -> Unit
) {
    var descripcion by remember { mutableStateOf("") }
    val disponible = (100f - porcentajeYaUsado * 100f).coerceIn(0f, 100f)
    var porcentajeText by remember { mutableStateOf(disponible.toInt().toString()) }
    val pct = porcentajeText.parseGrade()
    val valido = descripcion.isNotBlank() && pct != null && pct in 1f..disponible.coerceAtLeast(1f)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dd_add_grade, componenteNombre)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text(stringResource(R.string.detail_description)) },
                    placeholder = { Text(stringResource(R.string.detail_example_partial_1)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = porcentajeText,
                    onValueChange = { porcentajeText = it },
                    label = { Text(stringResource(R.string.dd_weight_cut)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Text(
                            text = stringResource(R.string.dd_available, disponible.toInt()),
                            color = if (pct != null && pct > disponible)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    isError = pct != null && pct > disponible
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = pct ?: return@Button
                    onAgregar(descripcion.trim(), p / 100f)
                },
                enabled = valido
            ) { Text(stringResource(R.string.detail_add)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) }
        }
    )
}

// ── Sección Meta y Notas ──────────────────────────────────────

