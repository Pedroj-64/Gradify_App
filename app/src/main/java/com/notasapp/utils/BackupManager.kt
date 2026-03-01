package com.notasapp.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.notasapp.data.local.AppDatabase
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
        private val REQUIRED_MATERIA_FIELDS = listOf("nombre", "periodo")
        private val REQUIRED_COMPONENTE_FIELDS = listOf("nombre", "porcentaje")
        private val REQUIRED_SUBNOTA_FIELDS = listOf("descripcion", "porcentajeDelComponente")
    }

    // ── Export ────────────────────────────────────────────────────────────────

    /**
     * Serializa todos los datos del usuario a JSON, los guarda en caché y
     * construye un [Intent] `ACTION_SEND` listo para lanzar con [startActivity].
     *
     * @param db        Instancia de Room (inyectada en el ViewModel).
     * @param usuarioId ID del usuario cuyas materias se exportarán.
     * @return          Intent de compartición con el archivo adjunto.
     */
    suspend fun buildExportIntent(db: AppDatabase, usuarioId: String): Intent {
        val json     = buildJson(db, usuarioId)
        val fileName = "notasapp_backup_${System.currentTimeMillis()}.json"
        val file     = File(context.cacheDir, fileName).also { it.writeText(json) }
        val uri      = FileProvider.getUriForFile(
            context,
            "${context.packageName}$AUTHORITY_SUFFIX",
            file
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "NotasApp – Backup de Notas")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
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
        val jsonStr = context.contentResolver.openInputStream(uri)
            ?.bufferedReader()?.use { it.readText() }
            ?: error("No se pudo leer el archivo de backup")

        return parseAndInsert(jsonStr, db, usuarioId)
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private suspend fun buildJson(db: AppDatabase, usuarioId: String): String {
        val root = JSONObject().apply {
            put("version",    BACKUP_VERSION)
            put("exportedAt", System.currentTimeMillis())
            put("appVersion", "1.1.0")
            put("usuarioId",  usuarioId)
        }

        val matConComp = db.materiaDao().getMateriasConComponentesOnce(usuarioId)
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
                put("googleSheetsId",      m.googleSheetsId ?: JSONObject.NULL)
                put("ultimaModificacionMs",m.ultimaModificacionMs)
            }

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

    private suspend fun parseAndInsert(
        jsonStr: String,
        db: AppDatabase,
        usuarioId: String
    ): Int {
        return try {
            val root = JSONObject(jsonStr)

            // Schema validation
            validateSchema(root)

            val version = root.optInt("version", 1)
            val materiasArray = root.getJSONArray("materias")
            var count = 0

            for (i in 0 until materiasArray.length()) {
                val mObj = materiasArray.getJSONObject(i)

                // Validate required fields
                REQUIRED_MATERIA_FIELDS.forEach { field ->
                    require(mObj.has(field)) { "Campo '$field' faltante en materia #${i + 1}" }
                }

                val matEntity = MateriaEntity(
                    usuarioId            = usuarioId,
                    nombre               = mObj.getString("nombre"),
                    periodo              = mObj.getString("periodo"),
                    profesor             = mObj.optString("profesor")
                        .takeIf { it.isNotBlank() && it != "null" },
                    creditos             = mObj.optInt("creditos", 0),
                    escalaMin            = mObj.getDouble("escalaMin").toFloat(),
                    escalaMax            = mObj.getDouble("escalaMax").toFloat(),
                    notaAprobacion       = mObj.getDouble("notaAprobacion").toFloat(),
                    tipoEscala           = mObj.getString("tipoEscala"),
                    googleSheetsId       = mObj.optString("googleSheetsId")
                        .takeIf { it.isNotBlank() && it != "null" },
                    ultimaModificacionMs = mObj.optLong(
                        "ultimaModificacionMs", System.currentTimeMillis()
                    )
                )
                val newMateriaId = db.materiaDao().insert(matEntity)

                val compArray = mObj.getJSONArray("componentes")
                for (j in 0 until compArray.length()) {
                    val cObj = compArray.getJSONObject(j)

                    REQUIRED_COMPONENTE_FIELDS.forEach { field ->
                        require(cObj.has(field)) {
                            "Campo '$field' faltante en componente #${j + 1} de materia '${matEntity.nombre}'"
                        }
                    }

                    val compEntity = ComponenteEntity(
                        materiaId   = newMateriaId,
                        nombre      = cObj.getString("nombre"),
                        porcentaje  = cObj.getDouble("porcentaje").toFloat(),
                        orden       = cObj.getInt("orden"),
                        fechaLimite = if (cObj.isNull("fechaLimite")) null
                                      else cObj.getLong("fechaLimite")
                    )
                    val newCompId = db.componenteDao().insert(compEntity)

                    val snArray = cObj.getJSONArray("subNotas")
                    for (k in 0 until snArray.length()) {
                        val snObj = snArray.getJSONObject(k)

                        REQUIRED_SUBNOTA_FIELDS.forEach { field ->
                            require(snObj.has(field)) {
                                "Campo '$field' faltante en sub-nota #${k + 1}"
                            }
                        }

                        val newSnId = db.subNotaDao().insert(
                            SubNotaEntity(
                                componenteId            = newCompId,
                                descripcion             = snObj.getString("descripcion"),
                                porcentajeDelComponente = snObj.getDouble("porcentajeDelComponente")
                                    .toFloat(),
                                valor                   = if (snObj.isNull("valor")) null
                                                          else snObj.getDouble("valor").toFloat()
                            )
                        )

                        // Import sub-nota details (v2+)
                        if (snObj.has("detalles") && !snObj.isNull("detalles")) {
                            val detArray = snObj.getJSONArray("detalles")
                            for (l in 0 until detArray.length()) {
                                val detObj = detArray.getJSONObject(l)
                                db.subNotaDetailDao().insert(
                                    SubNotaDetailEntity(
                                        subNotaId   = newSnId,
                                        descripcion = detObj.getString("descripcion"),
                                        porcentaje  = detObj.getDouble("porcentaje").toFloat(),
                                        valor       = if (detObj.isNull("valor")) null
                                                      else detObj.getDouble("valor").toFloat()
                                    )
                                )
                            }
                        }
                    }
                }
                count++
            }

            Timber.i("Backup v$version importado exitosamente: $count materia(s)")
            count

        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Error de validación en backup JSON")
            throw IllegalStateException("Archivo de backup inválido: ${e.message}", e)
        } catch (e: Exception) {
            Timber.e(e, "Error al parsear backup JSON")
            throw e
        }
    }

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
