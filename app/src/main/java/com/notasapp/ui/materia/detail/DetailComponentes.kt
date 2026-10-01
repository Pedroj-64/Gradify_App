package com.notasapp.ui.materia.detail

import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.runtime.MutableState
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicTextField
import com.notasapp.ui.components.gradeStyle
import com.notasapp.ui.components.SurfaceCard
import com.notasapp.ui.components.MenuAction
import com.notasapp.ui.components.RowMenu
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
internal fun ComponenteCard(
    componente: Componente,
    escalaMax: Float,
    onSubNotaValueChange: (Long, Float?) -> Unit,
    onAgregarSubNota: (descripcion: String, porcentaje: Float) -> Unit,
    onEliminarSubNota: (Long) -> Unit,
    onAgregarDetalle: (subNotaId: Long, descripcion: String, porcentaje: Float) -> Unit,
    onActualizarDetalle: (detalleId: Long, valor: Float?) -> Unit,
    onEliminarDetalle: (detalleId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var mostrarDialogAgregar by remember { mutableStateOf(false) }
    val rename = LocalRename.current
    val porcentajeSubNotasUsado = componente.subNotas
        .sumOf { it.porcentajeDelComponente.toDouble() }.toFloat()

    SurfaceCard(modifier = modifier.animateContentSize(tween(250))) {
        // ── Cabecera: nombre, peso y nota del corte ──────────────
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = componente.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${componente.porcentajeDisplay}% del total · ${componente.progresoDisplay}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                AnimatedText(
                    text = componente.promedio?.let { "%.2f".format(it) } ?: "--",
                    style = gradeStyle(26),
                    color = if (componente.promedio == null) MaterialTheme.colorScheme.outline
                    else MaterialTheme.colorScheme.onSurface
                )
                componente.aporteAlFinal?.let { aporte ->
                    Text(
                        text = "aporta %.2f".format(aporte),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            RowMenu(
                items = listOf(
                    MenuAction(stringResource(R.string.rename_action)) {
                        rename(RenameKind.COMPONENTE, componente.id, componente.nombre)
                    }
                )
            )
        }

        Spacer(Modifier.height(12.dp))

        componente.subNotas.forEach { subNota ->
            SubNotaRow(
                subNota = subNota,
                escalaMax = escalaMax,
                onValueChange = { valor -> onSubNotaValueChange(subNota.id, valor) },
                onEliminar = { onEliminarSubNota(subNota.id) },
                onAgregarDetalle = { desc, pct -> onAgregarDetalle(subNota.id, desc, pct) },
                onActualizarDetalle = onActualizarDetalle,
                onEliminarDetalle = onEliminarDetalle
            )
        }

        TextButton(
            onClick = { mostrarDialogAgregar = true },
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.detail_add_grade), style = MaterialTheme.typography.labelLarge)
        }
    }

    if (mostrarDialogAgregar) {
        AgregarSubNotaDialog(
            componenteNombre = componente.nombre,
            porcentajeYaUsado = porcentajeSubNotasUsado,
            onDismiss = { mostrarDialogAgregar = false },
            onAgregar = { desc, pct ->
                onAgregarSubNota(desc, pct)
                mostrarDialogAgregar = false
            }
        )
    }
}

/** Solo dígitos con un decimal opcional (máx. 2): rechaza "NaN", "1e5", letras y signos al teclear. */
internal val GRADE_INPUT = Regex("^\\d{0,3}([.,]\\d{0,2})?$")

/** True si el texto no es una nota válida en 0..[max] (se pinta en rojo y NO se guarda). */
internal fun isGradeInvalid(text: String, max: Float): Boolean {
    if (text.isEmpty()) return false
    val v = text.parseGrade() ?: return true
    return v !in 0f..max
}

/**
 * Texto de un campo de nota. Se sincroniza con el valor guardado SOLO cuando difiere de lo que el
 * usuario ya escribió (p. ej. entrada rápida); así no se reformatea mientras teclea ("4" → "4.0").
 */
@Composable
private fun rememberGradeText(valor: Float?, max: Float, focused: Boolean): MutableState<String> {
    val text = remember { mutableStateOf(valor?.toInputString() ?: "") }
    // Con el foco puesto manda lo que el usuario teclea: un valor guardado "viejo" no debe pisarlo.
    LaunchedEffect(valor, focused) {
        if (!focused && !isGradeInvalid(text.value, max) && text.value.parseGrade() != valor) {
            text.value = valor?.toInputString() ?: ""
        }
    }
    return text
}

/** Campo de nota compacto: relleno tonal, sin contorno, cifra centrada. */
@Composable
internal fun GradeField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    onFocusChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val errorDesc = stringResource(R.string.grade_out_of_range)
    BasicTextField(
        value = value,
        onValueChange = { input -> if (GRADE_INPUT.matches(input)) onValueChange(input) },
        modifier = modifier
            .width(72.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isError) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .onFocusChanged { onFocusChange(it.isFocused) }
            .semantics {
                contentDescription = label
                if (isError) error(errorDesc)
            },
        textStyle = gradeStyle(18, FontWeight.SemiBold).copy(
            color = if (isError) MaterialTheme.colorScheme.onErrorContainer
            else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { inner ->
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (value.isEmpty()) {
                    Text("–", style = gradeStyle(18, FontWeight.Medium), color = MaterialTheme.colorScheme.outline)
                }
                inner()
            }
        }
    )
}

