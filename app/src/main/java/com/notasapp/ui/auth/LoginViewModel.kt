package com.notasapp.ui.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.notasapp.BuildConfig
import com.notasapp.data.local.UserPreferencesRepository
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
 * Gestiona el flujo completo de Google Sign-In usando Credential Manager API:
 *  1. Lanza el selector de cuentas Google desde [signInWithGoogle].
 *  2. Parsea el [GoogleIdTokenCredential] recibido.
 *  3. Persiste el usuario en Room y su email en DataStore
 *     (para uso posterior en [SheetsService]).
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val usuarioDao: UsuarioDao,
    private val userPrefsRepository: UserPreferencesRepository
) : ViewModel() {

    // ── Estado de UI ───────────────────────────────────────────

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // ── Acciones públicas ──────────────────────────────────────

    /**
     * Inicia el flujo de Google Sign-In con Credential Manager.
     *
     * Requiere el [Context] de la Activity/Composable porque Credential Manager
     * necesita lanzar el selector de cuentas como diálogo del sistema.
     *
     * Esto reemplaza el antiguo `signInLauncher` legacy basado en PendingIntent.
     */
    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val credentialManager = CredentialManager.create(context)

                // Pedir cualquier cuenta Google guardada de este dispositivo
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)   // Muestra TODAS las cuentas
                    .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                    .setAutoSelectEnabled(false)            // Forzar selector explícito
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                // Parsear el credential devuelto
                val credential = result.credential
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    persistUser(
                        googleId = googleCredential.id,
                        nombre   = googleCredential.displayName ?: googleCredential.id,
                        email    = googleCredential.id,
                        fotoUrl  = googleCredential.profilePictureUri?.toString()
                    )
                } else {
                    Timber.w("Tipo de credencial inesperado: ${credential.type}")
                    _uiState.value = LoginUiState.Error("Tipo de credencial no soportado")
                }

            } catch (e: GetCredentialCancellationException) {
                // El usuario cerró el selector — no es un error grave
                Timber.d("Login cancelado por el usuario")
                _uiState.value = LoginUiState.Idle
            } catch (e: NoCredentialException) {
                // No hay ninguna cuenta Google en el dispositivo
                Timber.w(e, "Sin cuentas Google disponibles")
                _uiState.value = LoginUiState.Error("Agrega una cuenta Google en los ajustes del dispositivo")
            } catch (e: GetCredentialException) {
                Timber.e(e, "GetCredentialException")
                _uiState.value = LoginUiState.Error("No se pudo iniciar sesión: ${e.localizedMessage}")
            }
        }
    }

    // ── Helpers privados ───────────────────────────────────────

    private suspend fun persistUser(
        googleId: String,
        nombre: String,
        email: String,
        fotoUrl: String?
    ) {
        try {
            usuarioDao.insertOrUpdate(
                UsuarioEntity(
                    googleId = googleId,
                    nombre   = nombre,
                    email    = email,
                    fotoUrl  = fotoUrl
                )
            )
            // Guardar email en DataStore para GoogleAccountCredential (Sheets API)
            userPrefsRepository.setUserEmail(email)
            Timber.i("Login exitoso: $email")
            _uiState.value = LoginUiState.Success
        } catch (e: Exception) {
            Timber.e(e, "Error al guardar usuario")
            _uiState.value = LoginUiState.Error("Error al guardar sesión: ${e.localizedMessage}")
        }
    }

    fun resetError() {
        _uiState.value = LoginUiState.Idle
    }
}

// ── Estados de UI ──────────────────────────────────────────────

sealed class LoginUiState {
    data object Idle    : LoginUiState()
    data object Loading : LoginUiState()
    data object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
