package com.notasapp.ui.components

import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notasapp.R

/*
 * Sistema de diseño de Gradify.
 *
 *  - Una sola marca (azul). El color de estado (verde / ámbar / rojo) se usa SOLO en
 *    números y puntos, nunca para pintar tarjetas enteras.
 *  - Superficies tonales planas: sin sombras ni bordes; la jerarquía la da el espacio.
 *  - Números grandes y tabulares (las notas son lo que el usuario viene a leer).
 *  - Encabezado único a la izquierda en todas las pantallas.
 */

/** Estado académico → color semántico del tema. */
enum class Tone { OK, WARN, BAD, NONE }

@Composable
fun Tone.color(): Color = when (this) {
    Tone.OK -> MaterialTheme.colorScheme.secondary
    Tone.WARN -> MaterialTheme.colorScheme.tertiary
    Tone.BAD -> MaterialTheme.colorScheme.error
    Tone.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
}

/** Cifras de notas: negrita, anchos tabulares para que no "bailen" al cambiar. */
@Composable
fun gradeStyle(size: Int, weight: FontWeight = FontWeight.Bold): TextStyle =
    MaterialTheme.typography.headlineLarge.copy(
        fontSize = size.sp,
        lineHeight = (size * 1.1f).sp,
        fontWeight = weight,
        letterSpacing = (-0.02f * size).sp,
        fontFeatureSettings = "tnum"
    )

/**
 * Encabezado de pantalla: título grande alineado a la izquierda, subtítulo opcional,
 * flecha "atrás" solo cuando [onBack] != null (las pestañas no la llevan).
 */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = if (onBack == null) 20.dp else 4.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.btn_back))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp, lineHeight = 34.sp),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        actions()
    }
}

/** Superficie tonal plana, sin sombra ni borde. */
@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    container: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(container)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(18.dp),
        content = content
    )
}

/** Rótulo de sección en minúsculas y sobrio (nada de MAYÚSCULAS ni color de marca). */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(start = 4.dp, top = 8.dp),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** Punto + texto de estado. Sin fondo de píldora. */
@Composable
fun StatusLabel(text: String, tone: Tone, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(tone.color())
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = tone.color()
        )
    }
}

/** Separador de pares etiqueta / valor en una sola línea. */
@Composable
fun StatPair(value: String, label: String, modifier: Modifier = Modifier, tone: Tone = Tone.NONE) {
    Column(modifier = modifier) {
        Text(
            text = value,
            style = gradeStyle(24),
            color = if (tone == Tone.NONE) MaterialTheme.colorScheme.onSurface else tone.color()
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RowSpacer(width: Int = 12) = Spacer(Modifier.width(width.dp))

@Composable
fun FullWidthColumn(modifier: Modifier = Modifier, spacing: Int = 12, content: @Composable ColumnScope.() -> Unit) =
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spacing.dp), content = content)

class MenuAction(val label: String, val destructive: Boolean = false, val onClick: () -> Unit)

/** Menú ⋮ de una fila: reemplaza la fila de íconos (lápiz, papelera, expandir…). */
@Composable
fun RowMenu(items: List<MenuAction>) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }, modifier = Modifier.size(48.dp)) {   // mínimo táctil de accesibilidad
            Icon(
                Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.more_options),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            item.label,
                            color = if (item.destructive) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = { open = false; item.onClick() }
                )
            }
        }
    }
}


/** Chips seleccionables en el color de marca (el predeterminado de M3 es verde). */
@Composable
fun gradifyChipColors() = androidx.compose.material3.FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
)