@Composable
internal fun SubNotaRow(
    subNota: SubNota,
    escalaMax: Float,
    onValueChange: (Float?) -> Unit,
    onEliminar: () -> Unit,
    onAgregarDetalle: (descripcion: String, porcentaje: Float) -> Unit,
    onActualizarDetalle: (detalleId: Long, valor: Float?) -> Unit,
    onEliminarDetalle: (detalleId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember(subNota.esCompuesta) { mutableStateOf(subNota.esCompuesta) }
    var mostrarDialogDetalle by remember { mutableStateOf(false) }
    var enfocado by remember { mutableStateOf(false) }
    var textValue by rememberGradeText(subNota.valor, escalaMax, enfocado)
    val porcentajeDetallesUsado = subNota.detalles.sumOf { it.porcentaje.toDouble() }.toFloat()
    val rename = LocalRename.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .let { if (subNota.esCompuesta) it.clickable { expanded = !expanded } else it },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subNota.descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = buildString {
                        append("${(subNota.porcentajeDelComponente * 100).toInt()}% del corte")
                        if (subNota.esCompuesta) append(" · ${subNota.detalles.size} partes")
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (subNota.esCompuesta) {
                Text(
                    text = subNota.valorEfectivo?.let { "%.2f".format(it) } ?: "--",
                    style = gradeStyle(20),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) stringResource(R.string.detail_collapse) else stringResource(R.string.detail_expand),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                GradeField(
                    value = textValue,
                    label = stringResource(R.string.grade_field_desc, subNota.descripcion),
                    isError = isGradeInvalid(textValue, escalaMax),
                    onFocusChange = { enfocado = it },
                    onValueChange = { input ->
                        textValue = input
                        val parsed = input.parseGrade()
                        if (parsed != null && parsed in 0f..escalaMax) {
                            onValueChange(parsed)
                        } else if (input.isEmpty()) {
                            onValueChange(null)
                        }
                    }
                )
            }

            RowMenu(
                items = buildList {
                    add(MenuAction(stringResource(R.string.rename_action)) {
                        rename(RenameKind.SUBNOTA, subNota.id, subNota.descripcion)
                    })
                    if (!subNota.esCompuesta) {
                        add(MenuAction(stringResource(R.string.detail_split)) { mostrarDialogDetalle = true })
                    }
                    add(MenuAction(stringResource(R.string.detail_delete_grade), destructive = true) { onEliminar() })
                }
            )
        }

        // ── Partes de una nota compuesta ──────────────────────
        if (expanded && subNota.esCompuesta) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, bottom = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                subNota.detalles.forEach { detalle ->
                    DetalleRow(
                        detalle = detalle,
                        escalaMax = escalaMax,
                        onValueChange = { valor -> onActualizarDetalle(detalle.id, valor) },
                        onEliminar = { onEliminarDetalle(detalle.id) }
                    )
                }
                TextButton(onClick = { mostrarDialogDetalle = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.detail_add_element), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }

    if (mostrarDialogDetalle) {
        AgregarDetalleDialog(
            subNotaNombre = subNota.descripcion,
            porcentajeYaUsado = porcentajeDetallesUsado,
            onDismiss = { mostrarDialogDetalle = false },
            onAgregar = { desc, pct ->
                onAgregarDetalle(desc, pct)
                mostrarDialogDetalle = false
                expanded = true
            }
        )
    }
}

@Composable
internal fun DetalleRow(
    detalle: SubNotaDetalle,
    escalaMax: Float,
    onValueChange: (Float?) -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var enfocado by remember { mutableStateOf(false) }
    var textValue by rememberGradeText(detalle.valor, escalaMax, enfocado)
    val rename = LocalRename.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = detalle.descripcion, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                text = "${kotlin.math.round(detalle.porcentaje * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        GradeField(
            value = textValue,
            label = stringResource(R.string.grade_field_desc, detalle.descripcion),
            isError = isGradeInvalid(textValue, escalaMax),
            onFocusChange = { enfocado = it },
            onValueChange = { input ->
                textValue = input
                val parsed = input.parseGrade()
                if (parsed != null && parsed in 0f..escalaMax) {
                    onValueChange(parsed)
                } else if (input.isEmpty()) {
                    onValueChange(null)
                }
            }
        )
        RowMenu(
            items = listOf(
                MenuAction(stringResource(R.string.rename_action)) {
                    rename(RenameKind.DETALLE, detalle.id, detalle.descripcion)
                },
                MenuAction(stringResource(R.string.btn_delete), destructive = true) { onEliminar() }
            )
        )
    }
}
