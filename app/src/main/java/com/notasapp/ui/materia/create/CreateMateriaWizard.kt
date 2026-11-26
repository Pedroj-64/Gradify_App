package com.notasapp.ui.materia.create

import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuBox
import com.notasapp.domain.model.Materia
import com.notasapp.ui.components.MateriaAvatar
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.AssistChip
import com.notasapp.domain.model.ScaleTemplate
import com.notasapp.R
import androidx.compose.ui.res.stringResource
import com.notasapp.ui.UiEvent
import com.notasapp.ui.components.gradifyChipColors
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import com.notasapp.ui.components.ScreenHeader
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.OutlinedButton
import com.notasapp.ui.components.DecimalField
import com.notasapp.ui.components.toInputString
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import com.notasapp.domain.model.TipoEscala
import com.notasapp.utils.getDisplayName

/**
 * Wizard de 3 pasos para crear una nueva materia.
 *
 * Paso 1: Información básica (nombre, período, profesor)
 * Paso 2: Escala de calificación
 * Paso 3: Componentes / Cortes con porcentajes
 *
 * @param onWizardComplete Callback al guardar exitosamente.
 * @param onBack           Callback para cancelar y regresar al Home.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMateriaWizard(
    onWizardComplete: () -> Unit,
    onBack: () -> Unit,
    viewModel: CreateMateriaViewModel = hiltViewModel()
) {
    val state by viewModel.wizardState.collectAsStateWithLifecycle()
    val otras by viewModel.otrasMaterias.collectAsStateWithLifecycle()
    val porDefecto = listOf(
        stringResource(R.string.cw_default_c1), stringResource(R.string.cw_default_c2), stringResource(R.string.cw_default_c3)
    )
    LaunchedEffect(Unit) { viewModel.ponerComponentesPorDefecto(porDefecto) }
    val nombreNuevo = stringResource(R.string.cw_component_n, state.componentes.size + 1)
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val ctx = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                UiEvent.Done -> onWizardComplete()
                is UiEvent.Message -> snackbarHostState.showSnackbar(event.text.asString(ctx))
            }
        }
    }

    Scaffold(
        topBar = {
            ScreenHeader(
                title = stringResource(R.string.home_fab_add),
                subtitle = stringResource(R.string.cw_step, state.currentStep),
                onBack = {
                    if (state.currentStep == 1) onBack()
                    else viewModel.goBack()
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Progreso en 3 tramos ─────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                (1..3).forEach { i ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(
                                if (i <= state.currentStep) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                    )
                }
            }

            // ── Contenido del paso actual ────────────────────
            AnimatedContent(
                targetState = state.currentStep,
                label = "wizard_step"
            ) { step ->
                when (step) {
                    1 -> Step1BasicInfo(
                        state = state,
                        onNombreChange = viewModel::updateNombre,
                        onPeriodoChange = viewModel::updatePeriodo,
                        onProfesorChange = viewModel::updateProfesor,
                        onCreditosChange = viewModel::updateCreditos,
                        onNext = viewModel::goToStep2
                    )
                    2 -> Step2Escala(
                        state = state,
                        onTemplateChange = viewModel::applyTemplate,
                        onNotaAprobacionChange = viewModel::updateNotaAprobacion,
                        onNext = viewModel::goToStep3
                    )
                    3 -> Step3Componentes(
                        state = state,
                        isSaving = saveState is SaveState.Loading,
                        onAddComponente = { viewModel.addComponente(nombreNuevo) },
                        onRemoveComponente = viewModel::removeComponente,
                        onNombreChange = viewModel::updateComponenteNombre,
                        onPorcentajeChange = viewModel::updateComponentePorcentaje,
                        onRepartir = viewModel::repartirRestante,
                        otras = otras,
                        onCopiar = viewModel::copiarEstructura,
                        onGuardarOtra = { viewModel.guardarMateria(otra = true) },
                        onGuardar = { viewModel.guardarMateria() }
                    )
                }
            }
        }
    }
}

// ── Paso 1: Información básica ─────────────────────────────────

@Composable
private fun Step1BasicInfo(
    state: WizardState,
    onNombreChange: (String) -> Unit,
    onPeriodoChange: (String) -> Unit,
    onProfesorChange: (String) -> Unit,
    onCreditosChange: (Int) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.wizard_step1_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = state.nombre,
            onValueChange = onNombreChange,
            label = { Text(stringResource(R.string.campo_nombre)) },
            placeholder = { Text(stringResource(R.string.cw_name_ex)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            singleLine = true
        )

        OutlinedTextField(
            value = state.periodo,
            onValueChange = onPeriodoChange,
            label = { Text(stringResource(R.string.campo_periodo)) },
            placeholder = { Text("Ej: 2026-1") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            singleLine = true
        )

        OutlinedTextField(
            value = state.profesor,
            onValueChange = onProfesorChange,
            label = { Text(stringResource(R.string.edit_subject_teacher)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            singleLine = true
        )

        // ── Créditos ──
        Text(
            text = stringResource(R.string.cw_credits, state.creditos),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(1, 2, 3, 4, 5, 6).forEach { c ->
                FilterChip(
                    colors = gradifyChipColors(),
                    selected = state.creditos == c,
                    onClick = { onCreditosChange(c) },
                    label = { Text("$c") }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = state.nombre.isNotBlank() && state.periodo.isNotBlank()
        ) {
            Text(stringResource(R.string.onboard_next))
        }
    }
}

// ── Paso 2: Escala ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step2Escala(
    state: WizardState,
    onTemplateChange: (ScaleTemplate) -> Unit,
    onNotaAprobacionChange: (Float) -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.wizard_step2_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = stringResource(R.string.cw_scale_q),
            style = MaterialTheme.typography.bodyLarge
        )

        // País/escala en lista desplegable: antes eran 10 chips que empujaban el campo de
        // aprobación fuera de la pantalla.
        var escalaAbierta by remember { mutableStateOf(false) }
        val actual = ScaleTemplate.entries.firstOrNull {
            state.escalaMin == it.min && state.escalaMax == it.max && state.notaAprobacion == it.aprobacion
        } ?: ScaleTemplate.entries.firstOrNull { state.escalaMin == it.min && state.escalaMax == it.max }
        ExposedDropdownMenuBox(expanded = escalaAbierta, onExpandedChange = { escalaAbierta = it }) {
            OutlinedTextField(
                value = actual?.let { stringResource(it.label) } ?: stringResource(R.string.scale_custom),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.wizard_step2_title)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(escalaAbierta) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = escalaAbierta, onDismissRequest = { escalaAbierta = false }) {
                ScaleTemplate.entries.forEach { t ->
                    DropdownMenuItem(
                        text = { Text(stringResource(t.label)) },
                        onClick = { onTemplateChange(t); escalaAbierta = false },
                        leadingIcon = if (t == actual) { { Icon(Icons.Default.Check, null) } } else null
                    )
                }
            }
        }

        // Nota mínima de aprobación: escrita, la plantilla ya pone el valor típico.
        DecimalField(
            value = state.notaAprobacion,
            onValueChange = onNotaAprobacionChange,
            label = stringResource(R.string.cw_pass_label),
            min = state.escalaMin,
            max = state.escalaMax,
            suffix = "/ ${state.escalaMax.toInputString()}",
            imeAction = ImeAction.Done,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(stringResource(R.string.onboard_next))
        }
    }
}

// ── Paso 3: Componentes ────────────────────────────────────────

@Composable
private fun Step3Componentes(
    state: WizardState,
    isSaving: Boolean,
    onAddComponente: () -> Unit,
    onRemoveComponente: (Int) -> Unit,
    onNombreChange: (Int, String) -> Unit,
    onPorcentajeChange: (Int, Float) -> Unit,
    onRepartir: () -> Unit,
    otras: List<Materia>,
    onCopiar: (Materia) -> Unit,
    onGuardarOtra: () -> Unit,
    onGuardar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.wizard_step3_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = stringResource(R.string.wizard_step3_hint),
            style = MaterialTheme.typography.bodyLarge
        )

        // Copiar la estructura de otra materia: 1 toque en vez de reescribir cortes y pesos.
        if (otras.isNotEmpty()) {
            Text(stringResource(R.string.cw_copy_from), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                otras.take(10).forEach { m ->
                    AssistChip(
                        onClick = { onCopiar(m) },
                        label = { Text(m.nombre, maxLines = 1) },
                        leadingIcon = { MateriaAvatar(m.nombre, m.id, size = 22.dp) }
                    )
                }
            }
        }

        // Lista de componentes
        state.componentes.forEachIndexed { index, componente ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = componente.nombre,
                            onValueChange = { onNombreChange(index, it) },
                            label = { Text(stringResource(R.string.edit_subject_name)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        if (state.componentes.size > 1) {
                            IconButton(onClick = { onRemoveComponente(index) }) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = stringResource(R.string.wizard_delete_component),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DecimalField(
                            value = componente.porcentaje * 100f,
                            onValueChange = { onPorcentajeChange(index, it / 100f) },
                            label = stringResource(R.string.cw_weight_label),
                            min = 0f,
                            max = 100f,
                            suffix = "%",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Total de porcentajes
        val totalColor = if (state.porcentajesValidos)
            MaterialTheme.colorScheme.secondary
        else
            MaterialTheme.colorScheme.error

        Text(
            text = stringResource(if (state.porcentajesValidos) R.string.cw_total_ok else R.string.cw_total_bad, (Math.round(state.sumaPorcentajes * 10_000.0) / 100.0).toFloat().toInputString()),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = totalColor
        )

        if (!state.porcentajesValidos && state.sumaPorcentajes < 1f) {
            OutlinedButton(onClick = onRepartir, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.cw_fill_rest))
            }
        }

        // Botón agregar componente
        TextButton(
            onClick = onAddComponente,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text(stringResource(R.string.btn_agregar_componente))
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botón guardar
        Button(
            onClick = onGuardar,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !isSaving && state.porcentajesValidos
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(stringResource(R.string.btn_guardar_materia))
            }
        }
        OutlinedButton(
            onClick = onGuardarOtra,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = !isSaving && state.porcentajesValidos
        ) { Text(stringResource(R.string.cw_save_and_next)) }
    }
}
