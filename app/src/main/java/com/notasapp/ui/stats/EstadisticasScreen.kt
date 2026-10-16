package com.notasapp.ui.stats

import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.notasapp.ui.components.EmptyState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import com.notasapp.ui.components.gradeStyle
import com.notasapp.ui.components.color
import com.notasapp.ui.components.Tone
import com.notasapp.ui.components.StatPair
import com.notasapp.ui.components.SurfaceCard
import com.notasapp.ui.components.ScreenHeader
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notasapp.R
import com.notasapp.domain.model.Materia
import com.notasapp.domain.model.Semestre
import kotlinx.coroutines.delay

/** CompositionLocal holder to pass stats down without threading params. */
private val LocalStatsHolder = androidx.compose.runtime.staticCompositionLocalOf<EstadisticasSemestre?> { null }

/**
 * Pantalla de estadísticas del semestre.
 *
 * Muestra un resumen visual del desempeño en todas las materias:
 * gauge de promedio general, contadores por estado (aprobado / en riesgo / reprobado),
 * y listas de mejores/peores materias.
 *
 * @param onNavigateBack  Callback para volver atrás.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    onNavigateBack: () -> Unit,
    viewModel: EstadisticasViewModel = hiltViewModel()
) {
    val stats by viewModel.estadisticas.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            ScreenHeader(
                title = stringResource(R.string.stats_title),
                subtitle = stringResource(R.string.stats_subtitle)
            )
        }
    ) { paddingValues ->
        if (stats.totalMaterias == 0) {
            EmptyState(
                icon = Icons.Default.BarChart,
                title = stringResource(R.string.stats_empty),
                hint = stringResource(R.string.stats_empty_hint),
                modifier = Modifier.padding(paddingValues).fillMaxSize()
            )
        } else {
            EstadisticasContent(
                stats = stats,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

// ── Contenido principal ──────────────────────────────────────────────────────

@Composable
private fun EstadisticasContent(
    stats: EstadisticasSemestre,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(80)
        visible = true
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalStatsHolder provides stats) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Gauge + promedio general
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -it / 3 }
            ) {
                PromedioGeneralCard(
                    promedioGeneral = stats.promedioGeneral,
                    totalMaterias = stats.totalMaterias
                )
            }
        }

        // Contadores de estado
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -it / 3 }
            ) {
                EstadoGrid(
                    aprobadas = stats.aprobadas,
                    enRiesgo = stats.enRiesgo,
                    reprobadas = stats.reprobadas,
                    sinNotas = stats.sinNotas
                )
            }
        }

        // Top materias
        if (stats.materiasMejorNota.isNotEmpty()) {
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -it / 3 }
                ) {
                    MateriaRankingCard(
                        titulo = stringResource(R.string.stats_best),
                        materias = stats.materiasMejorNota,
                        esPositivo = true
                    )
                }
            }
        }

        if (stats.materiasPeorNota.any { !it.aprobado }) {
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { -it / 3 }
                ) {
                    MateriaRankingCard(
                        titulo = stringResource(R.string.stats_needs_attention),
                        materias = stats.materiasPeorNota.filter { !it.aprobado },
                        esPositivo = false
                    )
                }
            }
        }

        // Gráfico de evolución por semestre (línea)
        if (stats.semestres.size > 1) {
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(850)) + slideInVertically(tween(850)) { -it / 3 }
                ) {
                    SemesterEvolutionChart(semestres = stats.semestres)
                }
            }
        }

        // Historial por semestre
        if (stats.semestres.size > 1) {
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(900)) + slideInVertically(tween(900)) { -it / 3 }
                ) {
                    SemestresHistorialCard(semestres = stats.semestres)
                }
            }
        }
    }
    } // CompositionLocalProvider
}

// ── Promedio general (protagonista, sin tarjeta) ─────────────────────────────

@Composable
private fun PromedioGeneralCard(
    promedioGeneral: Float?,
    totalMaterias: Int,
    modifier: Modifier = Modifier
) {
    val stats = LocalStatsHolder.current
    val escala = stats?.materiasMejorNota?.firstOrNull()?.escalaMax
        ?: stats?.materiasPeorNota?.firstOrNull()?.escalaMax ?: 5f

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 6.dp)) {
        Text(
            text = stringResource(R.string.stats_general_avg),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = promedioGeneral?.let { "%.2f".format(it) } ?: "--",
                style = gradeStyle(64)
            )
            Text(
                text = " / ${escala.toInt()}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            StatPair(value = totalMaterias.toString(), label = stringResource(R.string.stats_subjects_label))
            if (stats?.promedioPonderado != null) {
                StatPair(value = "%.2f".format(stats.promedioPonderado), label = stringResource(R.string.stats_weighted))
            }
            if (stats != null && stats.totalCreditos > 0) {
                StatPair(value = "${stats.creditosAprobados}/${stats.totalCreditos}", label = stringResource(R.string.stats_credits))
            }
        }
    }
}

// ── Distribución de estados: una barra segmentada + leyenda ──────────────────

@Composable
private fun EstadoGrid(
    aprobadas: Int,
    enRiesgo: Int,
    reprobadas: Int,
    sinNotas: Int,
    modifier: Modifier = Modifier
) {
    val total = (aprobadas + enRiesgo + reprobadas + sinNotas).coerceAtLeast(1)
    val rows = listOf(
        Triple(stringResource(R.string.stats_passing), aprobadas, Tone.OK),
        Triple(stringResource(R.string.stats_at_risk), enRiesgo, Tone.WARN),
        Triple(stringResource(R.string.stats_failing), reprobadas, Tone.BAD),
        Triple(stringResource(R.string.stats_no_grades), sinNotas, Tone.NONE)
    )
    SurfaceCard(modifier = modifier) {
        Text(
            stringResource(R.string.stats_subject_status),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            rows.filter { it.second > 0 }.forEach { (_, n, tone) ->
                Box(
                    Modifier
                        .weight(n.toFloat() / total)
                        .fillMaxHeight()
                        .background(if (tone == Tone.NONE) MaterialTheme.colorScheme.outline else tone.color())
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        rows.forEach { (label, n, tone) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (tone == Tone.NONE) MaterialTheme.colorScheme.outline else tone.color())
                )
                Spacer(Modifier.width(10.dp))
                Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text(n.toString(), style = gradeStyle(18))
            }
        }
    }
}

// ── Ranking de materias ──────────────────────────────────────────────────────

@Composable
private fun MateriaRankingCard(
    titulo: String,
    materias: List<Materia>,
    esPositivo: Boolean,
    modifier: Modifier = Modifier
) {
    SurfaceCard(modifier = modifier) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        materias.forEachIndexed { index, materia ->
            MateriaRankingRow(materia = materia, rank = index + 1, esPositivo = esPositivo)
        }
    }
}

@Composable
private fun MateriaRankingRow(
    materia: Materia,
    rank: Int,
    esPositivo: Boolean
) {
    val promedio = materia.promedio ?: return
    val progress = materia.fraccionEscala(promedio)
    val tone = if (materia.aprobado) Tone.OK else Tone.BAD

    Column(Modifier.fillMaxWidth().padding(vertical = 7.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.width(22.dp)
            )
            Text(
                text = materia.nombre,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Text(text = "%.2f".format(promedio), style = gradeStyle(18), color = tone.color())
        }
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .padding(start = 22.dp)
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(progress)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(tone.color())
            )
        }
    }
}

// ── Gráfico de evolución por semestre (línea) ─────────────────────────────────

// ── Historial de semestres ───────────────────────────────────────────────────

// ── Estado vacío ─────────────────────────────────────────────────────────────

