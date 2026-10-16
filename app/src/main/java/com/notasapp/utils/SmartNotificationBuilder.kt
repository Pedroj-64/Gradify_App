package com.notasapp.utils

import android.content.Context
import com.notasapp.R
import com.notasapp.domain.model.Materia
import com.notasapp.domain.model.TipoEvento

/**
 * Genera mensajes de notificación personalizados basados en el rendimiento
 * del estudiante en la materia asociada al evento.
 *
 * Los mensajes se generan con lógica de palabras clave (no IA),
 * pero parecen inteligentes y contextuales.
 */
object SmartNotificationBuilder {

    /**
     * Contenido personalizado para una notificación de evento.
     */
    data class SmartContent(
        val title: String,
        val message: String
    )

    /**
     * Genera título y mensaje personalizados para un evento académico.
     *
     * @param eventTitle   Título del evento (e.g., "Parcial 1 de Cálculo")
     * @param eventType    Tipo del evento
     * @param materia      Materia asociada (puede ser null si no se encuentra)
     * @param minutesBefore Minutos de antelación del recordatorio
     */
    fun buildContent(
        context: Context,
        eventTitle: String,
        eventType: TipoEvento,
        materia: Materia?,
        minutesBefore: Int = 60
    ): SmartContent {
        val emoji = eventType.emoji
        val timeLabel = formatMinutes(context, minutesBefore)

        // Sin datos de materia → notificación genérica
        if (materia == null) {
            return SmartContent(
                title = "$emoji $eventTitle",
                message = context.getString(R.string.sn_generic, timeLabel)
            )
        }

        val promedio = materia.promedio
        val notaAprobacion = materia.notaAprobacion
        val yaAprobo = materia.yaAprobo
        val porcentajeEvaluado = materia.porcentajeEvaluado
        val notaNecesaria = materia.notaNecesariaParaAprobar
        val materiaNombre = materia.nombre

        // ── Clasificación por palabras clave de rendimiento ─────────────

        val (tag, motivationalMsg) = when {
            // Sin notas aún
            promedio == null -> "INICIO" to
                    context.getString(R.string.sn_inicio, materiaNombre)

            // Ya aprobó la materia
            yaAprobo -> "EXCELENTE" to
                    context.getString(R.string.sn_excelente, formatNota(promedio), formatNota(notaAprobacion), materiaNombre)

            // Aprobando con buen margen (>20% arriba de la nota mínima)
            promedio >= notaAprobacion * 1.2f -> "MUY_BIEN" to
                    context.getString(R.string.sn_muy_bien, materiaNombre, formatNota(promedio))

            // Aprobando justo
            promedio >= notaAprobacion -> "APROBANDO" to
                    context.getString(R.string.sn_aprobando, formatNota(promedio), materiaNombre)

            // Cerca de aprobar (falta poco)
            notaNecesaria != null && notaNecesaria <= materia.escalaMax * 0.7f -> "RECUPERABLE" to
                    context.getString(R.string.sn_recuperable, formatNota(notaNecesaria), materiaNombre)

            // En riesgo alto
            notaNecesaria != null && notaNecesaria <= materia.escalaMax -> "RIESGO" to
                    context.getString(R.string.sn_riesgo, formatNota(notaNecesaria), materiaNombre)

            // Situación muy difícil
            else -> "CRITICO" to
                    context.getString(R.string.sn_critico, materiaNombre, formatNota(promedio), formatNota(notaAprobacion))
        }

        // ── Título dinámico ──────────────────────────────────────────────
        val title = when (tag) {
            "EXCELENTE", "MUY_BIEN" -> "$emoji $eventTitle ⭐"
            "RIESGO", "CRITICO" -> context.getString(R.string.sn_attention, emoji, eventTitle)
            else -> "$emoji $eventTitle"
        }

        // ── Mensaje compuesto ────────────────────────────────────────────
        val progressInfo = if (porcentajeEvaluado > 0f) {
            context.getString(R.string.sn_progress, kotlin.math.round(porcentajeEvaluado * 100).toInt())
        } else ""

        val message = "$motivationalMsg$progressInfo"

        return SmartContent(title = title, message = message)
    }

    private fun formatNota(nota: Float): String = "%.1f".format(nota)

    private fun formatMinutes(context: Context, minutes: Int): String = when {
        minutes <= 0 -> context.getString(R.string.sn_now)
        minutes < 60 -> context.getString(R.string.sn_minutes, minutes)
        minutes == 60 -> context.getString(R.string.sn_hour)
        minutes < 1440 -> context.getString(R.string.sn_hours, minutes / 60)
        minutes == 1440 -> context.getString(R.string.sn_tomorrow)
        else -> context.getString(R.string.sn_days, minutes / 1440)
    }
}
