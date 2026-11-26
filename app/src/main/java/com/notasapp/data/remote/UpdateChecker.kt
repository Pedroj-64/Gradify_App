package com.notasapp.data.remote

import com.notasapp.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.URL

/**
 * Pregunta a GitHub por la última release de [BuildConfig.UPDATE_REPO] ("dueño/repo").
 * Solo hace un GET a la API pública, sin cuenta ni token; con `UPDATE_REPO` vacío
 * (builds para Play Store o F-Droid, que no permiten auto-actualizarse) no hace nada.
 */
object UpdateChecker {

    data class Release(val version: String, val apkUrl: String, val pageUrl: String)

    /** La release más nueva si es posterior a la instalada; null si no hay o si falla la red. */
    suspend fun nuevaVersion(actual: String = BuildConfig.VERSION_NAME): Release? = withContext(Dispatchers.IO) {
        val repo = BuildConfig.UPDATE_REPO
        if (repo.isBlank()) return@withContext null
        try {
            val conn = (URL("https://api.github.com/repos/$repo/releases/latest").openConnection() as HttpURLConnection).apply {
                connectTimeout = 8_000
                readTimeout = 8_000
                setRequestProperty("Accept", "application/vnd.github+json")
            }
            if (conn.responseCode != 200) return@withContext null
            val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            val tag = json.getString("tag_name")
            val assets = json.getJSONArray("assets")
            val apk = (0 until assets.length()).map { assets.getJSONObject(it) }
                .firstOrNull { it.getString("name").endsWith(".apk") }
                ?.getString("browser_download_url")
            val page = json.getString("html_url")
            if (esMasNueva(tag, actual)) Release(tag.removePrefix("v"), apk ?: page, page) else null
        } catch (e: Exception) {
            Timber.w(e, "No se pudo consultar la última versión")   // sin red: se intenta en el próximo arranque
            null
        }
    }

    /** "v2.10.0" > "2.9.1"; compara número a número e ignora sufijos como "-beta". */
    fun esMasNueva(remota: String, actual: String): Boolean {
        fun partes(v: String) = v.removePrefix("v").substringBefore('-').split('.').map { it.toIntOrNull() ?: 0 }
        val r = partes(remota)
        val a = partes(actual)
        for (i in 0 until maxOf(r.size, a.size)) {
            val d = r.getOrElse(i) { 0 } - a.getOrElse(i) { 0 }
            if (d != 0) return d > 0
        }
        return false
    }
}
