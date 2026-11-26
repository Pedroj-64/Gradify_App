@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.notasapp.ui.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.semantics.Role
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.TextButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.vector.ImageVector
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

/** Fondo suave del tono, para tarjetas de resultado y avisos. */
@Composable
fun Tone.container(): Color = when (this) {
    Tone.OK -> MaterialTheme.colorScheme.secondaryContainer
    Tone.WARN -> MaterialTheme.colorScheme.tertiaryContainer
    Tone.BAD -> MaterialTheme.colorScheme.errorContainer
    Tone.NONE -> MaterialTheme.colorScheme.primaryContainer
}

@Composable
fun Tone.onContainer(): Color = when (this) {
    Tone.OK -> MaterialTheme.colorScheme.onSecondaryContainer
    Tone.WARN -> MaterialTheme.colorScheme.onTertiaryContainer
    Tone.BAD -> MaterialTheme.colorScheme.onErrorContainer
    Tone.NONE -> MaterialTheme.colorScheme.onPrimaryContainer
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
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(container)
            .let {
                when {
                    onLongClick != null -> it.combinedClickable(
                        role = Role.Button,
                        onLongClickLabel = onLongClickLabel,
                        onLongClick = onLongClick,
                        onClick = onClick ?: {}
                    )
                    onClick != null -> it.clickable(role = Role.Button, onClick = onClick)
                    else -> it
                }
            }
            .padding(18.dp),
        content = content
    )
}

/** Rótulo de sección en color de marca, con una barrita de acento a la izquierda. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(start = 4.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(width = 4.dp, height = 16.dp).clip(CircleShape).background(BrandGradient))
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/** Píldora de estado: fondo suave del tono, punto y texto. */
@Composable
fun StatusLabel(text: String, tone: Tone, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(tone.container())
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(tone.onContainer())
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = tone.onContainer()
        )
    }
}

// ── Identidad: degradado de marca y color por materia ──────────────────────

/** Azul Gradify → violeta → rosa Axo. Fijo en claro, oscuro y Material You: es la firma de la app. */
val BrandGradient = androidx.compose.ui.graphics.Brush.linearGradient(
    listOf(Color(0xFF2B59C3), Color(0xFF6F4BD1), Color(0xFFC9508A))
)

/**
 * Paleta de materias: tonos medios que llevan texto blanco con contraste AA y se ven en claro y oscuro.
 * ponytail: se elige por id (estable, sin migración); si el usuario quiere elegirlo, columna `colorIndex` en Room.
 */
val MateriaPalette = listOf(
    Color(0xFF2B59C3), Color(0xFF6F4BD1), Color(0xFFB8407A), Color(0xFF0F7C7C),
    Color(0xFFC0570C), Color(0xFF2E7D32), Color(0xFF3949AB), Color(0xFF8E3FA8)
)

fun materiaColor(id: Long): Color = MateriaPalette[(id % MateriaPalette.size).toInt().let { if (it < 0) -it else it }]

/** Círculo con la inicial de la materia en su color. */
@Composable
fun MateriaAvatar(nombre: String, id: Long, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 44.dp) {
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(materiaColor(id)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = nombre.trim().firstOrNull()?.uppercase() ?: "·",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Tarjeta protagonista con el degradado de marca y texto blanco. */
@Composable
fun HeroCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(BrandGradient)
            .padding(22.dp),
        content = content
    )
}

/** Píldora translúcida para usar sobre [HeroCard]. */
@Composable
fun HeroPill(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        color = Color.White,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold
    )
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

// ── Axo, la mascota ─────────────────────────────────────────────────────────

/** Estados de ánimo de Axo (dibujos en `res/drawable/axo_*.xml`, generados por `docs/assets/axo/gen.py`). */
enum class AxoMood(@androidx.annotation.DrawableRes val res: Int) {
    FELIZ(R.drawable.axo_feliz),
    TRANQUI(R.drawable.axo_tranqui),
    PREOCUPADO(R.drawable.axo_preocupado),
    ESTUDIANDO(R.drawable.axo_estudiando)
}

/** Axo decorativo: el texto de al lado ya dice lo importante, así que TalkBack lo ignora. */
@Composable
fun Axo(mood: AxoMood, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 72.dp) {
    androidx.compose.foundation.Image(
        painter = androidx.compose.ui.res.painterResource(mood.res),
        contentDescription = null,
        modifier = modifier.size(size)
    )
}

// ── Estados y contenedores compartidos ──────────────────────────────────────

/** Estado vacío: icono tonal, título, pista y acción opcional. */
@Composable
fun EmptyState(
    icon: ImageVector?,
    title: String,
    hint: String? = null,
    modifier: Modifier = Modifier,
    mood: AxoMood? = null,
    action: (@Composable () -> Unit)? = null
) {
    Box(modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (mood != null) Axo(mood, size = 112.dp)
            else if (icon != null) Icon(icon, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            if (hint != null) {
                Text(hint, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
            if (action != null) {
                Spacer(Modifier.height(8.dp))
                action()
            }
        }
    }
}

/** Aviso de error en superficie tonal; el reintento es opcional. */
@Composable
fun ErrorBanner(message: String, modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null) {
    SurfaceCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(Tone.BAD.color()))
            Text(message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            if (onRetry != null) {
                TextButton(onClick = onRetry) { Text(stringResource(R.string.recommendations_retry)) }
            }
        }
    }
}

/** Resultado de un cálculo: icono con el color del estado, título y detalle. */
@Composable
fun ResultCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tone: Tone,
    modifier: Modifier = Modifier,
    mood: AxoMood? = null
) {
    SurfaceCard(modifier = modifier, container = tone.container()) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (mood != null) Axo(mood, size = 56.dp)
            else Icon(icon, contentDescription = null, tint = tone.onContainer(), modifier = Modifier.size(28.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = tone.onContainer())
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = tone.onContainer().copy(alpha = 0.85f))
            }
        }
    }
}

/** Indicador de carga centrado con texto opcional (anunciado por TalkBack). */
@Composable
fun LoadingBlock(modifier: Modifier = Modifier, text: String? = null) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
        if (text != null) Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Diálogo estándar: título, contenido, confirmar y cancelar con los mismos estilos en toda la app. */
@Composable
fun GradifyDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String? = stringResource(R.string.btn_cancel),
    confirmEnabled: Boolean = true,
    destructive: Boolean = false,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp), content = content) },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = confirmEnabled) {
                Text(confirmText, color = if (destructive) MaterialTheme.colorScheme.error else Color.Unspecified)
            }
        },
        dismissButton = dismissText?.let { { TextButton(onClick = onDismiss) { Text(it) } } }
    )
}

/** Bottom sheet estándar: mismo asa y color. El scroll lo maneja cada contenido (no anidar verticalScroll aquí). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradifySheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            content = content
        )
    }
}
