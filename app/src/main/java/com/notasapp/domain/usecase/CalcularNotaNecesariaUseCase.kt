package com.notasapp.domain.usecase

import com.notasapp.domain.model.Componente
import javax.inject.Inject

/**
 * Caso de uso: calcula la nota necesaria en los componentes faltantes
 * para que el usuario alcance su meta de nota final.
 *
 * Fórmula:
 *   meta = Σ(aporte_existentes) + Σ(nota_necesaria_i * porcentaje_i)
 *
 *   nota_necesaria_para_restante = (meta - aporte_existentes) / Σ(porcentaje_faltantes)
 *
 * Si la nota necesaria supera [escalaMax], el objetivo es imposible con la escala.
 */
class CalcularNotaNecesariaUseCase @Inject constructor() {

    /**
     * @param componentes       Lista completa de componentes con sus notas actuales.
     * @param metaFinal         Nota objetivo del usuario en la escala de la materia.
     * @param escalaMax         Nota máxima posible de la materia.
     * @return [Resultado] con la nota necesaria o el estado correspondiente.
     */
    operator fun invoke(
        componentes: List<Componente>,
        metaFinal: Float,
        escalaMax: Float
    ): Resultado {
        val aporteExistente = componentes
            .filter { it.aporteAlFinal != null }
            .sumOf { it.aporteAlFinal!!.toDouble() }
            .toFloat()

        val faltantes = componentes.filter { it.aporteAlFinal == null }

        if (faltantes.isEmpty()) {
            // Todos los componentes tienen nota: mostrar promedio real
            return Resultado.MetaYaAlcanzada(aporteExistente >= metaFinal)
        }

        val sumaPorcentajeFaltantes = faltantes.sumOf { it.porcentaje.toDouble() }.toFloat()
        if (sumaPorcentajeFaltantes == 0f) return Resultado.Error("Suma de porcentajes = 0")

        val notaNecesaria = (metaFinal - aporteExistente) / sumaPorcentajeFaltantes

        return when {
            notaNecesaria <= 0f -> Resultado.MetaYaAlcanzada(true)
            notaNecesaria > escalaMax -> Resultado.Imposible(notaNecesaria)
            else -> Resultado.Posible(
                notaNecesaria = notaNecesaria,
                componentesFaltantes = faltantes.map { it.id }
            )
        }
    }

    /** Resultado sellado del caso de uso. */
    sealed class Resultado {

        /** La meta ya fue alcanzada con las notas actuales. */
        data class MetaYaAlcanzada(val aprobado: Boolean) : Resultado()

        /**
         * La meta es alcanzable. El usuario necesita sacar [notaNecesaria]
         * en promedio en todos los [componentesFaltantes].
         */
        data class Posible(
            val notaNecesaria: Float,
            val componentesFaltantes: List<Long>
        ) : Resultado()

        /** La nota necesaria supera [escalaMax]: objetivo imposible. */
        data class Imposible(val notaNecesariaCalculada: Float) : Resultado()

        data class Error(val mensaje: String) : Resultado()
    }
}
