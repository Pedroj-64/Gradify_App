package com.notasapp.utils

import com.notasapp.domain.model.TipoEvento
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** Un evento leído de un archivo .ics, ya con la hora en milisegundos UTC. */
data class IcsEvent(
    val titulo: String,
    val descripcion: String,
    val inicioEpochMs: Long,
    val tipo: TipoEvento
)

/**
 * Resultado de leer un .ics.
 * @property recurrentes eventos con repetición (RRULE) que se omiten: importar solo su primera
 *                       fecha sería engañoso (típico en horarios de clases).
 * @property pasados     eventos que ya ocurrieron.
 */
data class IcsResult(
    val eventos: List<IcsEvent>,
    val recurrentes: Int,
    val pasados: Int
)

/**
 * Lector mínimo de iCalendar (RFC 5545): solo VEVENT con SUMMARY, DESCRIPTION y DTSTART.
 * Sirve para archivos exportados de Google Calendar, Outlook, Apple o calendarios universitarios.
 */
object IcsParser {

    const val MAX_EVENTOS = 300

    private val FECHA_HORA = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
    private val FECHA = DateTimeFormatter.ofPattern("yyyyMMdd")

    fun parse(
        texto: String,
        ahoraMs: Long = System.currentTimeMillis(),
        zona: ZoneId = ZoneId.systemDefault()
    ): IcsResult {
        // Las líneas largas se "doblan": una línea que empieza con espacio o tab continúa la anterior.
        val lineas = texto.replace("\r\n", "\n").replace("\r", "\n")
            .replace(Regex("\n[ \t]"), "")
            .split('\n')

        val eventos = mutableListOf<IcsEvent>()
        var recurrentes = 0
        var pasados = 0
        var props: MutableMap<String, Pair<String, String>>? = null   // NOMBRE -> (parámetros, valor)

        for (linea in lineas) {
            when {
                linea.equals("BEGIN:VEVENT", ignoreCase = true) -> props = mutableMapOf()
                linea.equals("END:VEVENT", ignoreCase = true) -> {
                    val p = props
                    props = null
                    if (p == null || p["STATUS"]?.second.equals("CANCELLED", ignoreCase = true)) continue
                    if (p.containsKey("RRULE")) { recurrentes++; continue }
                    val (params, valor) = p["DTSTART"] ?: continue
                    val inicio = parseFecha(params, valor, zona) ?: continue
                    if (inicio < ahoraMs) { pasados++; continue }
                    if (eventos.size >= MAX_EVENTOS) continue
                    val titulo = desescapar(p["SUMMARY"]?.second.orEmpty()).trim().ifEmpty { "Sin título" }
                    eventos += IcsEvent(
                        titulo = titulo.take(200),
                        descripcion = desescapar(p["DESCRIPTION"]?.second.orEmpty()).trim().take(1000),
                        inicioEpochMs = inicio,
                        tipo = adivinarTipo(titulo)
                    )
                }
                props != null -> {
                    val dos = linea.indexOf(':')
                    if (dos <= 0) continue
                    val cabecera = linea.substring(0, dos)
                    val nombre = cabecera.substringBefore(';').uppercase()
                    val params = cabecera.substringAfter(';', "")
                    props[nombre] = params to linea.substring(dos + 1)
                }
            }
        }
        return IcsResult(eventos.sortedBy { it.inicioEpochMs }, recurrentes, pasados)
    }

    /** DTSTART en UTC ("…Z"), con zona (TZID=…), flotante (hora local) o de día completo (8 h). */
    private fun parseFecha(params: String, valor: String, zonaLocal: ZoneId): Long? = try {
        val v = valor.trim()
        when {
            params.contains("VALUE=DATE", ignoreCase = true) && !params.contains("DATE-TIME", ignoreCase = true) ||
                v.length == 8 ->
                LocalDate.parse(v, FECHA).atTime(LocalTime.of(8, 0)).atZone(zonaLocal).toInstant().toEpochMilli()
            v.endsWith("Z", ignoreCase = true) ->
                LocalDateTime.parse(v.dropLast(1), FECHA_HORA).toInstant(ZoneOffset.UTC).toEpochMilli()
            else -> {
                val tzid = Regex("TZID=([^;:]+)", RegexOption.IGNORE_CASE).find(params)?.groupValues?.get(1)
                val zona = tzid?.let { runCatching { ZoneId.of(it.trim('"')) }.getOrNull() } ?: zonaLocal
                LocalDateTime.parse(v, FECHA_HORA).atZone(zona).toInstant().toEpochMilli()
            }
        }
    } catch (_: Exception) {
        null
    }

    private fun desescapar(s: String) = s
        .replace("\\n", "\n").replace("\\N", "\n")
        .replace("\\,", ",").replace("\\;", ";").replace("\\\\", "\\")

    /** Clasifica por palabras del título para que el evento salga con el tipo y color adecuados. */
    internal fun adivinarTipo(titulo: String): TipoEvento {
        val t = titulo.lowercase()
        return when {
            "final" in t -> TipoEvento.FINAL
            "parcial" in t || "examen" in t || "exámen" in t || "prueba" in t -> TipoEvento.PARCIAL
            "quiz" in t || "quices" in t -> TipoEvento.QUIZ
            "proyecto" in t -> TipoEvento.PROYECTO
            "exposición" in t || "exposicion" in t || "sustentación" in t || "sustentacion" in t -> TipoEvento.EXPOSICION
            "tarea" in t || "taller" in t || "entrega" in t || "laboratorio" in t -> TipoEvento.TAREA
            else -> TipoEvento.OTRO
        }
    }
}
