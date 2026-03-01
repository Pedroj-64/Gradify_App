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
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Genera un archivo .xlsx a partir de una [Materia] con sus componentes, sub-notas y detalles.
 *
 * ## Estructura de la hoja
 * ```
 * A: Componente  B: Peso(%)  C: Actividad  D: Peso(corte)%  E: Nota  F: Aporte
 *
 * Fila componente : nombre, decimal(%)
 * Fila sub-nota simple   : "", "", descripcion, decimal(%), nota, =E*D
 * Fila sub-nota compuesta: "", "", descripcion, decimal(%), =avg(detalles), =E*D
 *   Detalle                "", "", "  · descripcion", decimal(%), nota, [info]
 * Fila SUBTOTAL  : "", "", "SUBTOTAL", "", =SUM(notas ponderadas), =E_sub*B_comp
 * Fila PROMEDIO FINAL: "PROMEDIO FINAL", …, =SUM de aportes
 * ```
 */
@Singleton
class ExcelExporter @Inject constructor() {

    /**
     * Nombre sugerido para el archivo .xlsx, útil al abrir el selector SAF.
     */
    fun sugerirNombreArchivo(materia: Materia): String {
        val safe = materia.nombre.replace(Regex("[^\\w\\s-]"), "").replace(" ", "_")
        val ts   = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
        return "${safe}_${ts}.xlsx"
    }

    /**
     * Exporta la materia a .xlsx escribiendo en el almacenamiento privado de la app.
     *
     * Usa un buffer intermedio (ByteArrayOutputStream) para garantizar que el
     * archivo no quede con 0 bytes si el stream subyacente se cierra prematuramente.
     */
    fun exportar(context: Context, materia: Materia): File {
        val bytes = buildWorkbookBytes(materia)

        val dir  = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        dir.mkdirs()
        val file = File(dir, sugerirNombreArchivo(materia))

        BufferedOutputStream(FileOutputStream(file)).use { bos ->
            bos.write(bytes)
            bos.flush()
        }

        Timber.i("Excel generado: ${file.absolutePath} (${bytes.size} bytes)")
        return file
    }

    /**
     * Exporta la materia a .xlsx escribiendo directamente en [outputStream].
     *
     * Úsalo con el URI elegido por el usuario vía SAF ([ActivityResultContracts.CreateDocument]).
     *
     * Escribe primero a un ByteArrayOutputStream interno para construir el archivo
     * completo en memoria, y luego copia los bytes al stream destino. Esto evita el
     * problema de archivos de 0 bytes cuando el OutputStream de SAF se cierra antes
     * de que POI termine de escribir.
     *
     * @param materia      Materia con sus componentes y sub-notas.
     * @param outputStream Stream abierto desde `contentResolver.openOutputStream(uri)`.
     */
    fun exportarToOutputStream(materia: Materia, outputStream: OutputStream) {
        try {
            val bytes = buildWorkbookBytes(materia)

            if (bytes.isEmpty()) {
                throw IllegalStateException("El workbook generó 0 bytes — posible error de POI")
            }

            BufferedOutputStream(outputStream).use { bos ->
                bos.write(bytes)
                bos.flush()
            }
            Timber.i("Excel escrito en OutputStream para: ${materia.nombre} (${bytes.size} bytes)")
        } catch (e: Exception) {
            Timber.e(e, "Error escribiendo Excel para ${materia.nombre}")
            throw e
        }
    }

    // ── Construcción del workbook ─────────────────────────────────────────────

    /**
     * Construye el workbook y lo serializa a un ByteArray.
     *
     * Al escribir primero a ByteArrayOutputStream se garantiza que:
     * 1. El ZIP interno de XLSX se cierra completamente.
     * 2. No hay dependencia del timing del OutputStream destino (SAF, FileProvider, etc.).
     * 3. Se puede verificar que el resultado no es vacío antes de escribir al disco.
     */
    private fun buildWorkbookBytes(materia: Materia): ByteArray {
        val workbook = buildWorkbook(materia)
        val baos = ByteArrayOutputStream()
        try {
            workbook.write(baos)
            baos.flush()
        } finally {
            workbook.close()
        }
        val bytes = baos.toByteArray()
        Timber.d("Workbook serializado: ${bytes.size} bytes para '${materia.nombre}'")
        return bytes
    }

