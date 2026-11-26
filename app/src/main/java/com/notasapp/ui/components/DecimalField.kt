package com.notasapp.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

/** Solo dígitos con un decimal opcional (máx. 2): rechaza "NaN", "1e5", letras y signos al teclear. */
val GRADE_INPUT = Regex("^\\d{0,3}([.,]\\d{0,2})?$")

/** Acepta coma o punto decimal. */
fun String.parseGrade(): Float? = this.replace(',', '.').toFloatOrNull()

/** 4.0 → "4", 3.75 → "3.75", 0.3f * 100 (= 30.000002) → "30". Máx. 2 decimales, como admite [GRADE_INPUT]. */
fun Float.toInputString(): String =
    java.math.BigDecimal(this.toString()).setScale(2, java.math.RoundingMode.HALF_UP)
        .stripTrailingZeros().toPlainString()

/** Fracción → porcentaje para mostrar: 0.205 → "20.5", 0.3 → "30". Nunca corta los decimales. */
fun Float.pctText(): String = (this * 100f).toInputString()

/** True si el texto no es un número válido en [min]..[max]. Vacío no es error. */
fun isGradeInvalid(text: String, max: Float, min: Float = 0f): Boolean {
    if (text.isEmpty()) return false
    val v = text.parseGrade() ?: return true
    return v !in min..max
}

/**
 * Campo numérico escrito (en vez de slider). Guarda en [onValueChange] solo valores válidos dentro
 * de [min]..[max]; mientras el usuario teclea manda su texto, y al salir se resincroniza con [value].
 */
@Composable
fun DecimalField(
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String,
    min: Float,
    max: Float,
    modifier: Modifier = Modifier,
    suffix: String? = null,
    imeAction: ImeAction = ImeAction.Next
) {
    var text by remember { mutableStateOf(value.toInputString()) }
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(value, focused) {
        if (!focused && text.parseGrade() != value) text = value.toInputString()
    }
    val invalid = text.isEmpty() || isGradeInvalid(text, max, min)

    OutlinedTextField(
        value = text,
        onValueChange = { input ->
            if (GRADE_INPUT.matches(input)) {
                text = input
                if (!isGradeInvalid(input, max, min)) input.parseGrade()?.let(onValueChange)
            }
        },
        label = { Text(label) },
        suffix = suffix?.let { { Text(it) } },
        singleLine = true,
        isError = invalid,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = imeAction),
        modifier = modifier.onFocusChanged { focused = it.isFocused }
    )
}
