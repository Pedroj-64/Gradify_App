package com.notasapp.ui.home

import com.notasapp.ui.components.onContainer
import com.notasapp.ui.components.container
import androidx.compose.ui.graphics.Color
import com.notasapp.ui.components.toInputString
import com.notasapp.ui.components.materiaColor
import com.notasapp.ui.components.MateriaAvatar
import com.notasapp.ui.components.HeroPill
import com.notasapp.ui.components.HeroCard
import com.notasapp.ui.components.pctText
import com.notasapp.ui.components.Axo
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.automirrored.filled.OpenInNew

import com.notasapp.ui.components.AxoMood
import com.notasapp.ui.components.EmptyState
import com.notasapp.ui.components.gradifyChipColors
import androidx.compose.material.icons.filled.Check
import com.notasapp.ui.components.gradeStyle
import com.notasapp.ui.components.color
import com.notasapp.ui.components.Tone
import com.notasapp.ui.components.StatusLabel
import com.notasapp.ui.components.SurfaceCard
import com.notasapp.ui.components.ScreenHeader
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.launch
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notasapp.R
import com.notasapp.domain.model.Materia
import com.notasapp.domain.util.GradeCalculator
import com.notasapp.ui.components.AnimatedText
import com.notasapp.ui.components.EstadoBadge
import com.notasapp.ui.components.GradeLinearIndicator
import com.notasapp.ui.components.MateriaCardShimmer
import kotlinx.coroutines.delay

