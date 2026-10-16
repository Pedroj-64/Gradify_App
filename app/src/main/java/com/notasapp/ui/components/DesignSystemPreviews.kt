package com.notasapp.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.notasapp.ui.theme.NotasAppTheme

@Preview(name = "Claro", showBackground = true)
@Preview(name = "Oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DesignSystemStatesPreview() {
    NotasAppTheme {
        Surface {
            Column(Modifier.padding(16.dp)) {
                EmptyState(Icons.Default.School, "Sin materias", "Agrega la primera para empezar")
                ErrorBanner("No se pudo cargar", onRetry = {})
                ResultCard(Icons.Default.CheckCircle, "Necesitas 3.40", "Promedio en las 2 evaluaciones pendientes", Tone.OK)
                LoadingBlock(text = "Cargando…")
            }
        }
    }
}
