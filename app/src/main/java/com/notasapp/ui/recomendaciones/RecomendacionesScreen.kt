package com.notasapp.ui.recomendaciones

import com.notasapp.ui.components.ErrorBanner
import com.notasapp.ui.components.EmptyState
import com.notasapp.ui.components.SectionLabel
import com.notasapp.ui.components.SurfaceCard
import com.notasapp.ui.components.ScreenHeader
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notasapp.R
import com.notasapp.data.remote.ai.Recomendacion
import com.notasapp.data.remote.ai.TipoRecomendacion
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay

// ── Metadata por tipo ──────────────────────────────────────────

/**
 * Pantalla de recomendaciones de estudio generadas por Gemini AI.
 *
 * Diseño minimalista: chips de materia, tarjetas con acento lateral de
 * color segun el tipo, skeleton shimmer durante la carga y banners
 * de error/apikey compactos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecomendacionesScreen(
    onBack: () -> Unit,
    onOpenSettings: () -> Unit = {},
    viewModel: RecomendacionesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val materias by viewModel.materias.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            ScreenHeader(
                title = stringResource(R.string.recs_title),
                subtitle = stringResource(R.string.recs_subtitle)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        val sectionVideos = stringResource(R.string.recs_videos)
        val sectionBooks = stringResource(R.string.recs_books)
        val sectionWebResources = stringResource(R.string.recs_web_resources)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Selector de materias
            if (materias.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.School,
                        title = stringResource(R.string.recs_no_subjects),
                        hint = stringResource(R.string.recs_no_subjects_hint)
                    )
                }
            } else {
                item {
                    MateriaSelector(
                        materias = materias.map { it.nombre to it.id },
                        selectedId = uiState.materiaSeleccionada?.id,
                        onSelect = { id ->
                            materias.find { it.id == id }?.let { viewModel.seleccionarMateria(it) }
                        }
                    )
                }
            }

            // API Key faltante
            if (uiState.apiKeyFaltante) {
                item { ApiKeyMissingBanner(onOpenSettings) }
            }

            // Cargando
            if (uiState.isLoading) {
                item {
                    LoadingBanner(materia = uiState.materiaSeleccionada?.nombre ?: "")
                }
                items(3) { i -> RecomendacionCardSkeleton(index = i) }
            }

            // Error
            if (!uiState.isLoading && uiState.error != null) {
                item {
                    ErrorBanner(
                        message = uiState.error!!.asString(context),
                        onRetry = { viewModel.reintentar() }
                    )
                }
            }

            // Recomendaciones por tipo
            if (uiState.recomendaciones.isNotEmpty()) {
                val grupos = listOf(
                    TipoRecomendacion.YOUTUBE to sectionVideos,
                    TipoRecomendacion.LIBRO   to sectionBooks,
                    TipoRecomendacion.RECURSO to sectionWebResources
                )
                var globalIdx = 0
                grupos.forEach { (tipo, titulo) ->
                    val lista = uiState.recomendaciones.filter { it.tipo == tipo }
                    if (lista.isNotEmpty()) {
                        item(key = "header_$tipo") {
                            SectionDivider(tipo = tipo, title = titulo)
                        }
                        itemsIndexed(lista, key = { _, rec -> "${tipo}_${rec.url}" }) { i, rec ->
                            AnimatedRecomendacionCard(
                                recomendacion = rec,
                                index = globalIdx + i,
                                onOpenUrl = { url ->
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        )
                                    }
                                }
                            )
                        }
                        globalIdx += lista.size
                    }
                }

                item {
                    OutlinedButton(
                        onClick = { viewModel.reintentar() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.recs_regenerate))
                    }
                }
            }

            // Placeholder: materia no seleccionada
            if (!uiState.isLoading
                && uiState.recomendaciones.isEmpty()
                && uiState.materiaSeleccionada == null
                && materias.isNotEmpty()
                && !uiState.apiKeyFaltante
            ) {
                item { SelectMateriaHint() }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ── Selector de materias ──────────────────────────────────────

@Composable
private fun MateriaSelector(
    materias: List<Pair<String, Long>>,
    selectedId: Long?,
    onSelect: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionLabel(stringResource(R.string.recs_subject_label).lowercase().replaceFirstChar { it.titlecase() })
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(materias) { (nombre, id) ->
                FilterChip(
                    selected = selectedId == id,
                    onClick = { onSelect(id) },
                    label = {
                        Text(
                            text = nombre,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = if (selectedId == id) {
                        { Icon(Icons.Default.School, null, Modifier.size(FilterChipDefaults.IconSize)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}

// ── Divisor de seccion ────────────────────────────────────────

@Composable
private fun SectionDivider(tipo: TipoRecomendacion, title: String) {
    val meta = tipoMeta(tipo)
    Row(
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(meta.accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(meta.icon, null, tint = meta.accentColor, modifier = Modifier.size(16.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// ── Card de recomendacion ─────────────────────────────────────

// ── Skeleton de carga ─────────────────────────────────────────

// ── Banners de estado ─────────────────────────────────────────

@Composable
private fun LoadingBanner(materia: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                strokeCap = StrokeCap.Round
            )
            Text(
                text = if (materia.isNotBlank()) stringResource(R.string.recs_loading_for, materia) else stringResource(R.string.recs_loading),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun ApiKeyMissingBanner(onOpenSettings: () -> Unit) {
    SurfaceCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.VpnKey, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.recs_key_needed),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.recs_key_setup_steps),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(onClick = onOpenSettings, modifier = Modifier.padding(top = 4.dp)) {
            Text(stringResource(R.string.recs_go_settings))
        }
    }
}

// ── Estados vacíos ────────────────────────────────────────────

@Composable
private fun SelectMateriaHint() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Lightbulb, null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
            Text(
                text = stringResource(R.string.recommendations_select_materia),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.recs_will_generate),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

