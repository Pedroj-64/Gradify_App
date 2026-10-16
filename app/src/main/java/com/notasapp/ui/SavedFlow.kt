package com.notasapp.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/** MutableStateFlow que sobrevive a la muerte del proceso: se restaura de [handle] y se vuelve a guardar en cada cambio. T debe ser Serializable. */
fun <T> ViewModel.savedFlow(handle: SavedStateHandle, key: String, initial: T): MutableStateFlow<T> =
    MutableStateFlow(handle.get<T>(key) ?: initial).also { flow ->
        viewModelScope.launch { flow.collect { handle[key] = it } }
    }
