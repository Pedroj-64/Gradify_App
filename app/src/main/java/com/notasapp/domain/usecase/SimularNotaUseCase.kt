package com.notasapp.domain.usecase

import com.notasapp.domain.model.Componente
import com.notasapp.domain.model.ConfiguracionNota
import com.notasapp.domain.util.GradeCalculator
import javax.inject.Inject

/**
 * Caso de uso: simula la nota final si el usuario obtiene notas hipotéticas
 * en los componentes que aún no tienen nota ("¿Qué pasa si saco X?").
 *
 * Recibe la lista completa de componentes y un mapa de notas hipotéticas
 * para los componentes faltantes. Retorna la nota final proyectada.
 */
class SimularNotaUseCase @Inject constructor() {

    /**
     * @param componentes       Todos los componentes de la materia.
     * @param notasHipoteticas  Mapa de componenteId → nota hipotética para los faltantes.
     * @param config            Configuración de redondeo.
     * @return [Resultado] con la nota final simulada.
     */
    operator fun invoke(
        componentes: List<Componente>,
        notasHipoteticas: Map<Long, Float>,
        config: ConfiguracionNota = ConfiguracionNota()
    ): Resultado {
        // Aportes ya existentes (componentes con nota real)
        val aportesExistentes = componentes
            .filter { it.aporteAlFinal != null }
            .map { it.aporteAlFinal!! }

        // Componentes faltantes con notas hipotéticas
        val faltantes = componentes.filter { it.aporteAlFinal == null }
        if (faltantes.isEmpty()) {
            val notaActual = aportesExistentes.sum()
            return Resultado(
                notaFinalProyectada = GradeCalculator.redondear(notaActual, config),
                completamenteEvaluada = true,
                componentesSimulados = emptyList()
            )
        }

        val simulados = faltantes.map { comp ->
            val notaHipotetica = notasHipoteticas[comp.id] ?: 0f
            ComponenteSimulado(
                componenteId = comp.id,
                nombre = comp.nombre,
                porcentaje = comp.porcentaje,
                notaHipotetica = notaHipotetica,
                aporteSimulado = GradeCalculator.redondear(
                    notaHipotetica * comp.porcentaje, config
                )
            )
        }

        val hipoteticasPares = simulados.map { it.notaHipotetica to it.porcentaje }
        val notaFinal = GradeCalculator.simularNotaFinal(
            aportesExistentes = aportesExistentes,
            notasHipoteticas = hipoteticasPares,
            config = config
        )

        return Resultado(
            notaFinalProyectada = notaFinal,
            completamenteEvaluada = false,
            componentesSimulados = simulados
        )
    }

    /**
     * Resultado de la simulación.
     */
    data class Resultado(
        /** Nota final proyectada con las notas hipotéticas. */
        val notaFinalProyectada: Float,
        /** True si todos los componentes ya tienen nota real. */
        val completamenteEvaluada: Boolean,
        /** Detalle de cada componente simulado. */
        val componentesSimulados: List<ComponenteSimulado>
    )

    /**
     * Detalle de un componente con nota hipotética.
     */
    data class ComponenteSimulado(
        val componenteId: Long,
        val nombre: String,
        val porcentaje: Float,
        val notaHipotetica: Float,
        val aporteSimulado: Float
    )
}
