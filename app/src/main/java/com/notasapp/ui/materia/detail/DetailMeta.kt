package com.notasapp.ui.materia.detail

import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import com.notasapp.ui.components.gradeStyle
import com.notasapp.ui.components.Tone
import com.notasapp.ui.components.StatusLabel
import com.notasapp.ui.components.SurfaceCard
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
import com.notasapp.ui.components.PromedioGauge
import kotlinx.coroutines.launch

@Composable
internal fun MetaYNotasSection(
    materia: Materia,
    onEditMeta: () -> Unit,
    onEditNotas: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metaTone = when (materia.estadoMeta) {
        EstadoMeta.ALCANZADA, EstadoMeta.EN_CAMINO -> Tone.OK
        EstadoMeta.REQUIERE_ESFUERZO -> Tone.WARN
        EstadoMeta.INALCANZABLE -> Tone.BAD
        EstadoMeta.SIN_META -> Tone.NONE
    }
    val metaEstado = when (materia.estadoMeta) {
        EstadoMeta.ALCANZADA -> stringResource(R.string.goal_achieved)
        EstadoMeta.EN_CAMINO -> stringResource(R.string.goal_on_track)
        EstadoMeta.REQUIERE_ESFUERZO -> stringResource(R.string.goal_effort_needed)
        EstadoMeta.INALCANZABLE -> stringResource(R.string.goal_unreachable)
        EstadoMeta.SIN_META -> ""
    }
    val tieneNotas = materia.notas?.isNotBlank() == true

    SurfaceCard(modifier = modifier) {
        // ── Meta ───────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onEditMeta)
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Meta",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            if (materia.tieneMeta) {
                if (metaEstado.isNotEmpty()) {
                    StatusLabel(metaEstado, metaTone)
                    Spacer(Modifier.width(12.dp))
                }
                Text(text = "${materia.notaMeta}", style = gradeStyle(20))
            } else {
                Text(
                    text = stringResource(R.string.goal_no_goal),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 6.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        // ── Notas personales ──────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onEditNotas)
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Notas",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (tieneNotas) materia.notas!!.lineSequence().first() else "Añadir",
                style = MaterialTheme.typography.bodyMedium,
                color = if (tieneNotas) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1.4f, fill = false)
            )
        }
    }
}

// ── Diálogos ──────────────────────────────────────────────────

@Composable
internal fun MetaDialog(
    currentMeta: Float?,
    escalaMax: Float,
    onDismiss: () -> Unit,
    onSave: (Float?) -> Unit
) {
    var metaText by remember { mutableStateOf(currentMeta?.toInputString() ?: "") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val parsedMeta = metaText.parseGrade()
    val isValid = metaText.isEmpty() || (parsedMeta != null && parsedMeta > 0f && parsedMeta <= escalaMax)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.goal_set)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.goal_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = metaText,
                    onValueChange = { metaText = it },
                    label = { Text("Nota meta") },
                    placeholder = { Text("0.0 - ${escalaMax.toInt()}") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        if (parsedMeta != null && parsedMeta > escalaMax) {
                            Text(
                                text = "Máximo: ${escalaMax.toInt()}",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text("Deja vacío para quitar meta")
                        }
                    },
                    isError = !isValid
                )

                if (currentMeta != null) {
                    TextButton(
                        onClick = { showDeleteConfirm = true },
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(stringResource(R.string.goal_remove))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val meta = if (metaText.isEmpty()) null else parsedMeta
                    onSave(meta)
                },
                enabled = isValid
            ) {
                Text(stringResource(R.string.btn_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.goal_remove)) },
            text = { Text("¿Quitar la meta académica?") },
            confirmButton = {
                Button(
                    onClick = {
                        onSave(null)
                        showDeleteConfirm = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.btn_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
internal fun NotasDialog(
    currentNotas: String?,
    onDismiss: () -> Unit,
    onSave: (String?) -> Unit
) {
    var notasText by remember { mutableStateOf(currentNotas ?: "") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.notes_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.notes_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = notasText,
                    onValueChange = { notasText = it },
                    label = { Text("Notas") },
                    placeholder = { Text(stringResource(R.string.notes_placeholder)) },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Text("Deja vacío para quitar notas")
                    }
                )

                if (!currentNotas.isNullOrBlank()) {
                    TextButton(
                        onClick = { showDeleteConfirm = true },
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar notas")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val notes = notasText.takeIf { it.isNotBlank() }
                    onSave(notes)
                }
            ) {
                Text(stringResource(R.string.btn_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar notas") },
            text = { Text("¿Eliminar todas las notas personales?") },
            confirmButton = {
                Button(
                    onClick = {
                        onSave(null)
                        showDeleteConfirm = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.btn_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}