/**
 * Pantalla principal: lista de materias del usuario.
 *
 * - Shimmer skeleton mientras llega el primer dato del Flow.
 * - Animación de entrada escalonada (stagger) por cada card.
 * - Mantener pulsada una materia abre un menú (abrir / eliminar con Deshacer).
 * - Acceso rápido a la pantalla de estadísticas desde el TopAppBar.
 *
 * @param onNavigateToCreateMateria  Abre el Wizard de nueva materia.
 * @param onNavigateToMateria        Abre el detalle de la materia dada.
 * @param onNavigateToEstadisticas   Abre la pantalla de estadísticas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCreateMateria: () -> Unit,
    onNavigateToMateria: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val ctxHome = androidx.compose.ui.platform.LocalContext.current
    val materias by viewModel.materias.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val update by viewModel.update.collectAsStateWithLifecycle()
    val faltaRespaldo by viewModel.faltaRespaldo.collectAsStateWithLifecycle()
    val folderLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            ctxHome.contentResolver.takePersistableUriPermission(
                it,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            viewModel.elegirCarpetaRespaldo(it)
        }
    }
    val filtroSemestre by viewModel.filtroSemestre.collectAsStateWithLifecycle()
    val orden by viewModel.orden.collectAsStateWithLifecycle()
    val semestres by viewModel.semestresDisponibles.collectAsStateWithLifecycle()
    val materiasEnRiesgo by viewModel.materiasEnRiesgo.collectAsStateWithLifecycle()
    val promedioGeneral by viewModel.promedioGeneral.collectAsStateWithLifecycle()
    val verArchivadas by viewModel.verArchivadas.collectAsStateWithLifecycle()
    val hayArchivadas by viewModel.hayArchivadas.collectAsStateWithLifecycle()
    val vacia = materias.isEmpty() && searchQuery.isBlank() && !hayArchivadas
    val scope = rememberCoroutineScope()
    val deletedMsg = stringResource(R.string.home_deleted)
    val undoLabel = stringResource(R.string.btn_undo)
    val snackbarHostState = remember { SnackbarHostState() }

    var showSearch by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Controlamos si la primera carga ya resolvió
    var firstLoadDone by remember { mutableStateOf(false) }
    LaunchedEffect(materias) {
        if (!firstLoadDone) {
            delay(300)
            firstLoadDone = true
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it.asString(context))
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            ScreenHeader(
                title = stringResource(R.string.home_title),
                // Sin materias: ni "0 materias" ni buscar/ordenar (no hay nada que buscar).
                subtitle = if (vacia) null else pluralStringResource(R.plurals.home_count, materias.size, materias.size),
                actions = {
                  if (!vacia) {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.home_search))
                    }
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            @Suppress("DEPRECATION")
                            Icon(Icons.Default.Sort, contentDescription = stringResource(R.string.home_sort))
                        }
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            OrdenMateria.entries.forEach { o ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(o.label)) },
                                    onClick = {
                                        viewModel.updateOrden(o)
                                        showSortMenu = false
                                    },
                                    leadingIcon = if (orden == o) {
                                        { Icon(Icons.Default.Check, contentDescription = null) }
                                    } else null
                                )
                            }
                        }
                    }
                  }
                }
            )
        },
        floatingActionButton = {
            if (materias.isNotEmpty() || searchQuery.isNotBlank() || filtroSemestre != null || hayArchivadas)
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreateMateria,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.home_fab_add)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        when {
            // --- Shimmer de carga inicial ---
            !firstLoadDone -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(4) { MateriaCardShimmer() }
                }
            }

            // --- Lista vacía (sin materias reales, no solo sin filtro) ---
            materias.isEmpty() && searchQuery.isBlank() && filtroSemestre == null && !verArchivadas && !hayArchivadas -> {
                EmptyState(
                    icon = null,
                    mood = AxoMood.TRANQUI,
                    title = stringResource(R.string.home_welcome),
                    hint = stringResource(R.string.home_welcome_hint),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    action = {
                        Button(onClick = onNavigateToCreateMateria) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.home_add_first))
                        }
                    }
                )
            }

            // --- Lista con materias (o resultado de búsqueda) ---
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ── Barra de búsqueda ─────────────────────────
                    if (showSearch) {
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text(stringResource(R.string.home_search_placeholder)) },
                                leadingIcon = { Icon(Icons.Default.Search, null) },
                                trailingIcon = {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                            Icon(Icons.Default.Clear, stringResource(R.string.home_clear))
                                        }
                                    }
                                },
                                singleLine = true
                            )
                        }
                    }

                    // ── Filtro por semestre + semestres cerrados ──
                    if (semestres.isNotEmpty() || hayArchivadas || verArchivadas) {
                        item {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (semestres.isNotEmpty()) {
                                    item {
                                        FilterChip(
                                            colors = gradifyChipColors(),
                                            selected = filtroSemestre == null,
                                            onClick = { viewModel.updateFiltroSemestre(null) },
                                            label = { Text(stringResource(R.string.home_filter_all)) }
                                        )
                                    }
                                    items(semestres, key = { it }) { sem ->
                                        FilterChip(
                                            colors = gradifyChipColors(),
                                            selected = filtroSemestre == sem,
                                            onClick = {
                                                viewModel.updateFiltroSemestre(
                                                    if (filtroSemestre == sem) null else sem
                                                )
                                            },
                                            label = { Text(sem) }
                                        )
                                    }
                                }
                                if (hayArchivadas || verArchivadas) {
                                    item {
                                        FilterChip(
                                            colors = gradifyChipColors(),
                                            selected = verArchivadas,
                                            onClick = { viewModel.toggleArchivadas() },
                                            label = { Text(stringResource(R.string.home_archived)) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── Cerrar / reabrir el semestre seleccionado ──
                    if (filtroSemestre != null) {
                        item {
                            TextButton(
                                onClick = { viewModel.setSemestreArchivado(filtroSemestre!!, archivado = !verArchivadas) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    stringResource(
                                        if (verArchivadas) R.string.home_reopen_semester else R.string.home_close_semester,
                                        filtroSemestre!!
                                    )
                                )
                            }
                        }
                    }

                    // ── Versión nueva en GitHub ───────────────────
                    update?.let { u ->
                        item(key = "update") {
                            AvisoCard(
                                mood = AxoMood.FELIZ,
                                tone = Tone.NONE,
                                title = stringResource(R.string.update_title, u.version),
                                text = stringResource(R.string.update_desc),
                                action = stringResource(R.string.update_download),
                                onAction = {
                                    context.startActivity(
                                        android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(u.apkUrl))
                                    )
                                },
                                onDismiss = viewModel::descartarUpdate
                            )
                        }
                    }

                    // ── Sin carpeta de respaldo ───────────────────
                    if (faltaRespaldo) {
                        item(key = "respaldo") {
                            AvisoCard(
                                mood = AxoMood.PREOCUPADO,
                                tone = Tone.WARN,
                                title = stringResource(R.string.backup_nag_title),
                                text = stringResource(R.string.backup_nag_desc),
                                action = stringResource(R.string.backup_folder_choose),
                                onAction = { folderLauncher.launch(null) },
                                onDismiss = viewModel::descartarRespaldo
                            )
                        }
                    }

                    // ── Todo cerrado ──────────────────────────────
                    if (materias.isEmpty() && !verArchivadas && hayArchivadas && searchQuery.isBlank()) {
                        item {
                            EmptyState(icon = null, mood = AxoMood.FELIZ, title = stringResource(R.string.home_all_closed))
                        }
                    }

                    // ── Resumen ───────────────────────────────────
                    if (!verArchivadas && (promedioGeneral != null || materiasEnRiesgo.isNotEmpty())) {
                        item {
                            SummaryHero(
                                promedioGeneral = promedioGeneral,
                                escalaMax = materias.firstOrNull()?.escalaMax ?: 5f,
                                enRiesgo = materiasEnRiesgo.size,
                                sinNotas = materias.count { it.promedio == null }
                            )
                        }
                    }

                    // ── Resultado de búsqueda vacío ───────────────
                    if (materias.isEmpty() && (searchQuery.isNotBlank() || filtroSemestre != null || verArchivadas)) {
                        item {
                            EmptyState(icon = null, mood = AxoMood.PREOCUPADO, title = stringResource(R.string.home_no_results))
                        }
                    }

                    // ── Cards de materias ─────────────────────────
                    itemsIndexed(
                        items = materias,
                        key = { _, m -> m.id }
                    ) { index, materia ->
                        var showed by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(minOf(index, 8) * 50L)   // tope: en listas largas no hacer esperar a las últimas
                            showed = true
                        }

                        AnimatedVisibility(
                            visible = showed,
                            enter = slideInVertically(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ) { it / 3 } + fadeIn(tween(400))
                        ) {
                            // Mantener pulsado → menú. Antes era swipe y se borraban materias sin querer.
                            MateriaCard(
                                materia = materia,
                                onClick = { onNavigateToMateria(materia.id) },
                                onCerrarSemestre = if (verArchivadas) null else {
                                    { viewModel.setSemestreArchivado(materia.periodo, archivado = true) }
                                },
                                onDelete = {
                                    viewModel.softDelete(materia.id)
                                    scope.launch {
                                        val r = snackbarHostState.showSnackbar(
                                            message = deletedMsg,
                                            actionLabel = undoLabel,
                                            withDismissAction = true
                                        )
                                        if (r == SnackbarResult.ActionPerformed) viewModel.undoDelete(materia.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Componentes internos ──────────────────────────────────────

/** Aviso con Axo, fondo del tono, una acción y "Ahora no" (versión nueva, respaldo…). */
@Composable
private fun AvisoCard(
    mood: AxoMood,
    tone: Tone,
    title: String,
    text: String,
    action: String,
    onAction: () -> Unit,
    onDismiss: () -> Unit
) {
    SurfaceCard(container = tone.container()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Axo(mood, size = 56.dp)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = tone.onContainer())
                Text(text, style = MaterialTheme.typography.bodySmall, color = tone.onContainer())
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.update_later), color = tone.onContainer()) }
            Button(onClick = onAction) { Text(action) }
        }
    }
}

