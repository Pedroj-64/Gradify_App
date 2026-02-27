package com.notasapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.notasapp.navigation.NotasNavGraph
import com.notasapp.ui.theme.NotasAppTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Actividad principal y único punto de entrada de la app.
 *
 * Responsabilidades:
 * - Instalar SplashScreen
 * - Habilitar edge-to-edge
 * - Lanzar el NavGraph de Compose
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Instalar SplashScreen antes del super.onCreate()
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotasAppContent()
        }
    }
}

@Composable
private fun NotasAppContent() {
    NotasAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NotasNavGraph()
        }
    }
}
