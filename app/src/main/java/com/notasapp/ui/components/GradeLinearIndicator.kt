package com.notasapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Barra de progreso lineal animada para mostrar el aporte de un componente.
 *
 * @param progreso  Valor de 0.0 a 1.0.
 * @param color     Color de la barra.
 */
@Composable
fun GradeLinearIndicator(
    progreso: Float,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    trackColor: Color = Color.Unspecified
) {
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(progreso) {
        animatedProgress.animateTo(
            targetValue = progreso.coerceIn(0f, 1f),
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        )
    }

    val barColor = if (color == Color.Unspecified)
        MaterialTheme.colorScheme.primary
    else color
    val bgColor = if (trackColor == Color.Unspecified)
        MaterialTheme.colorScheme.surfaceVariant
    else trackColor

    Canvas(
        modifier = modifier
            .size(height = 8.dp, width = Dp.Unspecified)
    ) {
        drawRoundRect(
            color = bgColor,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f),
        )
        drawRoundRect(
            color = barColor,
            size = androidx.compose.ui.geometry.Size(
                width = size.width * animatedProgress.value,
                height = size.height
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f),
        )
    }
}
