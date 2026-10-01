package com.notasapp.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Marca Gradify ─────────────────────────────────────────────────
// Azul académico + verde "aprobado" + ámbar "en riesgo". Fijos en todos los celulares;
// Material You es opcional (Ajustes → Apariencia).
val PrimaryBlue = Color(0xFF2B59C3)
val PrimaryLight = Color(0xFFB2C5FF)
val SecondaryGreen = Color(0xFF1B7F4F)
val SecondaryLightGreen = Color(0xFF7FDBA6)
val ErrorRed = Color(0xFFBA1A1A)
val WarningAmber = Color(0xFFB26A00)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE5FF),
    onPrimaryContainer = Color(0xFF00164F),
    secondary = SecondaryGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC3F0D6),
    onSecondaryContainer = Color(0xFF00210F),
    tertiary = WarningAmber,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDDB3),
    onTertiaryContainer = Color(0xFF2A1700),
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF6F8FC),
    onBackground = Color(0xFF181B22),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF181B22),
    surfaceVariant = Color(0xFFE1E5F0),
    onSurfaceVariant = Color(0xFF444953),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF1F4FA),
    surfaceContainer = Color(0xFFEBEFF7),
    surfaceContainerHigh = Color(0xFFE5E9F2),
    surfaceContainerHighest = Color(0xFFDFE3EC),
    outline = Color(0xFF747985),
    outlineVariant = Color(0xFFC4C8D4)
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = Color(0xFF002A78),
    primaryContainer = Color(0xFF1B418F),
    onPrimaryContainer = Color(0xFFDCE5FF),
    secondary = SecondaryLightGreen,
    onSecondary = Color(0xFF00391E),
    secondaryContainer = Color(0xFF00522E),
    onSecondaryContainer = Color(0xFFC3F0D6),
    tertiary = Color(0xFFFFB866),
    onTertiary = Color(0xFF462A00),
    tertiaryContainer = Color(0xFF653D00),
    onTertiaryContainer = Color(0xFFFFDDB3),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF10131A),
    onBackground = Color(0xFFE2E4ED),
    surface = Color(0xFF10131A),
    onSurface = Color(0xFFE2E4ED),
    surfaceVariant = Color(0xFF444953),
    onSurfaceVariant = Color(0xFFC4C8D4),
    surfaceContainerLowest = Color(0xFF0B0E14),
    surfaceContainerLow = Color(0xFF181B22),
    surfaceContainer = Color(0xFF1C1F27),
    surfaceContainerHigh = Color(0xFF272A32),
    surfaceContainerHighest = Color(0xFF32353D),
    outline = Color(0xFF8E919D),
    outlineVariant = Color(0xFF444953)
)

private val GradifyShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * Tema principal de NotasApp.
 *
 * Soporta:
 * - Dynamic Color (Material You) en Android 12+
 * - Modo oscuro automático según la configuración del sistema
 * - Colores fijos en versiones anteriores de Android
 */
@Composable
fun NotasAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,  // Material You (Android 12+), opcional en Ajustes
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Adaptar barra de estado al color del tema
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NotasTypography,
        shapes = GradifyShapes,
        content = content
    )
}
