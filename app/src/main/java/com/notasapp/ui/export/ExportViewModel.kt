package com.notasapp.ui.export

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notasapp.domain.repository.MateriaRepository
import com.notasapp.navigation.Screen
import com.notasapp.utils.ExcelExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel de la pantalla de exportación / sincronización.
 */
@HiltViewModel
class ExportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val materiaRepository: MateriaRepository,
    private val excelExporter: ExcelExporter,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val materiaId: Long = checkNotNull(
        savedStateHandle[Screen.Export.ARG_MATERIA_ID]
    )

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    /**
     * Exporta la materia a un archivo .xlsx en el almacenamiento del dispositivo.
     */
    fun exportarExcel() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportError = null) }
            try {
                val materia = materiaRepository.getMateriaConComponentes(materiaId).first()
                    ?: throw IllegalStateException("Materia no encontrada")

                val filePath = excelExporter.exportar(context, materia)
                Timber.i("Excel exportado: $filePath")
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportSuccess = true,
                        exportedFilePath = filePath
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al exportar Excel")
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportError = "Error al exportar: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    /**
     * Sincroniza la materia con Google Sheets (Fase 3).
     * Por ahora muestra un mensaje de "próximamente".
     */
    fun sincronizarSheets() {
        // TODO: Implementar en Fase 3 con Google Sheets API
        _uiState.update { it.copy(sheetsMessage = "Sincronización con Google Sheets - Próximamente en la Fase 3") }
    }

    fun clearMessages() = _uiState.update {
        it.copy(exportSuccess = false, exportError = null, sheetsMessage = null)
    }
}

data class ExportUiState(
    val isExporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val exportedFilePath: String? = null,
    val exportError: String? = null,
    val sheetsMessage: String? = null
)
