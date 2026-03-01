package com.notasapp.ui.materia.detail

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notasapp.domain.model.Componente
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

/**
 * Pantalla de detalle de una materia.
 *
 * Muestra el gauge animado del promedio, componentes con sub-notas editables,
 * shimmer skeleton durante la carga inicial, y un FAB para abrir la
 * calculadora "¿qué nota necesito?".
 *
 * @param materiaId           ID de la materia a mostrar.
 * @param onBack              Callback para volver atrás.
 * @param onEditPorcentajes   Abre la pantalla de edición de porcentajes.
 * @param onExport            Abre la pantalla de exportación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MateriaDetailScreen(
    materiaId: Long,
    onBack: () -> Unit,
    onEditPorcentajes: () -> Unit,
    onExport: () -> Unit,
    viewModel: MateriaDetailViewModel = hiltViewModel()
) {
    val materia by viewModel.materia.collectAsState()
    val error by viewModel.error.collectAsState()
    val notaNecesariaResult by viewModel.notaNecesariaResult.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // ── Bottom sheet de calculadora ───────────────────────────
    var showCalculadora by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = materia?.nombre ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = onEditPorcentajes) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar porcentajes")
                    }
                    IconButton(onClick = onExport) {
                        Icon(Icons.Default.Share, contentDescription = "Exportar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (materia != null) {
                FloatingActionButton(
                    onClick = { showCalculadora = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "¿Qué nota necesito?",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        if (materia == null) {
            // --- Shimmer skeleton ---
            MateriaDetailShimmer(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            materia?.let { mat ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        PromedioResumen(materia = mat)
                    }
                    items(mat.componentes, key = { it.id }) { componente ->
                        ComponenteCard(
                            componente = componente,
                            escalaMax = mat.escalaMax,
                            onSubNotaValueChange = { subNotaId, valor ->
                                viewModel.actualizarSubNota(subNotaId, valor)
                            },
                            onAgregarSubNota = { desc, pct ->
                                viewModel.agregarSubNota(componente.id, desc, pct)
                            },
                            onEliminarSubNota = { subNotaId ->
                                viewModel.eliminarSubNota(subNotaId)
                            },
                            onAgregarDetalle = { subNotaId, desc, pct ->
                                viewModel.agregarDetalle(subNotaId, desc, pct)
                            },
                            onActualizarDetalle = { detalleId, valor ->
                                viewModel.actualizarDetalle(detalleId, valor)
                            },
                            onEliminarDetalle = { detalleId ->
                                viewModel.eliminarDetalle(detalleId)
                            }
                        )
                    }
                }
            }
        }
    }

    // ── Calculadora bottom sheet ──────────────────────────────
    if (showCalculadora) {
        materia?.let { mat ->
            CalculadoraBottomSheet(
                escalaMax = mat.escalaMax,
                resultado = notaNecesariaResult,
                onCalcular = { meta ->
                    viewModel.calcularNotaNecesaria(meta)
                },
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showCalculadora = false
                        viewModel.clearCalculadora()
                    }
                },
                sheetState = sheetState
            )
        }
    }
}

// ── Componentes internos ──────────────────────────────────────

@Composable
private fun PromedioResumen(
    materia: Materia,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // ── Card principal de promedio ────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (materia.aprobado)
                    MaterialTheme.colorScheme.secondaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gauge animado
                PromedioGauge(
                    promedio = materia.promedio ?: 0f,
                    escalaMin = materia.escalaMin,
                    escalaMax = materia.escalaMax,
                    aprobacion = materia.notaAprobacion,
                    modifier = Modifier.size(100.dp)
                )

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Promedio actual",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AnimatedText(
                        text = "${materia.promedioDisplay} / ${materia.escalaMax.toInt()}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    EstadoBadge(
                        aprobado = materia.aprobado,
                        texto = if (materia.aprobado) "APROBADO" else "EN RIESGO"
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Mínimo: ${materia.notaAprobacion}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Card de progreso acumulado ────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Acumulado",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    AnimatedText(
                        text = "${materia.acumuladoDisplay} / ${materia.escalaMax.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(4.dp))
                // Barra de progreso general
                val progresoAcum = (materia.acumulado / materia.escalaMax).coerceIn(0f, 1f)
                GradeLinearIndicator(progreso = progresoAcum)
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Evaluado: ${(materia.porcentajeEvaluado * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    materia.notaNecesariaParaAprobar?.let { necesita ->
                        Text(
                            text = "Necesitas ≈ ${"%.2f".format(necesita)} en lo restante",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // ── Banner de felicitación ──────────────────────────────
        if (materia.yaAprobo && !materia.completa) {
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎉",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "¡Felicidades!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "Ya superaste el mínimo de ${materia.notaAprobacion} para aprobar. ¡Llevas ${materia.acumuladoDisplay} acumulado!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ComponenteCard(
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
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(tween(250)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ── Header del componente ──────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = componente.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${componente.porcentajeDisplay}% del total",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    AnimatedText(
                        text = componente.promedio?.let { "%.2f".format(it) } ?: "--",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    componente.aporteAlFinal?.let { aporte ->
                        Text(
                            text = "Aporte: %.2f".format(aporte),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = componente.progresoDisplay,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Barra de progreso del componente
            val progress = componente.promedio?.let { it / escalaMax } ?: 0f
            Spacer(Modifier.height(8.dp))
            GradeLinearIndicator(progreso = progress)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

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
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Botón agregar nota al corte
            TextButton(
                onClick = { mostrarDialogAgregar = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Agregar nota", style = MaterialTheme.typography.labelLarge)
            }
        }
    }

    if (mostrarDialogAgregar) {
        AgregarSubNotaDialog(
            componenteNombre = componente.nombre,
            onDismiss = { mostrarDialogAgregar = false },
            onAgregar = { desc, pct ->
                onAgregarSubNota(desc, pct)
                mostrarDialogAgregar = false
            }
        )
    }
}

@Composable
private fun SubNotaRow(
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
    var textValue by remember(subNota.valor) {
        mutableStateOf(subNota.valor?.let { "%.1f".format(it) } ?: "")
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // ── Fila principal de la sub-nota ─────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = subNota.descripcion, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "${(subNota.porcentajeDelComponente * 100).toInt()}% del corte",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (subNota.esCompuesta) {
                    Text(
                        text = "Compuesta · ${subNota.detalles.size} elemento(s)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (subNota.esCompuesta) {
                // Sub-nota compuesta: mostrar valor calculado (solo lectura)
                Text(
                    text = subNota.valorEfectivo?.let { "%.2f".format(it) } ?: "--",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                // Botón expandir/contraer
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Contraer detalles" else "Ver detalles"
                    )
                }
            } else {
                // Sub-nota simple: campo editable
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { input ->
                        textValue = input
                        val parsed = input.toFloatOrNull()
                        if (parsed != null && parsed in 0f..escalaMax) {
                            onValueChange(parsed)
                        } else if (input.isEmpty()) {
                            onValueChange(null)
                        }
                    },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(0.35f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    placeholder = {
                        Text(text = "--", style = MaterialTheme.typography.bodySmall)
                    }
                )
            }

            IconButton(
                onClick = onEliminar,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar nota",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // ── Sección de detalles (sub-notas internas) ──────────
        if (expanded && subNota.esCompuesta) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(8.dp)
            ) {
                subNota.detalles.forEach { detalle ->
                    DetalleRow(
                        detalle = detalle,
                        escalaMax = escalaMax,
                        onValueChange = { valor -> onActualizarDetalle(detalle.id, valor) },
                        onEliminar = { onEliminarDetalle(detalle.id) }
                    )
                    Spacer(Modifier.height(4.dp))
                }
                TextButton(
                    onClick = { mostrarDialogDetalle = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Agregar elemento", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Botón para "convertir a compuesta" (agregar primer detalle)
        if (!subNota.esCompuesta) {
            TextButton(
                onClick = { mostrarDialogDetalle = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Dividir en sub-notas",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (mostrarDialogDetalle) {
        AgregarDetalleDialog(
            subNotaNombre = subNota.descripcion,
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
private fun DetalleRow(
    detalle: SubNotaDetalle,
    escalaMax: Float,
    onValueChange: (Float?) -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textValue by remember(detalle.valor) {
        mutableStateOf(detalle.valor?.let { "%.1f".format(it) } ?: "")
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "· ${detalle.descripcion}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${(detalle.porcentaje * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        OutlinedTextField(
            value = textValue,
            onValueChange = { input ->
                textValue = input
                val parsed = input.toFloatOrNull()
                if (parsed != null && parsed in 0f..escalaMax) {
                    onValueChange(parsed)
                } else if (input.isEmpty()) {
                    onValueChange(null)
                }
            },
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(0.35f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            placeholder = {
                Text(text = "--", style = MaterialTheme.typography.bodySmall)
            }
        )

        IconButton(
            onClick = onEliminar,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AgregarDetalleDialog(
    subNotaNombre: String,
    onDismiss: () -> Unit,
    onAgregar: (descripcion: String, porcentaje: Float) -> Unit
) {
    var descripcion by remember { mutableStateOf("") }
    var porcentajeText by remember { mutableStateOf("100") }
    val pct = porcentajeText.toFloatOrNull()
    val valido = descripcion.isNotBlank() && pct != null && pct in 1f..100f

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Elem. de «$subNotaNombre»") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    placeholder = { Text("Ej: Primer intento") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = porcentajeText,
                    onValueChange = { porcentajeText = it },
                    label = { Text("Peso (% dentro de la actividad)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = pct ?: return@Button
                    onAgregar(descripcion.trim(), p / 100f)
                },
                enabled = valido
            ) { Text("Agregar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun AgregarSubNotaDialog(
    componenteNombre: String,
    onDismiss: () -> Unit,
    onAgregar: (descripcion: String, porcentaje: Float) -> Unit
) {
    var descripcion by remember { mutableStateOf("") }
    var porcentajeText by remember { mutableStateOf("100") }
    val pct = porcentajeText.toFloatOrNull()
    val valido = descripcion.isNotBlank() && pct != null && pct in 1f..100f

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar nota · $componenteNombre") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    placeholder = { Text("Ej: Parcial 1") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = porcentajeText,
                    onValueChange = { porcentajeText = it },
                    label = { Text("Peso (% del corte)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = pct ?: return@Button
                    onAgregar(descripcion.trim(), p / 100f)
                },
                enabled = valido
            ) { Text("Agregar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

