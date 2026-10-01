package com.notasapp.ui.settings

import com.notasapp.utils.BackupFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SegmentedButton
import com.notasapp.ui.components.SurfaceCard
import com.notasapp.ui.components.SectionLabel
import com.notasapp.ui.components.ScreenHeader
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.material3.OutlinedTextField
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.notasapp.BuildConfig
import com.notasapp.R
import com.notasapp.domain.model.ConfiguracionNota
import com.notasapp.domain.model.ModoRedondeo
import com.notasapp.utils.getDisplayName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla de configuración y gestión de datos de la app.
 *
 * Secciones disponibles:
 * - **Datos y Backup**: exportar a JSON + restaurar desde archivo.
 * - **Acerca de**: versión e información del proyecto.
 *
 * @param onBack Callback para regresar al Home.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState          = viewModel.uiState.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }
    val context          = LocalContext.current

    // Navegar al Login cuando el logout se completa
    LaunchedEffect(uiState.loggedOut) {
        if (uiState.loggedOut) onLogout()
    }

    // SAF file picker para restaurar backup
    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.importarBackup(it) } }

    // Selector de carpeta para los respaldos automáticos (persistente aunque se desinstale la app)
    val folderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            viewModel.setBackupFolder(it)
        }
    }

    // Lanzar el share intent en cuanto esté disponible
    LaunchedEffect(uiState.shareIntent) {
        uiState.shareIntent?.let { intent ->
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.settings_share_backup)))
            viewModel.clearMessages()
        }
    }

    // Mostrar mensajes de resultado
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        val msg = uiState.successMessage ?: uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearMessages()
    }

    Scaffold(
        topBar = { ScreenHeader(title = stringResource(R.string.settings_title)) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Sección: Datos y Backup ───────────────────────────────────────
            SettingsSection(title = stringResource(R.string.settings_data_backup)) {

                SettingsActionCard(
                    icon      = Icons.Default.Folder,
                    title     = stringResource(R.string.backup_folder_title),
                    subtitle  = uiState.backupFolder?.let { uri ->
                        val name = BackupFolder.displayName(android.net.Uri.parse(uri))
                        uiState.lastBackupMs?.let { ms ->
                            stringResource(R.string.backup_folder_last, name, formatDate(ms))
                        } ?: name
                    } ?: stringResource(R.string.backup_folder_none),
                    ctaLabel  = stringResource(R.string.backup_folder_choose),
                    isLoading = false,
                    onClick   = { folderLauncher.launch(null) }
                )

                if (uiState.backupFolder != null) {
                    SettingsActionCard(
                        icon      = Icons.Default.Backup,
                        title     = stringResource(R.string.backup_now),
                        subtitle  = stringResource(R.string.backup_now_subtitle),
                        ctaLabel  = stringResource(R.string.backup_now),
                        isLoading = false,
                        onClick   = { viewModel.respaldarAhora() }
                    )
                }

                SettingsActionCard(
                    icon      = Icons.Default.Backup,
                    title     = stringResource(R.string.settings_export_backup),
                    subtitle  = uiState.lastSyncMs?.let {
                        stringResource(R.string.settings_last_export, formatDate(it))
                    } ?: stringResource(R.string.settings_export_subtitle),
                    ctaLabel  = stringResource(R.string.settings_export_cta),
                    isLoading = uiState.isLoading,
                    onClick   = { viewModel.exportarBackup() }
                )

                SettingsActionCard(
                    icon        = Icons.Default.CloudDownload,
                    title       = stringResource(R.string.settings_restore_backup),
                    subtitle    = stringResource(R.string.settings_restore_subtitle),
                    ctaLabel    = stringResource(R.string.settings_select_file),
                    isLoading   = false,
                    onClick     = {
                        openFileLauncher.launch(arrayOf("application/json", "*/*"))
                    }
                )
            }


            // ── Sección: Notas y Redondeo ──────────────────────────────────
            SettingsSection(title = stringResource(R.string.settings_notes_rounding)) {
                RedondeoConfigCard(
                    config = uiState.configuracionNota,
                    onConfigChange = { viewModel.updateConfiguracionNota(it) }
                )
            }


            // ── Sección: Apariencia (Material You solo existe desde Android 12) ──
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                SettingsSection(title = stringResource(R.string.settings_appearance)) {
                  SurfaceCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.settings_dynamic_color), style = MaterialTheme.typography.bodyLarge)
                            Text(
                                stringResource(R.string.settings_dynamic_color_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        androidx.compose.material3.Switch(
                            checked = uiState.dynamicColor,
                            onCheckedChange = { viewModel.setDynamicColor(it) }
                        )
                    }
                  }
                }
            }

            // ── Sección: IA ───────────────────────────────────────────────────────
            SettingsSection(title = stringResource(R.string.settings_ai)) {
                SurfaceCard {
                    AiKeyCard(
                        currentKey = uiState.geminiApiKey,
                        onSave = { viewModel.saveGeminiApiKey(it) }
                    )
                }
            }


            // ── Sección: Idioma ───────────────────────────────────────────────────
            SettingsSection(title = stringResource(R.string.settings_language)) {
                LanguageSelectorCard()
            }

            // ── Sección: Cuenta ───────────────────────────────────────────────────
            SettingsSection(title = stringResource(R.string.settings_account)) {
                SurfaceCard(onClick = if (uiState.isLoading) null else ({ viewModel.showLogoutDialog() })) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Default.Logout,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.error,
                            modifier           = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text       = stringResource(R.string.settings_logout),
                                style      = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text  = stringResource(R.string.settings_back_to_login),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        }
                    }
                }
            }
            // ── Sección: Acerca de ────────────────────────────────────────────
            SettingsSection(title = stringResource(R.string.settings_about)) {
                SurfaceCard {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(
                                    text       = stringResource(R.string.app_name),
                                    style      = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text  = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text  = stringResource(R.string.settings_app_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text       = stringResource(R.string.settings_developed_by),
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text  = stringResource(R.string.settings_acknowledgements),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // ── Diálogo de confirmación de logout ──────────────────────────────────────────
    if (uiState.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLogoutDialog() },
            icon = {
                Icon(
                    imageVector        = Icons.Default.Logout,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(stringResource(R.string.settings_logout)) },
            text  = {
                Text(
                    stringResource(R.string.settings_logout_confirm)
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.logout() },
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.settings_logout))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissLogoutDialog() }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

// ── Componentes internos ──────────────────────────────────────────────────────

@Composable
private fun AiKeyCard(currentKey: String, onSave: (String) -> Unit) {
    var text by remember(currentKey) { mutableStateOf(currentKey) }
    var visible by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.settings_ai_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(stringResource(R.string.settings_ai_key_label)) },
            singleLine = true,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { visible = !visible }) {
                    Text(stringResource(if (visible) R.string.settings_ai_hide else R.string.settings_ai_show))
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onSave(text) }, enabled = text.trim() != currentKey) {
                Text(stringResource(R.string.btn_save))
            }
            TextButton(onClick = { uriHandler.openUri("https://aistudio.google.com/app/apikey") }) {
                Text(stringResource(R.string.settings_ai_get_key))
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title:   String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(title)
        content()
    }
}

