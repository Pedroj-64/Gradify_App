package com.notasapp.utils

import android.content.Context
import android.os.Environment
import com.notasapp.domain.model.Materia
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utilidad para exportar una materia a formato Excel (.xlsx) usando Apache POI.
 *
 * La hoja generada tiene la siguiente estructura:
 * ```
 * | Componente   | Peso | Sub-Nota       | Peso(comp) | Nota | Aporte |
 * | Primer corte | 30%  | Taller 1       |   20%      | 4.0  | ...    |
 * |              |      | Parcial        |   80%      | 3.3  | ...    |
 * |              |      | SUBTOTAL       |            | 3.5  | 1.05   |
 * | PROMEDIO FINAL                                    | 3.8  |        |
 * ```
 *
 * El archivo se guarda en la carpeta Descargas del dispositivo.
 */
@Singleton
class ExcelExporter @Inject constructor() {

    /**
     * Exporta la materia a un archivo .xlsx.
     *
     * @param context Context para acceder al almacenamiento.
     * @param materia Materia a exportar (con componentes y sub-notas).
     * @return Ruta absoluta del archivo generado.
     * @throws Exception Si falla la escritura del archivo.
     */
    fun exportar(context: Context, materia: Materia): String {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("${materia.nombre} - ${materia.periodo}")

        // ── Estilos ─────────────────────────────────────────────
        val headerStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.ROYAL_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            val font = workbook.createFont().apply {
                bold = true
                color = IndexedColors.WHITE.index
            }
            setFont(font)
            alignment = HorizontalAlignment.CENTER
            setBorderBottom(BorderStyle.THIN)
        }

        val componenteStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.LIGHT_CORNFLOWER_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            val font = workbook.createFont().apply { bold = true }
            setFont(font)
        }

        val totalStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.LEMON_CHIFFON.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            val font = workbook.createFont().apply { bold = true }
            setFont(font)
        }

        // ── Encabezado ───────────────────────────────────────────
        val headers = arrayOf("Componente", "Peso", "Sub-Nota", "Peso (del corte)", "Nota", "Aporte")
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { col, title ->
            headerRow.createCell(col).apply {
                setCellValue(title)
                cellStyle = headerStyle
            }
        }

        var rowIndex = 1

        // ── Datos ────────────────────────────────────────────────
        materia.componentes.forEach { componente ->
            val componenteRow = sheet.createRow(rowIndex++)
            componenteRow.createCell(0).setCellValue(componente.nombre)
            componenteRow.createCell(1).setCellValue("${componente.porcentajeDisplay}%")
            componenteRow.getCell(0)?.cellStyle = componenteStyle
            componenteRow.getCell(1)?.cellStyle = componenteStyle

            // Sub-notas del componente
            componente.subNotas.forEach { subNota ->
                val subRow = sheet.createRow(rowIndex++)
                subRow.createCell(2).setCellValue(subNota.descripcion)
                subRow.createCell(3).setCellValue("${(subNota.porcentajeDelComponente * 100).toInt()}%")
                subRow.createCell(4).setCellValue(subNota.valor?.toDouble() ?: 0.0)
                subRow.createCell(5).setCellValue(
                    subNota.aporteAlComponente?.toDouble() ?: 0.0
                )
            }

            // Fila de subtotal del componente
            val subtotalRow = sheet.createRow(rowIndex++)
            subtotalRow.createCell(2).apply {
                setCellValue("SUBTOTAL ${componente.nombre}")
                cellStyle = totalStyle
            }
            subtotalRow.createCell(4).apply {
                setCellValue(componente.promedio?.toDouble() ?: 0.0)
                cellStyle = totalStyle
            }
            subtotalRow.createCell(5).apply {
                setCellValue(componente.aporteAlFinal?.toDouble() ?: 0.0)
                cellStyle = totalStyle
            }
        }

        // ── Fila de promedio final ───────────────────────────────
        val finalRow = sheet.createRow(rowIndex)
        finalRow.createCell(0).apply {
            setCellValue("PROMEDIO FINAL")
            cellStyle = totalStyle
        }
        finalRow.createCell(4).apply {
            setCellValue(materia.promedio?.toDouble() ?: 0.0)
            cellStyle = totalStyle
        }

        // Auto-size columnas
        (0..5).forEach { sheet.autoSizeColumn(it) }

        // ── Guardar archivo ──────────────────────────────────────
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
        val fileName = "${materia.nombre.replace(" ", "_")}_$timestamp.xlsx"

        val downloadsDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
        val file = File(downloadsDir, fileName)

        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()

        Timber.i("Excel exportado: ${file.absolutePath}")
        return file.absolutePath
    }
}
