package com.notasapp.ui.materia.detail

import com.notasapp.ui.components.GradifyDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.notasapp.R
import com.notasapp.domain.model.Materia

enum class RenameKind { COMPONENTE, SUBNOTA, DETALLE }

/**
 * Callback para renombrar desde cualquier fila sin pasarlo por 3 niveles de parámetros.
 * MateriaDetailScreen lo provee y hospeda el único diálogo.
 */
internal val LocalRename =
    staticCompositionLocalOf<(RenameKind, Long, String) -> Unit> { { _, _, _ -> } }

@Composable
internal fun RenameDialog(initial: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(initial) }
    GradifyDialog(
        title = stringResource(R.string.rename_title),
        onDismiss = onDismiss,
        confirmText = stringResource(R.string.btn_save),
        confirmEnabled = text.isNotBlank(),
        onConfirm = { onConfirm(text) }
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun EditMateriaDialog(
    materia: Materia,
    onDismiss: () -> Unit,
    onSave: (nombre: String, periodo: String, profesor: String?, creditos: Int) -> Unit
) {
    var nombre by remember { mutableStateOf(materia.nombre) }
    var periodo by remember { mutableStateOf(materia.periodo) }
    var profesor by remember { mutableStateOf(materia.profesor.orEmpty()) }
    var creditos by remember { mutableStateOf(materia.creditos.toString()) }
    GradifyDialog(
        title = stringResource(R.string.edit_subject_title),
        onDismiss = onDismiss,
        confirmText = stringResource(R.string.btn_save),
        confirmEnabled = nombre.isNotBlank() && periodo.isNotBlank(),
        onConfirm = { onSave(nombre, periodo, profesor.ifBlank { null }, creditos.toIntOrNull() ?: 0) }
    ) {
        OutlinedTextField(nombre, { nombre = it }, singleLine = true,
            label = { Text(stringResource(R.string.edit_subject_name)) }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(periodo, { periodo = it }, singleLine = true,
            label = { Text(stringResource(R.string.edit_subject_period)) }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(profesor, { profesor = it }, singleLine = true,
            label = { Text(stringResource(R.string.edit_subject_teacher)) }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(creditos, { creditos = it.filter(Char::isDigit).take(2) }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text(stringResource(R.string.edit_subject_credits)) }, modifier = Modifier.fillMaxWidth())
    }
}
