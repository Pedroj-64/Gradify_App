package com.notasapp.ui

import android.content.Context
import androidx.annotation.StringRes

/** Texto para la UI que se resuelve con el idioma vigente; los ViewModels no devuelven strings crudos. */
class UiText(@StringRes val id: Int, vararg val args: Any) {
    fun asString(context: Context): String = context.getString(id, *args)
}

/** Evento de una sola vez (snackbar o salida de pantalla) emitido por un ViewModel vía Channel. */
sealed interface UiEvent {
    data class Message(val text: UiText) : UiEvent
    data object Done : UiEvent
}
