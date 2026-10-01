package com.notasapp.ui.materia.detail

import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.BoxWithConstraints
import com.notasapp.ui.components.gradeStyle
import com.notasapp.ui.components.color
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
internal fun PromedioResumen(
    materia: Materia,
    modifier: Modifier = Modifier
) {
    val tone = when {
        materia.promedio == null -> Tone.NONE
        materia.aprobado -> Tone.OK
        else -> Tone.BAD
    }
    val estado = when {
        materia.promedio == null -> stringResource(R.string.home_not_evaluated)
        materia.aprobado -> stringResource(R.string.estado_aprobado)
        else -> stringResource(R.string.estado_en_riesgo)
    }.lowercase().replaceFirstChar { it.titlecase() }

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 6.dp)) {
        Text(
            text = stringResource(R.string.promedio_actual),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.Bottom) {
            AnimatedText(
                text = materia.promedioDisplay,
                style = gradeStyle(64),
                color = if (tone == Tone.NONE) MaterialTheme.colorScheme.outline else tone.color()
            )
            Text(
                text = " / ${materia.escalaMax.toInt()}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }
        Spacer(Modifier.height(2.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            StatusLabel(estado, tone)
            Text(
                text = "Mínimo ${materia.notaAprobacion}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(18.dp))

        GradeTrack(
            promedio = materia.promedio,
            aprobacion = materia.notaAprobacion,
            max = materia.escalaMax,
            tone = tone
        )

        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "Evaluado ${kotlin.math.round(materia.porcentajeEvaluado * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Acumulado ${materia.acumuladoDisplay}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ── Un solo recuadro de lectura: qué necesito + ánimo ─────
        val necesita = materia.notaNecesariaParaAprobar
        val mensaje = getMensajeMotivacional(materia)?.second
        val felicitacion = if (materia.yaAprobo && !materia.completa)
            "Ya superaste el mínimo de ${materia.notaAprobacion}. Llevas ${materia.acumuladoDisplay} acumulado."
        else null
        if (necesita != null || mensaje != null || felicitacion != null) {
            Spacer(Modifier.height(18.dp))
            SurfaceCard(modifier = Modifier.animateContentSize()) {
                if (necesita != null) {
                    Text(
                        text = "≈ ${"%.2f".format(necesita)}",
                        style = gradeStyle(28),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "es lo que necesitas en promedio en lo que falta para aprobar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val texto = felicitacion ?: mensaje
                if (texto != null) {
                    if (necesita != null) Spacer(Modifier.height(10.dp))
                    Text(text = texto, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

/** Barra de 0 a la nota máxima con una marca en la nota mínima de aprobación. */
@Composable
private fun GradeTrack(promedio: Float?, aprobacion: Float, max: Float, tone: Tone) {
    val fill = ((promedio ?: 0f) / max).coerceIn(0f, 1f)
    val mark = (aprobacion / max).coerceIn(0f, 1f)
    BoxWithConstraints(Modifier.fillMaxWidth().height(14.dp)) {
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        )
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(fill)
                .height(8.dp)
                .clip(CircleShape)
                .background(if (tone == Tone.NONE) MaterialTheme.colorScheme.outline else tone.color())
        )
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .offset(x = maxWidth * mark - 1.dp)
                .width(2.dp)
                .height(14.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        )
    }
}

/**
 * Devuelve un par (emoji, mensaje) motivacional según el progreso del estudiante.
 * Null si no hay notas ingresadas aún.
 */
internal fun getMensajeMotivacional(materia: Materia): Pair<String, String>? {
    val pct = materia.porcentajeEvaluado

    // Sin notas todavía
    if (pct == 0f || materia.promedio == null) return null

    // Ya completó todo
    if (materia.completa) {
        return if (materia.aprobado) {
            "🏆" to "¡Completaste todas las evaluaciones y aprobaste! Excelente semestre."
        } else {
            "📋" to "Completaste todas las evaluaciones. Revisa las áreas donde puedes mejorar."
        }
    }

    // Ya aprobó antes de terminar
    if (materia.yaAprobo) return null // El banner de felicitación ya maneja esto

    // Mensajes según porcentaje evaluado y rendimiento
    val rendimiento = materia.promedio!! / materia.escalaMax // 0.0 – 1.0
    val aprobacionRatio = materia.notaAprobacion / materia.escalaMax

    return when {
        // Primer corte (0-25%)
        pct <= 0.25f -> when {
            rendimiento >= aprobacionRatio * 1.2f ->
                "🚀" to "¡Gran inicio! Arrancaste con todo. Mantén ese ritmo y el semestre será tuyo."
            rendimiento >= aprobacionRatio ->
                "💪" to "Buen comienzo, vas por buen camino. ¡Sigue así y cada vez será más fácil!"
            else ->
                "📚" to "Es solo el inicio, hay mucho camino por recorrer. ¡Cada nota cuenta, tú puedes!"
        }
        // Segundo corte (25-50%)
        pct <= 0.50f -> when {
            rendimiento >= aprobacionRatio * 1.2f ->
                "⭐" to "¡Llevas un rendimiento excelente! Ya vas por la mitad, sigue destacándote."
            rendimiento >= aprobacionRatio ->
                "📈" to "Vas bien, ya pasaste la primera mitad. ¡Mantén el enfoque y lo lograrás!"
            else ->
                "🔥" to "Aún puedes remontar, queda bastante por evaluar. ¡Enfócate en lo que viene!"
        }
        // Tercer corte (50-75%)
        pct <= 0.75f -> when {
            rendimiento >= aprobacionRatio * 1.2f ->
                "🌟" to "¡Vas volando! Más de la mitad evaluada y tu rendimiento es sobresaliente."
            rendimiento >= aprobacionRatio ->
                "✨" to "¡Ya se ve la meta! Mantén la concentración en la recta final."
            else ->
                "💡" to "Queda poco, pero cada punto cuenta. ¡Da lo mejor de ti en lo que resta!"
        }
        // Recta final (75-99%)
        else -> when {
            rendimiento >= aprobacionRatio * 1.2f ->
                "🎯" to "¡Casi terminas y vas increíble! Un último empujón para cerrar con broche de oro."
            rendimiento >= aprobacionRatio ->
                "🏁" to "¡Ya casi llegas! Falta poco para terminar el semestre con éxito."
            else ->
                "⚡" to "El último tramo es clave. ¡Concéntrate y da todo, aún es posible!"
        }
    }
}

