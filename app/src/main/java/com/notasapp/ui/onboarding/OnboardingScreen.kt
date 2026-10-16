package com.notasapp.ui.onboarding

import com.notasapp.R
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Datos de cada página del onboarding.
 */
private data class OnboardingPage(
    val icon: ImageVector,
    @StringRes val title: Int,
    @StringRes val description: Int,
    @StringRes val highlight: Int
)

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Default.School,
        title = R.string.ob1_title,
        description = R.string.ob1_desc,
        highlight = R.string.ob1_highlight
    ),
    OnboardingPage(
        icon = Icons.Default.BarChart,
        title = R.string.ob2_title,
        description = R.string.ob2_desc,
        highlight = R.string.ob2_highlight
    ),
    OnboardingPage(
        icon = Icons.Default.CalendarMonth,
        title = R.string.ob3_title,
        description = R.string.ob3_desc,
        highlight = R.string.ob3_highlight
    ),
    OnboardingPage(
        icon = Icons.Default.CloudSync,
        title = R.string.ob4_title,
        description = R.string.ob4_desc,
        highlight = R.string.ob4_highlight
    )
)

/**
 * Pantalla de onboarding que se muestra la primera vez que
 * el usuario abre la app después de iniciar sesión.
 *
 * 4 páginas deslizables con animación, indicadores de progreso,
 * botón "Siguiente" / "Empezar" en la última.
 *
 * @param onFinish Se llama cuando el usuario completa el tutorial.
 */
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    var currentPage by remember { mutableIntStateOf(0) }
    val page = pages[currentPage]
    val isLastPage = currentPage == pages.lastIndex

    // Función que marca onboarding como visto y navega
    val finish = {
        viewModel.markOnboardingComplete()
        onFinish()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // ── Botón saltar ────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            if (!isLastPage) {
                TextButton(onClick = finish) {
                    Text(stringResource(R.string.ob_skip), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // ── Contenido animado ───────────────────────────────────
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { it } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it } + fadeOut())
                } else {
                    (slideInHorizontally { -it } + fadeIn()) togetherWith
                            (slideOutHorizontally { it } + fadeOut())
                }
            },
            label = "OnboardingPage"
        ) { pageIndex ->
            val p = pages[pageIndex]
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icono grande
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = p.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(Modifier.height(36.dp))

                // Título
                Text(
                    text = stringResource(p.title),
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp, lineHeight = 40.sp),
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(16.dp))

                // Descripción
                Text(
                    text = stringResource(p.description),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                // Highlight
                Text(
                    text = stringResource(p.highlight),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // ── Indicadores de página (barras) ──────────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 24.dp)
        ) {
            pages.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .width(if (index == currentPage) 28.dp else 10.dp)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == currentPage)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        // ── Botones de navegación ───────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentPage > 0) {
                TextButton(onClick = { currentPage-- }) {
                    Text(stringResource(R.string.ob_prev))
                }
            } else {
                Spacer(Modifier.width(1.dp))
            }

            Button(
                onClick = {
                    if (isLastPage) {
                        finish()
                    } else {
                        currentPage++
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLastPage)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(if (isLastPage) R.string.ob_start else R.string.btn_siguiente),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
