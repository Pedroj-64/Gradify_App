package com.notasapp.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notasapp.data.local.AppDatabase
import com.notasapp.data.local.UserPreferencesRepository
import com.notasapp.domain.model.ConfiguracionNota
import com.notasapp.utils.BackupManager
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
 * Estado de la UI de la pantalla de Configuración.
 *
 * @property isLoading         True mientras se procesa un backup o restauración.
 * @property successMessage    Mensaje de éxito a mostrar en Snackbar (null = ninguno).
 * @property errorMessage      Mensaje de error a mostrar en Snackbar (null = ninguno).
 * @property shareIntent       Intent ACTION_SEND listo para lanzar tras exportar el backup.
 * @property lastSyncMs        Timestamp (ms) de la última sync con Sheets; null si nunca.
 * @property showLogoutDialog  True cuando se debe mostrar el diálogo de confirmación de logout.
 * @property loggedOut         True cuando el logout fue completado (navegar a Login).
 */
data class SettingsUiState(
    val isLoading:        Boolean = false,
    val successMessage:   String? = null,
    val errorMessage:     String? = null,
    val shareIntent:      Intent? = null,
    val lastSyncMs:       Long?   = null,
    val showLogoutDialog: Boolean = false,
    val loggedOut:        Boolean = false,
    val configuracionNota: ConfiguracionNota = ConfiguracionNota()
)

/**
 * ViewModel de la pantalla de Configuración.
 *
 * Gestiona dos operaciones principales:
 * - [exportarBackup]: serializa todos los datos a JSON y construye un Intent de compartición.
 * - [importarBackup]: lee un archivo JSON seleccionado por el usuario y restaura los datos en Room.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPrefsRepository: UserPreferencesRepository,
    private val backupManager:       BackupManager,
    private val database:            AppDatabase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Observar el timestamp de la última sync
        viewModelScope.launch {
            userPrefsRepository.lastSyncMs.collect { ms ->
                _uiState.update { it.copy(lastSyncMs = ms) }
            }
        }
        // Observar la configuración de redondeo
        viewModelScope.launch {
            userPrefsRepository.configuracionNota.collect { config ->
                _uiState.update { it.copy(configuracionNota = config) }
            }
        }
    }

    // ── Backup ────────────────────────────────────────────────────────────────

    /**
     * Exporta las notas del usuario activo a un archivo JSON.
     *
     * Al completar, expone [SettingsUiState.shareIntent] con el Intent listo para
     * compartir via `startActivity(Intent.createChooser(...))`.
     */
    fun exportarBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val usuarioId = resolveUsuarioId()
                val intent    = backupManager.buildExportIntent(database, usuarioId)
                _uiState.update { it.copy(isLoading = false, shareIntent = intent) }
            } catch (e: Exception) {
                Timber.e(e, "Error al exportar backup")
                _uiState.update {
                    it.copy(
                        isLoading    = false,
                        errorMessage = "Error al exportar: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    // ── Restore ───────────────────────────────────────────────────────────────

    /**
     * Restaura notas desde un archivo JSON seleccionado con el SAF file picker.
     *
     * Los datos importados se combinan con los locales (fusión aditiva).
     * No se eliminan registros existentes.
     *
     * @param uri URI devuelta por [ActivityResultContracts.OpenDocument].
     */
    fun importarBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val usuarioId = resolveUsuarioId()
                val count     = backupManager.importFromUri(uri, database, usuarioId)
                _uiState.update {
                    it.copy(
                        isLoading      = false,
                        successMessage = "$count materia${if (count != 1) "s" else ""} restaurada${if (count != 1) "s" else ""} exitosamente"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al importar backup")
                _uiState.update {
                    it.copy(
                        isLoading    = false,
                        errorMessage = "Error al restaurar: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    /** Limpia todos los mensajes pendientes (llamado tras mostrarlos en Snackbar). */
    fun clearMessages() {
        _uiState.update {
            it.copy(successMessage = null, errorMessage = null, shareIntent = null)
        }
    }

    // ── Configuración de redondeo ─────────────────────────────────────────────

    /** Actualiza la configuración de redondeo de notas. */
    fun updateConfiguracionNota(config: ConfiguracionNota) {
        viewModelScope.launch {
            userPrefsRepository.setConfiguracionNota(config)
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    /** Muestra el diálogo de confirmación de cierre de sesión. */
    fun showLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = true) }
    }

    /** Oculta el diálogo de confirmación. */
    fun dismissLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = false) }
    }

    /**
     * Cierra la sesión del usuario limpiando el email de DataStore.
     * Los datos locales (materias) se conservan para cuando vuelva a ingresar.
     */
    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showLogoutDialog = false) }
            try {
                userPrefsRepository.clearUserEmail()
                Timber.i("Sesión cerrada correctamente")
                _uiState.update { it.copy(isLoading = false, loggedOut = true) }
            } catch (e: Exception) {
                Timber.e(e, "Error al cerrar sesión")
                _uiState.update {
                    it.copy(
                        isLoading    = false,
                        errorMessage = "Error al cerrar sesión: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Obtiene el ID del usuario activo desde Room o DataStore. */
    private suspend fun resolveUsuarioId(): String =
        database.usuarioDao().getUsuarioActivoOnce()?.googleId
            ?: userPrefsRepository.userEmail.first()
            ?: error("Debes iniciar sesión antes de operar con el backup")
}
