package com.notasapp.ui.components

import com.notasapp.R
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Tipos de permisos que la app solicita al usuario.
 */
enum class PermissionType(
    @StringRes val title: Int,
    val icon: ImageVector,
    @StringRes val explanation: Int,
    @StringRes val whatWeUseItFor: Int,
    @StringRes val whatHappensIfDenied: Int
) {
    NOTIFICATIONS(
        title = R.string.perm_notif_title,
        icon = Icons.Default.Notifications,
        explanation = R.string.perm_notif_expl,
        whatWeUseItFor = R.string.perm_notif_use,
        whatHappensIfDenied = R.string.perm_notif_denied
    )
}

/**
 * Diálogo de explicación de permisos.
 *
 * Se muestra ANTES de la solicitud del sistema para que el usuario
 * entienda por qué la app necesita el permiso.
 *
 * @param permissionType Tipo de permiso a explicar
 * @param onAccept El usuario aceptó → proceder a solicitar el permiso
 * @param onDeny El usuario rechazó → no solicitar
 */
@Composable
fun PermissionExplanationDialog(
    permissionType: PermissionType,
    onAccept: () -> Unit,
    onDeny: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDeny,
        icon = {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                text = stringResource(R.string.perm_needed),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icono + nombre del permiso
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            permissionType.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(permissionType.title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Explicación
                Text(
                    text = stringResource(permissionType.explanation),
                    style = MaterialTheme.typography.bodyMedium
                )

                // Para qué lo usamos
                Column {
                    Text(
                        text = stringResource(R.string.perm_why),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(permissionType.whatWeUseItFor),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Qué pasa si no lo da
                Column {
                    Text(
                        text = stringResource(R.string.perm_if_no),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = stringResource(permissionType.whatHappensIfDenied),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Nota de privacidad
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.perm_privacy),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onAccept) {
                Text(stringResource(R.string.perm_allow))
            }
        },
        dismissButton = {
            TextButton(onClick = onDeny) {
                Text(stringResource(R.string.perm_not_now))
            }
        }
    )
}

/**
 * Composable que maneja el flujo completo de solicitud de permiso
 * de notificaciones (Android 13+):
 * 1. Muestra diálogo de explicación
 * 2. Si acepta, solicita el permiso del sistema
 * 3. Reporta el resultado
 *
 * @param onResult True si el permiso fue concedido, false si fue denegado
 * @param onDismiss El usuario cerró sin aceptar
 */
@Composable
fun NotificationPermissionFlow(
    onResult: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var showExplanation by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onResult(granted)
    }

    if (showExplanation) {
        PermissionExplanationDialog(
            permissionType = PermissionType.NOTIFICATIONS,
            onAccept = {
                showExplanation = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    onResult(true) // Pre-13 no necesita permiso runtime
                }
            },
            onDeny = onDismiss
        )
    }
}
