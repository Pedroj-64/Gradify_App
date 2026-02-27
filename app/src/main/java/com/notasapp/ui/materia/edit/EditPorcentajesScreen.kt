package com.notasapp.ui.materia.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * Pantalla para editar los porcentajes de los componentes.
 *
 * Soporta:
 * - Reordenamiento por drag & drop (usando la librería reorderable)
 * - Ajuste de porcentaje en tiempo real con Sliders
 * - Validación: el total siempre debe sumar 100%
 *
 * @param materiaId ID de la materia cuyos componentes se editarán.
 * @param onBack    Callback para regresar al detalle de la materia.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPorcentajesScreen(
    materiaId: Long,
    onBack: () -> Unit,
    viewModel: EditPorcentajesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) onBack()
    }

    // Estado mutable local para la lista reordenable
    val componentes = remember(uiState.componentes) {
        mutableStateListOf(*uiState.componentes.toTypedArray())
    }

    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to ->
            val moved = componentes.removeAt(from.index)
            componentes.add(to.index, moved)
            viewModel.onReorder(componentes.toList())
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar porcentajes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Indicador de total
            val sumaTotal = componentes.sumOf { it.porcentaje.toDouble() }.toFloat()
            val totalColor = if (kotlin.math.abs(sumaTotal - 1f) <= 0.01f)
                MaterialTheme.colorScheme.secondary
            else
                MaterialTheme.colorScheme.error

            Text(
                text = "Total: ${(sumaTotal * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = totalColor,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            LazyColumn(
                state = reorderState.lazyListState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = componentes,
                    key = { _, item -> item.id }
                ) { index, componente ->
                    ReorderableItem(
                        reorderableLazyListState = reorderState,
                        key = componente.id
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = componente.nombre,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${(componente.porcentaje * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = componente.porcentaje,
                                onValueChange = { nuevoPct ->
                                    viewModel.onPorcentajeChange(componente.id, nuevoPct)
                                },
                                valueRange = 0f..1f,
                                steps = 19
                            )
                        }
                    }
                }
            }

            Button(
                onClick = viewModel::guardar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 8.dp),
                enabled = kotlin.math.abs(sumaTotal - 1f) <= 0.01f
            ) {
                Text("Guardar cambios")
            }
        }
    }
}
