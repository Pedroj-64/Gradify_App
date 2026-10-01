package com.notasapp.utils

import com.notasapp.domain.model.TipoEvento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class IcsParserTest {

    private val bogota = ZoneId.of("America/Bogota")
    private val ahora = ZonedDateTime.of(2026, 10, 1, 12, 0, 0, 0, bogota).toInstant().toEpochMilli()

    private fun cal(vararg eventos: String) =
        "BEGIN:VCALENDAR\r\nVERSION:2.0\r\n" + eventos.joinToString("") + "END:VCALENDAR\r\n"

    private fun ev(vararg l: String) = "BEGIN:VEVENT\r\n" + l.joinToString("") { "$it\r\n" } + "END:VEVENT\r\n"

    private fun parse(t: String) = IcsParser.parse(t, ahora, bogota)

    @Test fun `evento en UTC se convierte a milisegundos`() {
        val r = parse(cal(ev("SUMMARY:Parcial de Cálculo", "DTSTART:20261015T150000Z")))
        assertEquals(1, r.eventos.size)
        val e = r.eventos.single()
        assertEquals("Parcial de Cálculo", e.titulo)
        assertEquals(ZonedDateTime.of(2026, 10, 15, 15, 0, 0, 0, ZoneId.of("UTC")).toInstant().toEpochMilli(), e.inicioEpochMs)
        assertEquals(TipoEvento.PARCIAL, e.tipo)
    }

    @Test fun `TZID y hora flotante usan la zona correspondiente`() {
        val r = parse(cal(
            ev("SUMMARY:Quiz 1", "DTSTART;TZID=America/Bogota:20261020T080000"),
            ev("SUMMARY:Taller", "DTSTART:20261021T080000")          // flotante = hora local
        ))
        val esperado = ZonedDateTime.of(2026, 10, 20, 8, 0, 0, 0, bogota).toInstant().toEpochMilli()
        assertEquals(esperado, r.eventos[0].inicioEpochMs)
        assertEquals(TipoEvento.QUIZ, r.eventos[0].tipo)
        assertEquals(ZonedDateTime.of(2026, 10, 21, 8, 0, 0, 0, bogota).toInstant().toEpochMilli(), r.eventos[1].inicioEpochMs)
        assertEquals(TipoEvento.TAREA, r.eventos[1].tipo)
    }

    @Test fun `evento de dia completo queda a las 8 de la manana locales`() {
        val r = parse(cal(ev("SUMMARY:Entrega final del proyecto", "DTSTART;VALUE=DATE:20261105")))
        assertEquals(ZonedDateTime.of(2026, 11, 5, 8, 0, 0, 0, bogota).toInstant().toEpochMilli(), r.eventos.single().inicioEpochMs)
        assertEquals(TipoEvento.FINAL, r.eventos.single().tipo)   // "final" gana sobre "proyecto"
    }

    @Test fun `lineas dobladas y escapes se reconstruyen`() {
        val r = parse(cal(ev("SUMMARY:Examen de Física\\, unidad 2", "DESCRIPTION:Traer calculadora\\n y formu", " lario", "DTSTART:20261201T120000Z")))
        val e = r.eventos.single()
        assertEquals("Examen de Física, unidad 2", e.titulo)
        assertEquals("Traer calculadora\n y formulario", e.descripcion)
    }

    @Test fun `eventos recurrentes pasados y cancelados se omiten y se cuentan`() {
        val r = parse(cal(
            ev("SUMMARY:Clase", "DTSTART:20261015T120000Z", "RRULE:FREQ=WEEKLY"),
            ev("SUMMARY:Viejo", "DTSTART:20250101T120000Z"),
            ev("SUMMARY:Cancelado", "DTSTART:20261016T120000Z", "STATUS:CANCELLED"),
            ev("SUMMARY:Bueno", "DTSTART:20261017T120000Z")
        ))
        assertEquals(listOf("Bueno"), r.eventos.map { it.titulo })
        assertEquals(1, r.recurrentes)
        assertEquals(1, r.pasados)
    }

    @Test fun `texto que no es un calendario no revienta`() {
        listOf("", "hola", "BEGIN:VEVENT\nSUMMARY:sin fecha\nEND:VEVENT", "BEGIN:VEVENT\nDTSTART:basura\nEND:VEVENT").forEach {
            assertTrue(parse(it).eventos.isEmpty())
        }
    }

    @Test fun `los eventos salen ordenados por fecha`() {
        val r = parse(cal(ev("SUMMARY:B", "DTSTART:20261110T120000Z"), ev("SUMMARY:A", "DTSTART:20261105T120000Z")))
        assertEquals(listOf("A", "B"), r.eventos.map { it.titulo })
    }
}
