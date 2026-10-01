package com.notasapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import com.notasapp.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Tipografía de la app siguiendo el sistema Material Design 3.
 *
 * Se usa la fuente predeterminada del sistema para mejor rendimiento
 * y coherencia con el dispositivo del usuario.
 */
private val BaseTypography = Typography(
    // Títulos grandes (ej: nombre de la materia en detalle)
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    // Títulos de sección (ej: nombre del componente)
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    // Cuerpo de texto (ej: descripción de sub-nota)
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    // Etiquetas (ej: badge de porcentaje)
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp
    )
)

/** Plus Jakarta Sans (OFL), fuente variable: un solo archivo para todos los pesos. */
@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
val GradifyFontFamily = FontFamily(
    listOf(400, 500, 600, 700, 800).map { w ->
        Font(
            R.font.plus_jakarta_sans,
            weight = FontWeight(w),
            variationSettings = FontVariation.Settings(FontVariation.weight(w))
        )
    }
)

/** Tipografía de Gradify: la base Material 3 completa con la fuente de la marca. */
val NotasTypography: Typography = with(BaseTypography) {
    copy(
        displayLarge = displayLarge.copy(fontFamily = GradifyFontFamily),
        displayMedium = displayMedium.copy(fontFamily = GradifyFontFamily),
        displaySmall = displaySmall.copy(fontFamily = GradifyFontFamily),
        headlineLarge = headlineLarge.copy(fontFamily = GradifyFontFamily),
        headlineMedium = headlineMedium.copy(fontFamily = GradifyFontFamily),
        headlineSmall = headlineSmall.copy(fontFamily = GradifyFontFamily),
        titleLarge = titleLarge.copy(fontFamily = GradifyFontFamily),
        titleMedium = titleMedium.copy(fontFamily = GradifyFontFamily),
        titleSmall = titleSmall.copy(fontFamily = GradifyFontFamily),
        bodyLarge = bodyLarge.copy(fontFamily = GradifyFontFamily),
        bodyMedium = bodyMedium.copy(fontFamily = GradifyFontFamily),
        bodySmall = bodySmall.copy(fontFamily = GradifyFontFamily),
        labelLarge = labelLarge.copy(fontFamily = GradifyFontFamily),
        labelMedium = labelMedium.copy(fontFamily = GradifyFontFamily),
        labelSmall = labelSmall.copy(fontFamily = GradifyFontFamily)
    )
}
