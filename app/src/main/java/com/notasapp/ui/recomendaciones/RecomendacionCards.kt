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

internal data class TipoMeta(
    val icon: ImageVector,
    val accentColor: Color,
    val label: String
)

@Composable
internal fun tipoMeta(tipo: TipoRecomendacion): TipoMeta = when (tipo) {
    TipoRecomendacion.YOUTUBE -> TipoMeta(
        icon = Icons.Default.PlayCircle,
        accentColor = MaterialTheme.colorScheme.primary,
        label = stringResource(R.string.recs_video)
    )
    TipoRecomendacion.LIBRO -> TipoMeta(
        icon = Icons.Default.AutoStories,
        accentColor = MaterialTheme.colorScheme.tertiary,
        label = stringResource(R.string.recs_book)
    )
    TipoRecomendacion.RECURSO -> TipoMeta(
        icon = Icons.Default.Language,
        accentColor = MaterialTheme.colorScheme.secondary,
        label = stringResource(R.string.recs_web)
    )
}

@Composable
internal fun AnimatedRecomendacionCard(
    recomendacion: Recomendacion,
    index: Int,
    onOpenUrl: (String) -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(minOf(index, 8) * 60L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = tween(280, easing = androidx.compose.animation.core.FastOutSlowInEasing)
        ) { it / 3 } + fadeIn(tween(280))
    ) {
        RecomendacionCard(recomendacion = recomendacion, onOpenUrl = onOpenUrl)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecomendacionCard(
    recomendacion: Recomendacion,
    onOpenUrl: (String) -> Unit
) {
    val meta = tipoMeta(recomendacion.tipo)

    Card(
        onClick = { onOpenUrl(recomendacion.url) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Acento lateral de color
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
                    .background(meta.accentColor)
            )

            Spacer(Modifier.width(12.dp))

            // Icono tipo en caja redonda
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(meta.accentColor.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(meta.icon, null, tint = meta.accentColor, modifier = Modifier.size(20.dp))
            }

            Spacer(Modifier.width(12.dp))

            // Texto
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 14.dp)
            ) {
                Text(
                    text = recomendacion.titulo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                recomendacion.autor?.takeIf { it.isNotBlank() }?.let { autor ->
                    Text(
                        text = autor,
                        style = MaterialTheme.typography.labelSmall,
                        color = meta.accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }
                Text(
                    text = recomendacion.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Flecha "abrir"
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = stringResource(R.string.recs_open),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
internal fun RecomendacionCardSkeleton(index: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer_$index")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, delayMillis = index * 160, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset_$index"
    )
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            MaterialTheme.colorScheme.surfaceVariant
        ),
        start = Offset(shimmerOffset, 0f),
        end = Offset(shimmerOffset + 300f, 200f)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(Modifier.width(4.dp).fillMaxHeight().background(shimmerBrush))
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(shimmerBrush)
            )
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f).padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(Modifier.fillMaxWidth(0.75f).height(14.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
                Box(Modifier.fillMaxWidth(0.45f).height(10.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
                Box(Modifier.fillMaxWidth(0.90f).height(10.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
            }
            Spacer(Modifier.width(16.dp))
        }
    }
}
