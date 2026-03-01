package com.notasapp.data.remote

/**
 * Wrapper genérico para resultados de operaciones de red.
 *
 * Permite tratar las respuestas de forma uniforme en toda la capa de datos:
 *   - [Success] cuando la operación completó sin errores.
 *   - [Error] cuando ocurrió una excepción o el servidor respondió con fallo.
 *   - [Loading] cuando la operación está en curso (útil para StateFlow de UI).
 *
 * Uso típico:
 * ```kotlin
 * when (val result = repository.syncSheets()) {
 *     is NetworkResult.Success -> { /* usar result.data */ }
 *     is NetworkResult.Error   -> { /* mostrar result.message */ }
 *     is NetworkResult.Loading -> { /* mostrar spinner */ }
 * }
 * ```
 */
sealed class NetworkResult<out T> {

    /** Operación completada con éxito. [data] contiene el resultado. */
    data class Success<T>(val data: T) : NetworkResult<T>()

    /**
     * La operación falló.
     *
     * @param message   Mensaje descriptivo para mostrar en la UI.
     * @param cause     Excepción original (opcional, útil para logging).
     * @param needsUserRecovery  True cuando se requiere que el usuario realice
     *                           una acción (ej: conceder permisos OAuth2 en Sheets).
     */
    data class Error(
        val message: String,
        val cause: Throwable? = null,
        val needsUserRecovery: Boolean = false
    ) : NetworkResult<Nothing>()

    /** La operación está en curso. */
    data object Loading : NetworkResult<Nothing>()
}

// ── Extensiones de utilidad ──────────────────────────────────────────────────

/** Devuelve el dato si [NetworkResult.Success], o null en otro caso. */
fun <T> NetworkResult<T>.getOrNull(): T? = (this as? NetworkResult.Success)?.data

/** Mapea el dato de un [NetworkResult.Success] conservando el tipo de error. */
inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> = when (this) {
    is NetworkResult.Success -> NetworkResult.Success(transform(data))
    is NetworkResult.Error   -> this
    is NetworkResult.Loading -> NetworkResult.Loading
}
