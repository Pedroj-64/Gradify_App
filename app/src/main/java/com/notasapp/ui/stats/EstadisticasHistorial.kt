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

@Composable
internal fun SemesterEvolutionChart(
    semestres: List<Semestre>,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val dotColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)

    // Filter semestres that have a promedio
    val dataSemestres = semestres.filter { it.promedioSimple != null }
    if (dataSemestres.size < 2) return

    val promedios = dataSemestres.map { it.promedioSimple!! }
    val maxVal = promedios.max().coerceAtLeast(5f)
    val minVal = 0f

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.stats_evolution),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(16.dp))

            val chartDescription = stringResource(
                R.string.st_chart_desc,
                dataSemestres.joinToString("; ") { "${it.periodo}: ${"%.2f".format(it.promedioSimple)}" }
            )
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .semantics { contentDescription = chartDescription }
            ) {
                val leftPadding = 40.dp.toPx()
                val rightPadding = 16.dp.toPx()
                val topPadding = 12.dp.toPx()
                val bottomPadding = 32.dp.toPx()

                val chartWidth = size.width - leftPadding - rightPadding
                val chartHeight = size.height - topPadding - bottomPadding
                val range = (maxVal - minVal).coerceAtLeast(1f)

                val pointCount = dataSemestres.size
                val stepX = if (pointCount > 1) chartWidth / (pointCount - 1) else chartWidth

                // Y axis grid lines (4 lines)
                val gridSteps = 4
                for (i in 0..gridSteps) {
                    val yVal = minVal + (range / gridSteps) * i
                    val y = topPadding + chartHeight - (chartHeight * ((yVal - minVal) / range))
                    drawLine(
                        color = gridColor,
                        start = Offset(leftPadding, y),
                        end = Offset(size.width - rightPadding, y),
                        strokeWidth = 1.dp.toPx()
                    )
                    // Y axis label
                    drawContext.canvas.nativeCanvas.drawText(
                        "%.1f".format(yVal),
                        4.dp.toPx(),
                        y + 4.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = textColor.hashCode()
                            textSize = 10.sp.toPx()
                            isAntiAlias = true
                        }
                    )
                }

                // Calculate points
                val points = dataSemestres.mapIndexed { index, semestre ->
                    val x = leftPadding + index * stepX
                    val normalized = ((semestre.promedioSimple!! - minVal) / range).coerceIn(0f, 1f)
                    val y = topPadding + chartHeight - (chartHeight * normalized)
                    Offset(x, y)
                }

                // Fill area under the line
                if (points.size >= 2) {
                    val fillPath = Path().apply {
                        moveTo(points.first().x, topPadding + chartHeight)
                        lineTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                        lineTo(points.last().x, topPadding + chartHeight)
                        close()
                    }
                    drawPath(fillPath, fillColor)
                }

                // Draw line
                if (points.size >= 2) {
                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                    drawPath(
                        linePath,
                        color = lineColor,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // Draw dots and X axis labels
                points.forEachIndexed { index, point ->
                    // Dot
                    drawCircle(
                        color = Color.White,
                        radius = 6.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = dotColor,
                        radius = 4.dp.toPx(),
                        center = point
                    )

                    // X label (semester name)
                    val label = dataSemestres[index].periodo
                    val shortLabel = if (label.length > 7) label.takeLast(7) else label
                    drawContext.canvas.nativeCanvas.drawText(
                        shortLabel,
                        point.x - 16.dp.toPx(),
                        size.height - 4.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = textColor.hashCode()
                            textSize = 9.sp.toPx()
                            isAntiAlias = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
internal fun SemestresHistorialCard(
    semestres: List<Semestre>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.stats_history),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            semestres.forEach { semestre ->
                SemestreRow(semestre = semestre)
            }
        }
    }
}

@Composable
internal fun SemestreRow(semestre: Semestre) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = semestre.periodo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.st_sem_summary, semestre.totalMaterias, semestre.totalCreditos),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = semestre.promedioPonderadoDisplay,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.st_sem_passed, semestre.aprobadas, semestre.materiasConNotas.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