    private fun buildWorkbook(materia: Materia): XSSFWorkbook {
        val workbook = XSSFWorkbook()
        val sheetName = "${materia.nombre} - ${materia.periodo}"
            .take(31)   // Excel sheet name limit
            .replace(Regex("[\\[\\]\\*\\?/\\\\:]"), "")
        val sheet = workbook.createSheet(sheetName)

        val df     = workbook.createDataFormat()
        val pctFmt = df.getFormat("0%")
        val numFmt = df.getFormat("0.00")

        // ── Estilos ────────────────────────────────────────────────────────
        val headerStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.ROYAL_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setFont(workbook.createFont().apply { bold = true; color = IndexedColors.WHITE.index })
            alignment = HorizontalAlignment.CENTER
            setBorderBottom(BorderStyle.THIN)
        }
        val compStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.LIGHT_CORNFLOWER_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setFont(workbook.createFont().apply { bold = true })
        }
        val compPctStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.LIGHT_CORNFLOWER_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setFont(workbook.createFont().apply { bold = true })
            dataFormat = pctFmt
        }
        val subStyle    = workbook.createCellStyle()
        val subPctStyle = workbook.createCellStyle().apply { dataFormat = pctFmt }
        val subNumStyle = workbook.createCellStyle().apply { dataFormat = numFmt }
        val detalleStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.LIGHT_YELLOW.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }
        val detallePctStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.LIGHT_YELLOW.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            dataFormat = pctFmt
        }
        val detalleNumStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.LIGHT_YELLOW.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            dataFormat = numFmt
        }
        val totalStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.LEMON_CHIFFON.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setFont(workbook.createFont().apply { bold = true })
        }
        val totalNumStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.LEMON_CHIFFON.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setFont(workbook.createFont().apply { bold = true })
            dataFormat = numFmt
        }
        val finalStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GOLD.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setFont(workbook.createFont().apply { bold = true; fontHeightInPoints = 12 })
        }
        val finalNumStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GOLD.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setFont(workbook.createFont().apply { bold = true; fontHeightInPoints = 12 })
            dataFormat = numFmt
        }

        // ── Fila de metadatos de la materia ───────────────────────────────
        sheet.createRow(0).also { row ->
            row.createCell(0).apply {
                setCellValue("${materia.nombre}  ·  Período: ${materia.periodo ?: "-"}  ·  Escala: ${materia.escalaMax.toInt()}")
                cellStyle = workbook.createCellStyle().apply {
                    setFont(workbook.createFont().apply { bold = true; fontHeightInPoints = 13 })
                }
            }
        }
        sheet.createRow(1).also { row ->
            val prof = if (!materia.profesor.isNullOrBlank()) "Profesor: ${materia.profesor}  ·  " else ""
            row.createCell(0).setCellValue("${prof}Nota mínima: ${materia.notaAprobacion}")
        }
        sheet.createRow(2)  // blank separator

        // ── Encabezados (fila 3 → Excel row 4) ───────────────────────────
        val HEADER_ROW = 3
        sheet.createRow(HEADER_ROW).also { row ->
            arrayOf("Componente", "Peso %", "Actividad", "Peso (corte) %", "Nota", "Aporte")
                .forEachIndexed { col, t ->
                    row.createCell(col).apply { setCellValue(t); cellStyle = headerStyle }
                }
        }

        var rowIndex = HEADER_ROW + 1                  // siguiente fila disponible (0-based)
        val subtotalRowIndices = mutableListOf<Int>()  // índice 0-based de cada fila SUBTOTAL

        // ── Datos — un bloque por componente ──────────────────────────────
        materia.componentes.forEach { componente ->

            val compRowIdx = rowIndex
            val compExcel  = compRowIdx + 1  // 1-based

            // Fila del componente: nombre (A) + peso decimal (B)
            sheet.createRow(rowIndex++).also { row ->
                row.createCell(0).apply { setCellValue(componente.nombre);                cellStyle = compStyle    }
                row.createCell(1).apply { setCellValue(componente.porcentaje.toDouble()); cellStyle = compPctStyle }
                row.createCell(2).cellStyle = compStyle
                row.createCell(3).cellStyle = compStyle
                row.createCell(4).cellStyle = compStyle
                row.createCell(5).cellStyle = compStyle
            }

            // Track indices where this component's sub-nota aporte values land (col F)
            val subAporteRefs = mutableListOf<String>()   // "F12", "F15", etc.

            when {
                componente.subNotas.isEmpty() -> {
                    // Componente sin sub-notas: fila informativa
                    sheet.createRow(rowIndex++).also { row ->
                        row.createCell(2).apply {
                            setCellValue("(sin actividades registradas)")
                            cellStyle = subStyle
                        }
                    }
                }
                else -> {
                    componente.subNotas.forEach { subNota ->
                        val subRowIdx  = rowIndex
                        val subExcel   = subRowIdx + 1

                        when {
                            subNota.esCompuesta -> {
                                // ── Sub-nota COMPUESTA ───────────────────────────────────
                                val detalleExcels = mutableListOf<Int>()

                                // Primero, reservar una fila para el encabezado de la sub-nota
                                // (índice se establece aquí, la fila se crea después con el rango)
                                val compuestaHeaderIdx   = rowIndex
                                val compuestaHeaderExcel = compuestaHeaderIdx + 1
                                rowIndex++   // reservar la fila (se crea después del loop de detalles)

                                // Filas de los detalles
                                val detStartExcel = rowIndex + 1
                                subNota.detalles.forEach { detalle ->
                                    val detExcel = rowIndex + 1
                                    detalleExcels.add(detExcel)
                                    sheet.createRow(rowIndex++).also { row ->
                                        row.createCell(2).apply {
                                            setCellValue("  · ${detalle.descripcion}")
                                            cellStyle = detalleStyle
                                        }
                                        row.createCell(3).apply {
                                            setCellValue(detalle.porcentaje.toDouble())
                                            cellStyle = detallePctStyle
                                        }
                                        row.createCell(4).apply {
                                            if (detalle.valor != null)
                                                setCellValue(detalle.valor.toDouble())
                                            cellStyle = detalleNumStyle
                                        }
                                        // Aporte del detalle: nota × (peso_det / sum_pesos_det)
                                        // Solo informativo, no se usa en fórmulas superiores
                                        row.createCell(5).apply {
                                            cellStyle = detalleNumStyle
                                        }
                                    }
                                }
                                val detEndExcel = rowIndex  // exclusive

                                // Ahora crear la fila del encabezado de la sub-nota compuesta
                                // (usando los índices de los detalles ya creados)
                                val totalPct = subNota.detalles.sumOf { it.porcentaje.toDouble() }
                                sheet.createRow(compuestaHeaderIdx).also { row ->
                                    row.createCell(2).apply {
                                        setCellValue("${subNota.descripcion} [compuesta]")
                                        cellStyle = workbook.createCellStyle().apply {
                                            setFont(workbook.createFont().apply { italic = true; bold = true })
                                        }
                                    }
                                    row.createCell(3).apply {
                                        setCellValue(subNota.porcentajeDelComponente.toDouble())
                                        cellStyle = subPctStyle
                                    }
                                    // Nota ponderada = SUMPRODUCT(E_dets, D_dets) / SUM(D_dets)
                                    // o simplemente SUMPRODUCT si los pesos ya suman 1
                                    row.createCell(4).apply {
                                        if (subNota.detalles.isNotEmpty()) {
                                            val eRange = "E${detStartExcel}:E${detEndExcel - 1}"
                                            val dRange = "D${detStartExcel}:D${detEndExcel - 1}"
                                            if (totalPct > 0)
                                                setCellFormula("SUMPRODUCT($eRange,$dRange)/SUM($dRange)")
                                            else
                                                setCellValue("")
                                        }
                                        cellStyle = subNumStyle
                                    }
                                    // Aporte = Nota × Peso del corte
                                    row.createCell(5).apply {
                                        setCellFormula("E${compuestaHeaderExcel}*D${compuestaHeaderExcel}")
                                        cellStyle = subNumStyle
                                    }
                                }
                                subAporteRefs.add("F${compuestaHeaderExcel}")
                            }
                            else -> {
                                // ── Sub-nota SIMPLE ──────────────────────────────────────
                                sheet.createRow(rowIndex++).also { row ->
                                    row.createCell(2).apply {
                                        setCellValue(subNota.descripcion)
                                        cellStyle = subStyle
                                    }
                                    row.createCell(3).apply {
                                        setCellValue(subNota.porcentajeDelComponente.toDouble())
                                        cellStyle = subPctStyle
                                    }
                                    row.createCell(4).apply {
                                        if (subNota.valor != null)
                                            setCellValue(subNota.valor.toDouble())
                                        cellStyle = subNumStyle
                                    }
                                    // Aporte = Nota × Peso del corte
                                    row.createCell(5).apply {
                                        setCellFormula("E${subExcel}*D${subExcel}")
                                        cellStyle = subNumStyle
                                    }
                                }
                                subAporteRefs.add("F${subExcel}")
                            }
                        }
                    }
                }
            }

            // ── Fila SUBTOTAL del componente ──────────────────────────────
            val subtotalIdx   = rowIndex
            val subtotalExcel = subtotalIdx + 1
            subtotalRowIndices.add(subtotalIdx)

            sheet.createRow(rowIndex++).also { row ->
                row.createCell(2).apply { setCellValue("SUBTOTAL"); cellStyle = totalStyle }
                row.createCell(3).cellStyle = totalStyle

                // Nota ponderada del componente = suma de aportes de sub-notas
                row.createCell(4).apply {
                    if (subAporteRefs.isNotEmpty()) {
                        setCellFormula(subAporteRefs.joinToString("+"))
                    } else {
                        setCellValue(0.0)
                    }
                    cellStyle = totalNumStyle
                }

                // Aporte al promedio final = nota_componente × peso_componente
                row.createCell(5).apply {
                    setCellFormula("E${subtotalExcel}*B${compExcel}")
                    cellStyle = totalNumStyle
                }
            }

            // Línea de separación vacía entre componentes
            sheet.createRow(rowIndex++)
        }

        // ── Fila PROMEDIO FINAL ────────────────────────────────────────────
        sheet.createRow(rowIndex).also { row ->
            row.createCell(0).apply { setCellValue("PROMEDIO FINAL"); cellStyle = finalStyle }
            row.createCell(1).cellStyle = finalStyle
            row.createCell(2).cellStyle = finalStyle
            row.createCell(3).cellStyle = finalStyle

            val formula = subtotalRowIndices
                .joinToString("+") { "F${it + 1}" }
                .ifEmpty { "0" }
            row.createCell(4).apply { setCellFormula(formula); cellStyle = finalNumStyle }
            row.createCell(5).cellStyle = finalNumStyle
        }
        rowIndex++

        // ── Fila de acumulado y estado ────────────────────────────────────
        sheet.createRow(rowIndex++).also { row ->
            row.createCell(0).apply {
                setCellValue("Acumulado: ${materia.acumuladoDisplay}")
                cellStyle = workbook.createCellStyle().apply {
                    setFont(workbook.createFont().apply { italic = true })
                }
            }
            row.createCell(2).apply {
                val evaluado = (materia.porcentajeEvaluado * 100).toInt()
                setCellValue("Evaluado: $evaluado%")
            }
            row.createCell(4).apply {
                val estado = if (materia.yaAprobo) "¡APROBADO!" else "EN PROGRESO"
                setCellValue(estado)
                cellStyle = workbook.createCellStyle().apply {
                    setFont(workbook.createFont().apply {
                        bold = true
                        color = if (materia.yaAprobo) IndexedColors.GREEN.index else IndexedColors.RED.index
                    })
                }
            }
        }

        if (materia.yaAprobo) {
            sheet.createRow(rowIndex++).also { row ->
                row.createCell(0).apply {
                    setCellValue("¡Felicidades! Ya superaste el mínimo de ${materia.notaAprobacion} para aprobar.")
                    cellStyle = workbook.createCellStyle().apply {
                        setFont(workbook.createFont().apply { bold = true; fontHeightInPoints = 11 })
                    }
                }
            }
        } else {
            materia.notaNecesariaParaAprobar?.let { necesita ->
                sheet.createRow(rowIndex++).also { row ->
                    row.createCell(0).apply {
                        setCellValue("Necesitas promediar ≈ ${"%.2f".format(necesita)} en lo restante para aprobar.")
                    }
                }
            }
        }

        // Auto-size columnas
        (0..5).forEach { col ->
            try { sheet.autoSizeColumn(col) } catch (_: Exception) { }
        }

        return workbook
    }
}
