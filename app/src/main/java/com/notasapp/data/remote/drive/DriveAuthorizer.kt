package com.notasapp.data.remote.drive

import android.accounts.Account
import android.app.PendingIntent
import android.content.Context
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** El usuario debe aceptar el permiso de Drive: la UI lanza [pendingIntent] y reintenta. */
class DriveConsentRequiredException(val pendingIntent: PendingIntent) :
    IOException("Se necesita tu permiso para usar Google Drive")

/**
 * Pide a Google un token de acceso a Drive con la Authorization API.
 *
 * Reemplaza a `GoogleAccountCredential`, que solo "ve" las cuentas que el usuario eligió con su
 * propio selector: con el inicio de sesión moderno (Credential Manager) no encontraba la cuenta,
 * dejaba el nombre en `null` y fallaba con «the name must not be empty: null».
 */
@Singleton
class DriveAuthorizer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val SCOPE_DRIVE_FILE = "https://www.googleapis.com/auth/drive.file"
    }

    /** @throws DriveConsentRequiredException si falta el consentimiento del usuario. */
    suspend fun accessToken(email: String): String {
        val request = AuthorizationRequest.Builder()
            .setRequestedScopes(listOf(Scope(SCOPE_DRIVE_FILE)))
            .setAccount(Account(email, "com.google"))   // la cuenta con la que inició sesión
            .build()

        val result = try {
            Identity.getAuthorizationClient(context).authorize(request).await()
        } catch (e: ApiException) {
            throw IOException(
                "Google rechazó la autorización de Drive (código ${e.statusCode}). " +
                    "Si persiste, cierra sesión y vuelve a entrar con Google.", e
            )
        }

        if (result.hasResolution()) {
            val pending = result.pendingIntent
                ?: throw IOException("Google pidió consentimiento pero no devolvió la pantalla para mostrarlo")
            throw DriveConsentRequiredException(pending)
        }
        return result.accessToken ?: throw IOException("Google no devolvió un token de acceso")
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { cont.resume(it) }
        addOnFailureListener { cont.resumeWithException(it) }
        addOnCanceledListener { cont.cancel() }
    }
}
