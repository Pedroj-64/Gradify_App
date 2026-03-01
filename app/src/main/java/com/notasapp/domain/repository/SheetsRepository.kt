package com.notasapp.domain.repository

import android.content.Intent
import com.notasapp.data.remote.NetworkResult
import com.notasapp.domain.model.Materia

/**
 * Contrato para la sincronización con Google Sheets.
 *
 * Cada materia se exporta a su propio spreadsheet.
 * El ID del spreadsheet queda guardado en [Materia.googleSheetsId] (y en Room)
 * para actualizaciones posteriores sin crear duplicados.
 */
interface SheetsRepository {

    /**
     * Sincroniza [materia] con Google Sheets usando la cuenta asociada a [userEmail].
     *
     * - Si [materia.googleSheetsId] es null → crea un spreadsheet nuevo.
     * - Si ya existe → limpia el contenido y lo sobreescribe.
     *
     * @return [NetworkResult.Success] con el spreadsheet ID generado/usado,
     *         [NetworkResult.Error] con [NetworkResult.Error.needsUserRecovery] = true
     *         cuando se requiere conceder permisos OAuth2 (el campo [intent] lleva
     *         el [Intent] de recuperación), o un error genérico de red/servidor.
     */
    suspend fun syncMateria(
        materia: Materia,
        userEmail: String
    ): NetworkResult<String>

    /**
     * Sincroniza todas las materias del usuario [userEmail] con Sheets.
     * Se usa desde el [SheetsSyncWorker] para la sincronización periódica.
     *
     * @return [NetworkResult.Success] con la cantidad de materias sincronizadas.
     */
    suspend fun syncAllMaterias(userEmail: String): NetworkResult<Int>
}