/** Promedio general como protagonista: sin tarjeta, número grande y una línea de contexto. */
@Composable
private fun SummaryHero(
    promedioGeneral: Float?,
    escalaMax: Float,
    enRiesgo: Int,
    sinNotas: Int,
    modifier: Modifier = Modifier
) {
    HeroCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_overall_average),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = promedioGeneral?.let { "%.2f".format(it) } ?: "--",
                        style = gradeStyle(56),
                        color = Color.White
                    )
                    Text(
                        text = " / ${escalaMax.toInputString()}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
            Axo(
                when {
                    promedioGeneral == null -> AxoMood.TRANQUI
                    enRiesgo > 0 -> AxoMood.PREOCUPADO
                    else -> AxoMood.FELIZ
                },
                size = 84.dp
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeroPill(
                if (enRiesgo > 0) stringResource(R.string.home_n_at_risk, enRiesgo)
                else stringResource(R.string.home_all_good)
            )
            if (sinNotas > 0) HeroPill(stringResource(R.string.home_n_pending, sinNotas))
        }
    }
}

/** Fila de materia: nombre y nota a la vista, progreso fino en color de marca. */
@Composable
private fun MateriaCard(
    materia: Materia,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onCerrarSemestre: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    var menu by remember { mutableStateOf(false) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
  Box {
    val progreso = materia.porcentajeEvaluado.coerceIn(0f, 1f)
    val tone = when {
        materia.promedio == null -> Tone.NONE
        materia.aprobado -> Tone.OK
        else -> Tone.BAD
    }
    val statusText = when {
        materia.promedio == null -> stringResource(R.string.home_not_evaluated)
        materia.yaAprobo -> stringResource(R.string.home_status_approved)
        materia.aprobado -> stringResource(R.string.home_status_passing)
        else -> stringResource(R.string.home_status_at_risk)
    }.lowercase().replaceFirstChar { it.titlecase() }

    SurfaceCard(
        modifier = modifier,
        onClick = onClick,
        onLongClick = {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            menu = true
        },
        onLongClickLabel = stringResource(R.string.home_card_options)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            MateriaAvatar(materia.nombre, materia.id)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = materia.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = buildString {
                        append(materia.periodo)
                        materia.profesor?.let { append(" · $it") }
                        if (materia.creditos > 0) append(stringResource(R.string.home_credits_short, materia.creditos))
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = materia.promedioDisplay,
                    style = gradeStyle(34),
                    color = if (tone == Tone.NONE) MaterialTheme.colorScheme.outline else tone.color()
                )
                Text(
                    text = "/ ${materia.escalaMax.toInputString()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(progreso)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(materiaColor(materia.id))
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusLabel(statusText, tone)
            Text(
                text = stringResource(R.string.home_evaluated_pct, progreso.pctText()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.home_card_open)) },
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.OpenInNew, null) },
            onClick = { menu = false; onClick() }
        )
        if (onCerrarSemestre != null && materia.periodo.isNotBlank()) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.home_close_semester, materia.periodo)) },
                leadingIcon = { Icon(Icons.Default.Archive, null) },
                onClick = { menu = false; onCerrarSemestre() }
            )
        }
        DropdownMenuItem(
            text = { Text(stringResource(R.string.delete_subject), color = MaterialTheme.colorScheme.error) },
            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            onClick = { menu = false; onDelete() }
        )
    }
  }
}

