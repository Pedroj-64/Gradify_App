package com.notasapp.domain.usecase

import com.notasapp.domain.model.ConfiguracionNota
import com.notasapp.domain.model.Materia
import com.notasapp.domain.util.GradeCalculator
import javax.inject.Inject

/**
 * Caso de uso: calcula el promedio ponderado por créditos de un conjunto de materias.
 *
 * Utiliza [GradeCalculator] para aritmética BigDecimal con redondeo configurable.
 */
class CalcularPromedioPonderadoUseCase @Inject constructor() {

    /**
     * @param materias  Lista de materias con promedio y créditos.
     * @param config    Configuración de redondeo.
     * @return Resultado con promedios simple y ponderado.
     */
    operator fun invoke(
        materias: List<Materia>,
        config: ConfiguracionNota = ConfiguracionNota()
    ): Resultado {
        val conNotas = materias.filter { it.promedio != null }
        if (conNotas.isEmpty()) return Resultado()

        val promedioSimple = GradeCalculator.promedioSimple(
            conNotas.mapNotNull { it.promedio }, config
        )

        val conCreditos = conNotas.filter { it.creditos > 0 }
        val promedioPonderado = if (conCreditos.isNotEmpty()) {
            GradeCalculator.promedioPonderadoCreditos(
                conCreditos.map { it.promedio!! to it.creditos },
                config
            )
        } else null

        val totalCreditos = materias.sumOf { it.creditos }
        val creditosAprobados = materias.filter { it.aprobado }.sumOf { it.creditos }

        return Resultado(
            promedioSimple = promedioSimple,
            promedioPonderado = promedioPonderado,
            totalCreditos = totalCreditos,
            creditosAprobados = creditosAprobados,
            materiasAprobadas = materias.count { it.aprobado },
            materiasReprobadas = conNotas.count { !it.aprobado },
            totalMaterias = materias.size
        )
    }

    data class Resultado(
        val promedioSimple: Float? = null,
        val promedioPonderado: Float? = null,
        val totalCreditos: Int = 0,
        val creditosAprobados: Int = 0,
        val materiasAprobadas: Int = 0,
        val materiasReprobadas: Int = 0,
        val totalMaterias: Int = 0
    ) {
        val promedioDisplay: String
            get() = promedioPonderado?.let { "%.2f".format(it) }
                ?: promedioSimple?.let { "%.2f".format(it) }
                ?: "--"
    }
}