@Composable
private fun SettingsActionCard(
    icon:      ImageVector,
    title:     String,
    subtitle:  String,
    ctaLabel:  String,
    isLoading: Boolean,
    onClick:   () -> Unit,
    modifier:  Modifier = Modifier
) {
    SurfaceCard(modifier = modifier, onClick = if (isLoading) null else onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector        = icon,
                contentDescription = ctaLabel,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            }
        }
    }
}

// ── Selector de idioma ─────────────────────────────────────────────────────

private data class LanguageOption(val tag: String, val label: String)

@Composable
private fun LanguageSelectorCard(modifier: Modifier = Modifier) {
    val systemLabel = stringResource(R.string.settings_language_system)
    val esLabel = stringResource(R.string.settings_language_es)
    val enLabel = stringResource(R.string.settings_language_en)

    val options = remember(systemLabel, esLabel, enLabel) {
        listOf(
            LanguageOption("",   systemLabel),
            LanguageOption("es", esLabel),
            LanguageOption("en", enLabel)
        )
    }

    // Resolve current selection
    val currentLocales = AppCompatDelegate.getApplicationLocales()
    val currentTag = if (currentLocales.isEmpty) "" else currentLocales.get(0)?.language ?: ""

    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { i, option ->
            SegmentedButton(
                selected = currentTag == option.tag,
                onClick = {
                    val locales = if (option.tag.isEmpty()) {
                        LocaleListCompat.getEmptyLocaleList()
                    } else {
                        LocaleListCompat.forLanguageTags(option.tag)
                    }
                    AppCompatDelegate.setApplicationLocales(locales)
                },
                shape = SegmentedButtonDefaults.itemShape(index = i, count = options.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    activeBorderColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(option.label, maxLines = 1, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

private fun formatDate(ms: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(ms))

// ── Configuración de redondeo ──────────────────────────────────────────────

@Composable
private fun RedondeoConfigCard(
    config: ConfiguracionNota,
    onConfigChange: (ConfiguracionNota) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.settings_rounding_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(12.dp))

            // Decimales
            Text(
                text = stringResource(R.string.settings_decimals),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf(1, 2, 3).forEachIndexed { i, dec ->
                    SegmentedButton(
                        selected = config.decimales == dec,
                        onClick = { onConfigChange(config.copy(decimales = dec)) },
                        shape = SegmentedButtonDefaults.itemShape(index = i, count = 3),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            activeBorderColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("$dec")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Modo de redondeo
            Text(
                text = stringResource(R.string.settings_rounding_mode),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            ModoRedondeo.entries.forEach { modo ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onConfigChange(config.copy(modoRedondeo = modo)) }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = config.modoRedondeo == modo,
                        onClick = { onConfigChange(config.copy(modoRedondeo = modo)) }
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = modo.getDisplayName(context),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
