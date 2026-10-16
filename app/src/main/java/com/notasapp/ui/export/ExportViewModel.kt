package com.notasapp.ui.export

import com.notasapp.ui.UiEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import com.notasapp.ui.UiText
import com.notasapp.R
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.notasapp.data.local.UserPreferencesRepository
import com.notasapp.data.remote.NetworkResult
import com.notasapp.domain.repository.MateriaRepository
import com.notasapp.domain.repository.SheetsRepository
import com.notasapp.navigation.Screen
import com.notasapp.utils.ExcelExporter
import com.notasapp.utils.PdfExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

/**
 * ViewModel de la pantalla de exportación.
 *
 * Gestiona tres canales de salida de datos (todos vía SAF):
 *  1. **Excel** (.xlsx) — exporta vía Apache POI al almacenamiento elegido.
 *  2. **PDF** (.pdf) — genera resumen de notas.
 *  3. **Google Drive** — reutiliza el Excel export; el usuario elige Drive
 *     como destino en el selector de archivos del sistema (SAF).
 */
@HiltViewModel
class ExportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val materiaRepository: MateriaRepository,
    private val sheetsRepository: SheetsRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val excelExporter: ExcelExporter,
    private val pdfExporter: PdfExporter,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val materiaId: Long = checkNotNull(
        savedStateHandle[Screen.Export.ARG_MATERIA_ID]
    )

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        // Cargar nombre de materia para sugerir nombres de archivo
        viewModelScope.launch {
            val materia = materiaRepository.getMateriaConComponentes(materiaId).first()
            materia?.let { m ->
                val sanitizedName = sanitizeFilename(m.nombre)
                _uiState.update {
                    it.copy(
                        materiaNombre = m.nombre,
                        suggestedExcelFilename = "notas_$sanitizedName.xlsx",
                        suggestedPdfFilename = "notas_$sanitizedName.pdf"
                    )
                }
            }
        }
    }

    /**
     * Sanitiza el nombre para usarlo como filename válido.
     */
    private fun sanitizeFilename(name: String): String =
        name.replace(Regex("[^a-zA-Z0-9áéíóúñÁÉÍÓÚÑ_\\-]"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
            .take(50)

    // ── Excel ────────────────────────────────────────────────────────────────

    /**
     * Nombre sugerido para el archivo .xlsx al abrir el SAF picker.
     * Puede llamarse desde la UI para prerellenar el nombre en el diálogo del sistema.
     */
    fun sugerirNombreArchivo(): String = _uiState.value.suggestedExcelFilename

    /**
     * Nombre sugerido para el archivo .pdf al abrir el SAF picker.
     */
    fun sugerirNombrePdf(): String = _uiState.value.suggestedPdfFilename

    /**
     * Exporta la materia a .xlsx escribiendo en el URI elegido por el usuario (SAF).
     *
     * El usuario abre el selector de archivos del sistema con [ActivityResultContracts.CreateDocument]
     * y elige la carpeta/nombre. Este método escribe el contenido en ese URI.
     *
     * @param uri URI devuelto por [ActivityResultContracts.CreateDocument].
     */
    fun exportarExcelSAF(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportSuccess = false) }
            try {
                val materia = materiaRepository.getMateriaConComponentes(materiaId).first()
                    ?: throw IllegalStateException("Materia no encontrada")

                withContext(Dispatchers.IO) {
                    // Build the bytes fully in memory first
                    val bytes = excelExporter.buildWorkbookBytes(materia)
                    Timber.d("Excel bytes generados: ${bytes.size} para ${materia.nombre}")

                    if (bytes.isEmpty()) {
                        throw IllegalStateException(
                            "El archivo Excel generado tiene 0 bytes — error interno de POI"
                        )
                    }

                    // Write bytes to SAF URI
                    val outputStream = context.contentResolver.openOutputStream(uri)
                        ?: throw IOException("No se pudo abrir el archivo destino")

                    outputStream.use { os ->
                        os.write(bytes)
                        os.flush()
                    }

                    Timber.i("Excel guardado en SAF: $uri (${bytes.size} bytes)")
                }

                _uiState.update {
                    it.copy(
                        isExporting      = false,
                        exportSuccess    = true,
                        exportedFilePath = uri.toString(),
                        exportedFileUri  = uri
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al exportar Excel SAF")
                _events.trySend(UiEvent.Message(UiText(R.string.exp_err_file)))
                _uiState.update {
                    it.copy(
                        isExporting = false,
                    )
                }
            }
        }
    }

    // ── PDF ──────────────────────────────────────────────────────────────────

    /**
     * Exporta la materia a .pdf escribiendo en el URI elegido por el usuario (SAF).
     */
    fun exportarPdfSAF(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportSuccess = false) }
            try {
                val materia = materiaRepository.getMateriaConComponentes(materiaId).first()
                    ?: throw IllegalStateException("Materia no encontrada")

                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        pdfExporter.exportarMateriaToOutputStream(materia, outputStream)
                    } ?: throw IOException("No se pudo abrir el archivo destino")
                }

                Timber.i("PDF guardado en SAF: $uri")
                _uiState.update {
                    it.copy(
                        isExporting      = false,
                        exportSuccess    = true,
                        exportedFilePath = uri.toString(),
                        exportedFileUri  = uri
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al exportar PDF SAF")
                _events.trySend(UiEvent.Message(UiText(R.string.exp_err_file)))
                _uiState.update {
                    it.copy(
                        isExporting = false,
                    )
                }
            }
        }
    }

    fun clearMessages() = _uiState.update {
        it.copy(
            exportSuccess = false,
        )
    }

    fun exportarDrive() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isExporting = true,
                    exportSuccess = false
                )
            }

            try {
                val materia = materiaRepository.getMateriaConComponentes(materiaId).first()
                    ?: throw IllegalStateException("Materia no encontrada")
                val userEmail = userPreferencesRepository.userEmail.first()
                    ?: throw NotSignedInException()

                when (val result = sheetsRepository.syncMateria(materia, userEmail)) {
                    is NetworkResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isExporting = false,
                                exportSuccess = true,
                                driveWebLink = result.data
                            )
                        }
                    }

                    is NetworkResult.Error -> {
                        val recoverable = (result.cause as? UserRecoverableAuthIOException)
                        if (result.needsUserRecovery && recoverable != null) {
                            _uiState.update {
                                it.copy(
                                    isExporting = false,
                                    driveRecoveryIntent = recoverable.intent,
                                    pendingDriveRetry = true
                                )
                            }
                        } else {
                            _events.trySend(UiEvent.Message(UiText(R.string.exp_err_drive)))
                            _uiState.update {
                                it.copy(
                                    isExporting = false,
                                    pendingDriveRetry = false
                                )
                            }
                        }
                    }

                    is NetworkResult.Loading -> {
                        _uiState.update { it.copy(isExporting = true) }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al exportar a Drive")
                _events.trySend(UiEvent.Message(UiText(if (e is NotSignedInException) R.string.exp_err_login_required else R.string.exp_err_drive)))
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        pendingDriveRetry = false
                    )
                }
            }
        }
    }

    fun onDriveRecoveryIntentConsumed() {
        _uiState.update { it.copy(driveRecoveryIntent = null) }
    }

    fun onDriveAuthResult(granted: Boolean) {
        val shouldRetry = _uiState.value.pendingDriveRetry
        _uiState.update {
            it.copy(
                pendingDriveRetry = false,
                driveRecoveryIntent = null
            )
        }

        if (granted && shouldRetry) {
            exportarDrive()
        } else if (!granted) {
            _events.trySend(UiEvent.Message(UiText(R.string.exp_err_drive_permission)))
        }
    }
}

data class ExportUiState(
    val isExporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val exportedFilePath: String? = null,
    /** URI FileProvider del .xlsx generado, listo para compartir. */
    val exportedFileUri: Uri? = null,
    val driveWebLink: String? = null,
    val driveRecoveryIntent: Intent? = null,
    val pendingDriveRetry: Boolean = false,
    /** Nombre de la materia cargado. */
    val materiaNombre: String? = null,
    /** Nombre sugerido para el archivo Excel. */
    val suggestedExcelFilename: String = "notas.xlsx",
    /** Nombre sugerido para el archivo PDF. */
    val suggestedPdfFilename: String = "notas.pdf"
)

private class NotSignedInException : Exception()
