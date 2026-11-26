package com.notasapp.ui.materia.detail

import androidx.compose.material3.AssistChip
import androidx.compose.runtime.LaunchedEffect
import com.notasapp.ui.components.AxoMood
import com.notasapp.ui.components.GRADE_INPUT
import com.notasapp.ui.components.parseGrade
import com.notasapp.ui.components.toInputString
import com.notasapp.ui.components.GradifySheet
import com.notasapp.ui.components.Tone
import com.notasapp.ui.components.ResultCard
import com.notasapp.ui.UiText
import com.notasapp.R
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.notasapp.domain.usecase.CalcularNotaNecesariaUseCase

/**
 * Bottom sheet que permite al usuario ingresar una meta de nota final y
 * ver qué promedio necesita obtener en las evaluaciones pendientes.
 *
 * Consume [MateriaDetailViewModel.notaNecesariaResult] y llama a
 * [MateriaDetailViewModel.calcularNotaNecesaria] al presionar stringResource(R.string.calc_calculate).
 *
 * @param escalaMax          Nota máxima de la materia (para validación y hints).
 * @param resultado          Resultado actual del caso de uso (puede ser null = sin calcular).
 * @param onCalcular         Callback con la meta ingresada por el usuario.
 * @param onDismiss          Cierra el sheet (y limpia el estado con clearCalculadora).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculadoraBottomSheet(
    escalaMax: Float,
    notaAprobacion: Float,
    notaMeta: Float?,
    resultado: CalcularNotaNecesariaUseCase.Resultado?,
    onCalcular: (Float) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    GradifySheet(onDismiss = onDismiss, sheetState = sheetState) {
        CalculadoraContent(
            escalaMax = escalaMax,
            notaAprobacion = notaAprobacion,
            notaMeta = notaMeta,
            resultado = resultado,
            onCalcular = onCalcular,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}

// ── Handle ──────────────────────────────────────────────────────────────────

// ── Contenido principal ──────────────────────────────────────────────────────

@Composable
private fun CalculadoraContent(
    escalaMax: Float,
    notaAprobacion: Float,
    notaMeta: Float?,
    resultado: CalcularNotaNecesariaUseCase.Resultado?,
    onCalcular: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboard = LocalSoftwareKeyboardController.current
    // Abre ya calculado con la meta (o con la nota para pasar): la pregunta típica es "¿qué necesito?".
    var inputMeta by remember { mutableStateOf((notaMeta ?: notaAprobacion).toInputString()) }
    var inputError by remember { mutableStateOf<UiText?>(null) }
    LaunchedEffect(Unit) { calcular(inputMeta, escalaMax, onCalcular) { inputError = it } }
    val usar = { valor: Float ->
        inputMeta = valor.toInputString()
        inputError = null
        onCalcular(valor)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Calculate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.detail_what_grade_needed),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.calc_enter_target),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        // Input meta
        OutlinedTextField(
            value = inputMeta,
            onValueChange = { raw ->
                inputError = null
                if (GRADE_INPUT.matches(raw)) inputMeta = raw   // coma o punto
            },
            label = { Text(stringResource(R.string.calc_goal_label, escalaMax)) },
            singleLine = true,
            isError = inputError != null,
            supportingText = inputError?.let { { Text(it.asString(androidx.compose.ui.platform.LocalContext.current), color = MaterialTheme.colorScheme.error) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboard?.hide()
                    calcular(inputMeta, escalaMax, onCalcular) { inputError = it }
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Atajos: un toque y listo, sin teclear.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            AssistChip(
                onClick = { usar(notaAprobacion) },
                label = { Text(stringResource(R.string.calc_chip_pass, notaAprobacion.toInputString())) }
            )
            if (notaMeta != null) {
                AssistChip(
                    onClick = { usar(notaMeta) },
                    label = { Text(stringResource(R.string.calc_chip_goal, notaMeta.toInputString())) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                keyboard?.hide()
                calcular(inputMeta, escalaMax, onCalcular) { inputError = it }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.calc_calculate))
        }

        // Resultado animado
        AnimatedContent(
            targetState = resultado,
            transitionSpec = {
                (slideInVertically { it / 2 } + fadeIn()) togetherWith
                        (slideOutVertically { -it / 2 } + fadeOut())
            },
            label = "calc_result"
        ) { res ->
            if (res != null) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(16.dp))
                    ResultadoCard(resultado = res)
                }
            }
        }
    }
}

// ── Tarjeta de resultado ────────────────────────────────────────────────────

@Composable
private fun ResultadoCard(resultado: CalcularNotaNecesariaUseCase.Resultado) {
    when (resultado) {
        is CalcularNotaNecesariaUseCase.Resultado.MetaYaAlcanzada -> {
            ResultCard(
                icon = Icons.Default.CheckCircle,
                title = stringResource(R.string.calc_goal_reached),
                subtitle = stringResource(if (resultado.aprobado) R.string.calc_done_pass else R.string.calc_done_goal),
                tone = Tone.OK,
                mood = AxoMood.FELIZ
            )
        }

        is CalcularNotaNecesariaUseCase.Resultado.Posible -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ResultCard(
                    icon = Icons.Default.TrendingUp,
                    title = stringResource(R.string.calc_need, resultado.notaNecesaria),
                    subtitle = stringResource(R.string.calc_avg_pending, resultado.componentesFaltantes.size),
                    tone = Tone.NONE,
                    mood = AxoMood.ESTUDIANDO
                )
            }
        }

        is CalcularNotaNecesariaUseCase.Resultado.Imposible -> {
            ResultCard(
                icon = Icons.Default.Error,
                title = stringResource(R.string.goal_unreachable),
                subtitle = stringResource(R.string.calc_unreachable_sub, resultado.notaNecesariaCalculada),
                tone = Tone.BAD,
                mood = AxoMood.PREOCUPADO
            )
        }

        is CalcularNotaNecesariaUseCase.Resultado.Error -> {
            ResultCard(
                icon = Icons.Default.Info,
                title = stringResource(R.string.calc_error),
                subtitle = stringResource(R.string.calc_err_percent),
                tone = Tone.WARN
            )
        }
    }
}

// ── Helper ──────────────────────────────────────────────────────────────────

private fun calcular(
    input: String,
    escalaMax: Float,
    onCalcular: (Float) -> Unit,
    onError: (UiText) -> Unit
) {
    val meta = input.parseGrade()
    when {
        meta == null -> onError(UiText(R.string.calc_invalid_grade))
        meta <= 0f -> onError(UiText(R.string.calc_goal_positive))
        meta > escalaMax -> onError(UiText(R.string.calc_goal_max, escalaMax))
        else -> onCalcular(meta)
    }
}
