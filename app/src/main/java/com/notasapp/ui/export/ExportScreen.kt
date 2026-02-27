package com.notasapp.ui.export

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Pantalla de exportación y sincronización de una materia.
 *
 * Opciones disponibles:
 * - Exportar a .xlsx (Apache POI)
 * - Sincronizar con Google Sheets (Fase 3)
 *
 * @param materiaId ID de la materia a exportar.
 * @param onBack    Callback para regresar al detalle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    materiaId: Long,
    onBack: () -> Unit,
    viewModel: ExportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.exportSuccess, uiState.exportError, uiState.sheetsMessage) {
        when {
            uiState.exportSuccess -> {
                snackbarHostState.showSnackbar("✓ Archivo Excel guardado: ${uiState.exportedFilePath}")
                viewModel.clearMessages()
            }
            uiState.exportError != null -> {
                snackbarHostState.showSnackbar(uiState.exportError!!)
                viewModel.clearMessages()
            }
            uiState.sheetsMessage != null -> {
                snackbarHostState.showSnackbar(uiState.sheetsMessage!!)
                viewModel.clearMessages()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exportar / Sincronizar") },
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "¿Cómo deseas exportar\ntus notas?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Exportar a Excel ────────────────────────────
            Button(
                onClick = viewModel::exportarExcel,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                enabled = !uiState.isExporting
            ) {
                if (uiState.isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Exportar a Excel (.xlsx)")
                }
            }

            Text(
                text = "Descarga el archivo al almacenamiento de tu dispositivo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // ── Google Sheets ───────────────────────────────
            OutlinedButton(
                onClick = viewModel::sincronizarSheets,
                modifier = Modifier.fillMaxWidth().height(64.dp)
            ) {
                Icon(
                    Icons.Default.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Sincronizar con Google Sheets")
            }

            Text(
                text = "Crea o actualiza una hoja de cálculo en tu Google Drive.\nDisponible en la Fase 3.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
