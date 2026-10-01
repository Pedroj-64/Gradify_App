package com.notasapp.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.room.withTransaction
import com.notasapp.BuildConfig
import com.notasapp.data.local.AppDatabase
import com.notasapp.data.local.entities.ExamenEventEntity
import com.notasapp.data.local.entities.ComponenteEntity
import com.notasapp.data.local.entities.MateriaEntity
import com.notasapp.data.local.entities.SubNotaDetailEntity
import com.notasapp.data.local.entities.SubNotaEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestiona la exportación e importación del backup completo de datos en formato JSON.
 *
 * ## Exportar
 * Llama a [buildExportIntent] para obtener un [Intent] `ACTION_SEND` listo para
 * compartir. El archivo se guarda temporalmente en el directorio de caché de la app
 * y se expone mediante [FileProvider] (sin permisos de almacenamiento extra).
 *
 * ## Importar
 * Llama a [importFromUri] con la URI del archivo seleccionado por el usuario via SAF.
 * Las materias importadas se insertan con nuevos IDs; no se eliminan datos locales.
 *
 * ## Formato del archivo JSON v2
 * ```json
 * {
 *   "version": 2,
 *   "exportedAt": 1740000000000,
 *   "appVersion": "1.1.0",
 *   "usuarioId": "...",
 *   "materias": [
 *     {
 *       "nombre": "Matemáticas", "periodo": "2026-1", "creditos": 3, ...
 *       "componentes": [
 *         { "nombre": "Primer corte", "porcentaje": 0.3, ...
 *           "subNotas": [
 *             { "descripcion": "Taller 1", "valor": 4.0,
 *               "detalles": [
 *                 { "descripcion": "Parte 1", "porcentaje": 0.5, "valor": 4.2 }
 *               ]
 *             }
 *           ]
 *         }
 *       ]
 *     }
 *   ]
 * }
 * ```
 */
