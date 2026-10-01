package com.notasapp.ui.materia.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material.icons.filled.MoreVert
import com.notasapp.ui.components.SectionLabel
import com.notasapp.ui.components.ScreenHeader
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
import androidx.compose.material.icons.filled.DriveFileRenameOutline
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

/**
 * Formatea un Float con punto decimal (invariant) para que sea parseable.
 * Acepta tanto punto como coma al parsear.
 */
/** 4.0 → "4", 3.75 → "3.75" (sin redondear ni añadir ceros que estorben al teclear). */
internal fun Float.toInputString(): String =
    java.math.BigDecimal(this.toString()).stripTrailingZeros().toPlainString()
internal fun String.parseGrade(): Float? = this.replace(',', '.').toFloatOrNull()

/**
 * Pantalla de detalle de una materia.
 *
 * Muestra el gauge animado del promedio, componentes con sub-notas editables,
 * shimmer skeleton durante la carga inicial, y un FAB para abrir la
 * calculadora "¿qué nota necesito?".
 *
 * @param materiaId           ID de la materia a mostrar.
 * @param onBack              Callback para volver atrás.
 * @param onEditPorcentajes   Abre la pantalla de edición de porcentajes.
 * @param onExport            Abre la pantalla de exportación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MateriaDetailScreen(
    materiaId: Long,
    onBack: () -> Unit,
    onEditPorcentajes: () -> Unit,
    onExport: () -> Unit,
    onNavigateToRecomendaciones: () -> Unit = {},
    viewModel: MateriaDetailViewModel = hiltViewModel()
) {
    val materia by viewModel.materia.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val notaNecesariaResult by viewModel.notaNecesariaResult.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // ── Bottom sheet de calculadora ───────────────────────────
    var showCalculadora by rememberSaveable { mutableStateOf(false) }
    var showQuickEntry by rememberSaveable { mutableStateOf(false) }
    var showMetaDialog by rememberSaveable { mutableStateOf(false) }
    var showNotasDialog by rememberSaveable { mutableStateOf(false) }
    var showEditMateria by rememberSaveable { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<Triple<RenameKind, Long, String>?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val quickEntrySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalRename provides { kind, id, current -> renameTarget = Triple(kind, id, current) }
    ) {
    Scaffold(
        topBar = {
            var menuOpen by remember { mutableStateOf(false) }
            ScreenHeader(
                title = materia?.nombre ?: "",
                subtitle = materia?.let { m ->
                    buildString {
                        append(m.periodo)
                        m.profesor?.let { append(" · $it") }
                    }
                },
                onBack = onBack,
                actions = {
                    if (materia?.componentes?.any { c ->
                            c.subNotas.any { s -> !s.esCompuesta && s.valor == null }
                        } == true
                    ) {
                        IconButton(onClick = { showQuickEntry = true }) {
                            Icon(Icons.Default.FlashOn, contentDescription = stringResource(R.string.detail_quick_entry))
                        }
                    }
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options))
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.edit_subject_title)) },
                                leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, null) },
                                enabled = materia != null,
                                onClick = { menuOpen = false; showEditMateria = true }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.detail_edit_percentages)) },
                                leadingIcon = { Icon(Icons.Default.Edit, null) },
                                onClick = { menuOpen = false; onEditPorcentajes() }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.detail_study_resources)) },
                                leadingIcon = { Icon(Icons.Default.Lightbulb, null) },
                                onClick = { menuOpen = false; onNavigateToRecomendaciones() }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.detail_export)) },
                                leadingIcon = { Icon(Icons.Default.Share, null) },
                                onClick = { menuOpen = false; onExport() }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (materia != null) {
                FloatingActionButton(
                    onClick = { showCalculadora = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = stringResource(R.string.detail_what_grade_needed),
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        if (materia == null) {
            // --- Shimmer skeleton ---
            MateriaDetailShimmer(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            materia?.let { mat ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        PromedioResumen(materia = mat)
                    }

                    // ── Sección: Meta y Notas ──────────────────────
                    item {
                        MetaYNotasSection(
                            materia = mat,
                            onEditMeta = { showMetaDialog = true },
                            onEditNotas = { showNotasDialog = true }
                        )
                    }

                    // ── Sección: Evaluaciones ──────────────────────
                    item {
                        SectionLabel(stringResource(R.string.detail_evaluations))
                    }
                    items(mat.componentes, key = { it.id }) { componente ->
                        ComponenteCard(
                            componente = componente,
                            escalaMax = mat.escalaMax,
                            onSubNotaValueChange = { subNotaId, valor ->
                                viewModel.actualizarSubNota(subNotaId, valor)
                            },
                            onAgregarSubNota = { desc, pct ->
                                viewModel.agregarSubNota(componente.id, desc, pct)
                            },
                            onEliminarSubNota = { subNotaId ->
                                viewModel.eliminarSubNota(subNotaId)
                            },
                            onAgregarDetalle = { subNotaId, desc, pct ->
                                viewModel.agregarDetalle(subNotaId, desc, pct)
                            },
                            onActualizarDetalle = { detalleId, valor ->
                                viewModel.actualizarDetalle(detalleId, valor)
                            },
                            onEliminarDetalle = { detalleId ->
                                viewModel.eliminarDetalle(detalleId)
                            }
                        )
                    }
                }
            }
        }
    }
    } // CompositionLocalProvider


    // ── Calculadora bottom sheet ──────────────────────────────
    if (showCalculadora) {
        materia?.let { mat ->
            CalculadoraBottomSheet(
                escalaMax = mat.escalaMax,
                resultado = notaNecesariaResult,
                onCalcular = { meta ->
                    viewModel.calcularNotaNecesaria(meta)
                },
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showCalculadora = false
                        viewModel.clearCalculadora()
                    }
                },
                sheetState = sheetState
            )
        }
    }

    // ── Entrada rápida bottom sheet ──────────────────────────
    if (showQuickEntry) {
        materia?.let { mat ->
            QuickEntryBottomSheet(
                materia = mat,
                onValueChange = { subNotaId, valor ->
                    viewModel.actualizarSubNota(subNotaId, valor)
                },
                onDismiss = {
                    scope.launch { quickEntrySheetState.hide() }.invokeOnCompletion {
                        showQuickEntry = false
                    }
                },
                sheetState = quickEntrySheetState
            )
        }
    }

    // ── Diálogo de meta académica ──────────────────────────
    if (showMetaDialog) {
        materia?.let { mat ->
            MetaDialog(
                currentMeta = mat.notaMeta,
                escalaMax = mat.escalaMax,
                onDismiss = { showMetaDialog = false },
                onSave = { newMeta ->
                    viewModel.actualizarNotaMeta(newMeta)
                    showMetaDialog = false
                }
            )
        }
    }

    // ── Diálogo de notas personales ──────────────────────────
    if (showNotasDialog) {
        materia?.let { mat ->
            NotasDialog(
                currentNotas = mat.notas,
                onDismiss = { showNotasDialog = false },
                onSave = { newNotas ->
                    viewModel.actualizarNotas(newNotas)
                    showNotasDialog = false
                }
            )
        }
    }

    // ── Renombrar componente / sub-nota / detalle ──────────
    renameTarget?.let { (kind, id, current) ->
        RenameDialog(
            initial = current,
            onDismiss = { renameTarget = null },
            onConfirm = { viewModel.renombrar(kind, id, it); renameTarget = null }
        )
    }

    // ── Editar datos de la materia ─────────────────────────
    if (showEditMateria) {
        materia?.let { mat ->
            EditMateriaDialog(
                materia = mat,
                onDismiss = { showEditMateria = false },
                onSave = { n, p, prof, c ->
                    viewModel.editarMateria(n, p, prof, c)
                    showEditMateria = false
                }
            )
        }
    }
}
