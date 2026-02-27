package com.notasapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notasapp.data.local.dao.UsuarioDao
import com.notasapp.data.local.entities.UsuarioEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel de la pantalla de Login.
 *
 * Gestiona el estado del proceso de Sign-In con Google y persiste
 * el usuario en Room al completar el login exitosamente.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val usuarioDao: UsuarioDao
) : ViewModel() {

    // ── Estado de UI ───────────────────────────────────────────

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // ── Acciones ───────────────────────────────────────────────

    /**
     * Llamado cuando Google Sign-In devuelve los datos del usuario.
     *
     * @param googleId    Sub del JWT de Google.
     * @param nombre      Nombre completo del usuario.
     * @param email       Email de la cuenta Google.
     * @param fotoUrl     URL del avatar (puede ser null).
     */
    fun onGoogleSignInSuccess(
        googleId: String,
        nombre: String,
        email: String,
        fotoUrl: String?
    ) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                usuarioDao.insertOrUpdate(
                    UsuarioEntity(
                        googleId = googleId,
                        nombre = nombre,
                        email = email,
                        fotoUrl = fotoUrl
                    )
                )
                Timber.i("Login exitoso: $email")
                _uiState.value = LoginUiState.Success
            } catch (e: Exception) {
                Timber.e(e, "Error al guardar usuario en Room")
                _uiState.value = LoginUiState.Error("Error al iniciar sesión: ${e.localizedMessage}")
            }
        }
    }

    fun onSignInError(message: String) {
        Timber.w("Error de Google Sign-In: $message")
        _uiState.value = LoginUiState.Error(message)
    }

    fun resetError() {
        _uiState.value = LoginUiState.Idle
    }
}

// ── Estados de UI ──────────────────────────────────────────────

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
