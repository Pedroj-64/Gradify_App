package com.notasapp.domain.usecase

import com.notasapp.domain.model.Materia
import com.notasapp.domain.model.Semestre
import javax.inject.Inject

/**
 * Caso de uso: agrupa una lista plana de materias en objetos [Semestre] por período.
 *
 * Ordena los semestres por período descendente (más reciente primero).
 */
class AgruparPorSemestreUseCase @Inject constructor() {

    /**
     * @param materias Lista plana de materias del usuario.
     * @return Lista de [Semestre] agrupados por período (descendente).
     */
    operator fun invoke(materias: List<Materia>): List<Semestre> {
        if (materias.isEmpty()) return emptyList()
        return materias
            .groupBy { it.periodo }
            .map { (periodo, materiasDelPeriodo) ->
                Semestre(
                    periodo = periodo,
                    materias = materiasDelPeriodo
                )
            }
            .sortedByDescending { it.periodo }
    }
}