@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val BACKUP_VERSION   = 2
        private const val AUTHORITY_SUFFIX = ".provider"
        private const val BACKUP_DIR       = "backups"
        /** Tope de tamaño de un archivo de backup al importar (evita cargar archivos enormes). */
        private const val MAX_IMPORT_BYTES = 5 * 1024 * 1024
        private const val MAX_TEXT = 300
        private val REQUIRED_MATERIA_FIELDS = listOf("nombre", "periodo")
        private val REQUIRED_COMPONENTE_FIELDS = listOf("nombre", "porcentaje")
        private val REQUIRED_SUBNOTA_FIELDS = listOf("descripcion", "porcentajeDelComponente")

        /** Clave de deduplicación: la misma materia en el mismo periodo no se importa dos veces. */
        internal fun materiaKey(nombre: String, periodo: String) =
            nombre.trim().lowercase() + "|" + periodo.trim().lowercase()
    }

    // ── Export ────────────────────────────────────────────────────────────────

    /**
     * Serializa todos los datos del usuario a JSON, los guarda en caché y
     * construye un [Intent] `ACTION_SEND` listo para lanzar con [startActivity].
     */
    suspend fun buildExportIntent(db: AppDatabase, usuarioId: String): Intent {
        val json     = buildJson(db, usuarioId)
        val dir      = File(context.cacheDir, BACKUP_DIR).apply { mkdirs() }
        // Los backups compartidos antes ya no hacen falta: no dejar notas sueltas en caché.
        dir.listFiles()?.forEach { it.delete() }
        val file     = File(dir, "notasapp_backup_${System.currentTimeMillis()}.json").also { it.writeText(json) }
        val uri      = FileProvider.getUriForFile(
            context,
            "${context.packageName}$AUTHORITY_SUFFIX",
            file
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Gradify – Backup de Notas")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * JSON del backup, o `null` si el usuario no tiene materias (así el respaldo
     * automático nunca reemplaza copias buenas por un archivo vacío).
     */
    suspend fun buildJsonOrNull(db: AppDatabase, usuarioId: String): String? {
        val data = db.materiaDao().getMateriasConComponentesOnce(usuarioId)
        return if (data.isEmpty()) null else buildJson(db, usuarioId, data)
    }

    // ── Import ────────────────────────────────────────────────────────────────

    /**
     * Lee un archivo JSON desde [uri] e inserta su contenido en Room.
     *
     * Los datos se **fusionan** (insert con nuevos IDs Auto-generate). Los registros
     * locales existentes NO se borran — el backup es aditivo.
     *
     * @param uri       URI seleccionada por el usuario via SAF (Activity Result).
     * @param db        Instancia de Room.
     * @param usuarioId ID del usuario que "adoptará" las materias importadas.
     * @return          Número de materias restauradas.
     * @throws Exception Si el archivo está malformado o no pudo leerse.
     */
    suspend fun importFromUri(uri: Uri, db: AppDatabase, usuarioId: String): Int {
        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
            val out = java.io.ByteArrayOutputStream()
            val buf = ByteArray(8 * 1024)
            while (true) {
                val n = input.read(buf)
                if (n < 0) break
                out.write(buf, 0, n)
                check(out.size() <= MAX_IMPORT_BYTES) { "El archivo es demasiado grande para ser un backup" }
            }
            out.toByteArray()
        } ?: error("No se pudo leer el archivo de backup")
        val jsonStr = String(bytes, Charsets.UTF_8)

        return parseAndInsert(jsonStr, db, usuarioId)
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private suspend fun buildJson(
        db: AppDatabase,
        usuarioId: String,
        data: List<com.notasapp.data.local.relations.MateriaConComponentes>? = null
    ): String {
        val root = JSONObject().apply {
            put("version",    BACKUP_VERSION)
            put("exportedAt", System.currentTimeMillis())
            put("appVersion", BuildConfig.VERSION_NAME)
            put("usuarioId",  usuarioId)
        }

        val matConComp = data ?: db.materiaDao().getMateriasConComponentesOnce(usuarioId)
        val materiasArr = JSONArray()

        for (mcc in matConComp) {
            val m    = mcc.materia
            val mObj = JSONObject().apply {
                put("nombre",              m.nombre)
                put("periodo",             m.periodo)
                put("profesor",            m.profesor ?: JSONObject.NULL)
                put("creditos",            m.creditos)
                put("escalaMin",           m.escalaMin)
                put("escalaMax",           m.escalaMax)
                put("notaAprobacion",      m.notaAprobacion)
                put("tipoEscala",          m.tipoEscala)
                put("notaMeta",            m.notaMeta ?: JSONObject.NULL)
                put("notas",               m.notas ?: JSONObject.NULL)
                put("archivada",           m.archivada)
                put("ultimaModificacionMs",m.ultimaModificacionMs)
            }

            val evArr = JSONArray()
            for (e in db.examenEventDao().getByMateriaOnce(m.id)) {
                evArr.put(JSONObject().apply {
                    put("titulo",               e.titulo)
                    put("descripcion",          e.descripcion)
                    put("tipoEvento",           e.tipoEvento)
                    put("fechaEpochMs",         e.fechaEpochMs)
                    put("recordatorioMinutos",  e.recordatorioMinutos)
                })
            }
            mObj.put("eventos", evArr)

            val compArr = JSONArray()
            for (ccs in mcc.componentesConSubNotas) {
                val c    = ccs.componente
                val cObj = JSONObject().apply {
                    put("nombre",       c.nombre)
                    put("porcentaje",   c.porcentaje)
                    put("orden",        c.orden)
                    put("fechaLimite",  c.fechaLimite ?: JSONObject.NULL)
                }

                val snArr = JSONArray()
                for (snConDet in ccs.subNotas) {
                    val sn = snConDet.subNota
                    val snObj = JSONObject().apply {
                        put("descripcion",              sn.descripcion)
                        put("porcentajeDelComponente",  sn.porcentajeDelComponente)
                        put("valor",                    sn.valor ?: JSONObject.NULL)
                    }

                    // Exportar detalles de sub-notas compuestas
                    if (snConDet.detalles.isNotEmpty()) {
                        val detArr = JSONArray()
                        for (det in snConDet.detalles) {
                            detArr.put(JSONObject().apply {
                                put("descripcion", det.descripcion)
                                put("porcentaje",  det.porcentaje)
                                put("valor",       det.valor ?: JSONObject.NULL)
                            })
                        }
                        snObj.put("detalles", detArr)
                    }

                    snArr.put(snObj)
                }
                cObj.put("subNotas", snArr)
                compArr.put(cObj)
            }

            mObj.put("componentes", compArr)
            materiasArr.put(mObj)
        }

        root.put("materias", materiasArr)
        return root.toString(2)
    }

    internal suspend fun parseAndInsert(
        jsonStr: String,
        db: AppDatabase,
        usuarioId: String
    ): Int {
        return try {
            val root = JSONObject(jsonStr)
            validateSchema(root)

            val version = root.optInt("version", 1)
            val materiasArray = root.getJSONArray("materias")

            // Todo o nada: si una fila falla, no queda ninguna materia a medias.
            val count = db.withTransaction {
                val existentes = db.materiaDao().getMateriasConComponentesOnce(usuarioId)
                    .map { materiaKey(it.materia.nombre, it.materia.periodo) }
                    .toMutableSet()
                var importadas = 0

                for (i in 0 until materiasArray.length()) {
                    val mObj = materiasArray.getJSONObject(i)
                    REQUIRED_MATERIA_FIELDS.forEach { field ->
                        require(mObj.has(field)) { "Campo '$field' faltante en materia #${i + 1}" }
                    }
                    val nombre  = mObj.getString("nombre").trim()
                    val periodo = mObj.getString("periodo").trim()
                    require(nombre.isNotEmpty() && nombre.length <= MAX_TEXT) { "Nombre inválido en materia #${i + 1}" }
                    require(periodo.isNotEmpty() && periodo.length <= MAX_TEXT) { "Periodo inválido en '$nombre'" }

                    // Ya existe (mismo nombre y periodo): importar dos veces no duplica.
                    if (!existentes.add(materiaKey(nombre, periodo))) continue

                    val escalaMin = mObj.optFloat("escalaMin", 0f)
                    val escalaMax = mObj.optFloat("escalaMax", 5f)
                    require(escalaMax > escalaMin) { "Escala inválida en '$nombre'" }

                    val newMateriaId = db.materiaDao().insert(
                        MateriaEntity(
                            usuarioId            = usuarioId,
                            nombre               = nombre,
                            periodo              = periodo,
                            profesor             = mObj.optStringOrNull("profesor"),
                            creditos             = mObj.optInt("creditos", 0).coerceIn(0, 99),
                            escalaMin            = escalaMin,
                            escalaMax            = escalaMax,
                            notaAprobacion       = mObj.optFloat("notaAprobacion", (escalaMin + escalaMax) * 0.6f),
                            tipoEscala           = mObj.optString("tipoEscala", "NUMERICO_5").ifBlank { "NUMERICO_5" },
                            googleSheetsId       = null,
                            notaMeta             = mObj.optFloatOrNull("notaMeta"),
                            notas                = mObj.optStringOrNull("notas"),
                            archivada            = mObj.optBoolean("archivada", false),
                            ultimaModificacionMs = mObj.optLong("ultimaModificacionMs", System.currentTimeMillis())
                        )
                    )

                    val compArray = mObj.optJSONArray("componentes") ?: JSONArray()
                    for (j in 0 until compArray.length()) {
                        val cObj = compArray.getJSONObject(j)
                        REQUIRED_COMPONENTE_FIELDS.forEach { field ->
                            require(cObj.has(field)) { "Campo '$field' faltante en componente #${j + 1} de '$nombre'" }
                        }
                        val porcentaje = cObj.getDouble("porcentaje").toFloat()
                        require(porcentaje in 0f..1.0001f) { "Porcentaje inválido en componente #${j + 1} de '$nombre'" }

                        val newCompId = db.componenteDao().insert(
                            ComponenteEntity(
                                materiaId   = newMateriaId,
                                nombre      = cObj.getString("nombre").take(MAX_TEXT),
                                porcentaje  = porcentaje,
                                orden       = cObj.optInt("orden", j),
                                fechaLimite = cObj.optLongOrNull("fechaLimite")
                            )
                        )

                        val snArray = cObj.optJSONArray("subNotas") ?: JSONArray()
                        for (k in 0 until snArray.length()) {
                            val snObj = snArray.getJSONObject(k)
                            REQUIRED_SUBNOTA_FIELDS.forEach { field ->
                                require(snObj.has(field)) { "Campo '$field' faltante en sub-nota #${k + 1}" }
                            }
                            val snPct = snObj.getDouble("porcentajeDelComponente").toFloat()
                            require(snPct in 0f..1.0001f) { "Porcentaje inválido en sub-nota #${k + 1}" }
                            val snValor = snObj.optFloatOrNull("valor")
                            require(snValor == null || snValor in escalaMin..escalaMax) {
                                "Nota fuera de la escala en sub-nota #${k + 1} de '$nombre'"
                            }

                            val newSnId = db.subNotaDao().insert(
                                SubNotaEntity(
                                    componenteId            = newCompId,
                                    descripcion             = snObj.getString("descripcion").take(MAX_TEXT),
                                    porcentajeDelComponente = snPct,
                                    valor                   = snValor
                                )
                            )

                            val detArray = snObj.optJSONArray("detalles") ?: JSONArray()
                            for (l in 0 until detArray.length()) {
                                val detObj = detArray.getJSONObject(l)
                                val dPct = detObj.getDouble("porcentaje").toFloat()
                                require(dPct in 0f..1.0001f) { "Porcentaje inválido en detalle #${l + 1}" }
                                val dValor = detObj.optFloatOrNull("valor")
                                require(dValor == null || dValor in escalaMin..escalaMax) {
                                    "Nota fuera de la escala en detalle #${l + 1} de '$nombre'"
                                }
                                db.subNotaDetailDao().insert(
                                    SubNotaDetailEntity(
                                        subNotaId   = newSnId,
                                        descripcion = detObj.getString("descripcion").take(MAX_TEXT),
                                        porcentaje  = dPct,
                                        valor       = dValor
                                    )
                                )
                            }
                        }
                    }

                    // Eventos del calendario (sin alarma programada: se re-programa al editarlos).
                    val evArray = mObj.optJSONArray("eventos") ?: JSONArray()
                    for (e in 0 until evArray.length()) {
                        val eObj = evArray.getJSONObject(e)
                        db.examenEventDao().insert(
                            ExamenEventEntity(
                                materiaId           = newMateriaId,
                                titulo              = eObj.getString("titulo").take(MAX_TEXT),
                                descripcion         = eObj.optString("descripcion", ""),
                                tipoEvento          = eObj.optString("tipoEvento", "OTRO"),
                                fechaEpochMs        = eObj.getLong("fechaEpochMs"),
                                recordatorioMinutos = eObj.optInt("recordatorioMinutos", 60),
                                recordatorioProgramado = false
                            )
                        )
                    }
                    importadas++
                }
                importadas
            }

            Timber.i("Backup v$version importado: $count materia(s) nuevas")
            count

        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Error de validación en backup JSON")
            throw IllegalStateException("Archivo de backup inválido: ${e.message}", e)
        } catch (e: Exception) {
            Timber.e(e, "Error al parsear backup JSON")
            throw e
        }
    }

    private fun JSONObject.optFloat(key: String, default: Float): Float =
        if (has(key) && !isNull(key)) getDouble(key).toFloat() else default

    private fun JSONObject.optFloatOrNull(key: String): Float? =
        if (has(key) && !isNull(key)) getDouble(key).toFloat() else null

    private fun JSONObject.optLongOrNull(key: String): Long? =
        if (has(key) && !isNull(key)) getLong(key) else null

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (has(key) && !isNull(key)) getString(key).takeIf { it.isNotBlank() } else null

    /**
     * Valida la estructura básica del JSON de backup.
     * @throws IllegalArgumentException si la estructura es inválida.
     */
    private fun validateSchema(root: JSONObject) {
        require(root.has("materias")) {
            "El archivo no contiene el campo 'materias'. ¿Es un backup válido de NotasApp?"
        }
        val version = root.optInt("version", 0)
        require(version in 1..BACKUP_VERSION) {
            "Versión de backup no soportada: $version (máximo soportado: $BACKUP_VERSION)"
        }
        val materias = root.getJSONArray("materias")
        require(materias.length() > 0) {
            "El backup no contiene ninguna materia"
        }
    }
}
