package com.notasapp.utils

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract

/**
 * Carpeta de respaldos elegida por el usuario (Storage Access Framework).
 * Sobrevive a desinstalar la app porque vive fuera de ella (Descargas, Drive, etc.).
 */
object BackupFolder {

    private const val MIME_JSON = "application/json"

    fun write(context: Context, tree: Uri, fileName: String, content: String) {
        val resolver = context.contentResolver
        val parent = DocumentsContract.buildDocumentUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree))
        val doc = DocumentsContract.createDocument(resolver, parent, MIME_JSON, fileName)
            ?: error("No se pudo crear el archivo de respaldo")
        resolver.openOutputStream(doc, "w")?.use { it.write(content.toByteArray(Charsets.UTF_8)) }
            ?: error("No se pudo escribir el archivo de respaldo")
    }

    /** Conserva solo los [keep] respaldos más recientes cuyo nombre empieza con [prefix]. */
    fun prune(context: Context, tree: Uri, prefix: String, keep: Int) {
        val resolver = context.contentResolver
        val children = DocumentsContract.buildChildDocumentsUriUsingTree(
            tree, DocumentsContract.getTreeDocumentId(tree)
        )
        val found = mutableListOf<Triple<String, String, Long>>() // id, nombre, modificado
        resolver.query(
            children,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED
            ),
            null, null, null
        )?.use { c ->
            while (c.moveToNext()) {
                val name = c.getString(1) ?: continue
                if (name.startsWith(prefix)) found += Triple(c.getString(0), name, c.getLong(2))
            }
        }
        found.sortedByDescending { it.third }.drop(keep).forEach { (id, _, _) ->
            runCatching {
                DocumentsContract.deleteDocument(resolver, DocumentsContract.buildDocumentUriUsingTree(tree, id))
            }
        }
    }

    /** Nombre legible de la carpeta para mostrar en Ajustes. */
    fun displayName(tree: Uri): String =
        DocumentsContract.getTreeDocumentId(tree).substringAfter(':').ifBlank { "/" }
}
