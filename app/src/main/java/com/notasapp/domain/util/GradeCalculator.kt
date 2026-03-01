package com.notasapp.domain.util

import com.notasapp.domain.model.ConfiguracionNota
import com.notasapp.domain.model.ModoRedondeo
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Motor de cálculo de notas con precisión BigDecimal.
 *
 * Reemplaza la aritmética Float directa para evitar errores de punto flotante.
 * Aplica la [ConfiguracionNota] del usuario (decimales + modo de redondeo).
 *
 * Todas las funciones son puras (sin side-effects) y thread-safe.
 */
object GradeCalculator {

    /** Configuración por defecto: 2 decimales, redondeo matemático. */
    private val DEFAULT_CONFIG = ConfiguracionNota()

    // ── Redondeo ────────────────────────────────────────────────

    /**
     * Redondea [valor] según la [config] del usuario.
     */
    fun redondear(
        valor: Float,
        config: ConfiguracionNota = DEFAULT_CONFIG
    ): Float = redondearBD(valor.toBigDecimal(), config).toFloat()

    /**
     * Versión BigDecimal del redondeo.
     */
    fun redondearBD(
        valor: BigDecimal,
        config: ConfiguracionNota = DEFAULT_CONFIG
    ): BigDecimal {
        val mode = when (config.modoRedondeo) {
            ModoRedondeo.MATEMATICO -> RoundingMode.HALF_UP
            ModoRedondeo.TRUNCAR   -> RoundingMode.DOWN
            ModoRedondeo.ACADEMICO -> RoundingMode.HALF_UP // igual que matemático en BigDecimal
        }
        return if (config.modoRedondeo == ModoRedondeo.ACADEMICO) {
            // Redondeo académico: primero redondeamos a un decimal extra, después al final
            val extraDec = config.decimales + 1
            valor.setScale(extraDec, RoundingMode.HALF_UP)
                 .setScale(config.decimales, RoundingMode.HALF_UP)
        } else {
            valor.setScale(config.decimales, mode)
        }
    }

    /**
     * Formatea el valor redondeado como String para la UI.
     */
    fun display(
        valor: Float,
        config: ConfiguracionNota = DEFAULT_CONFIG
    ): String = redondearBD(valor.toBigDecimal(), config).toPlainString()

    /**
     * Formatea o devuelve "--" si el valor es null.
     */
    fun displayOrDash(
        valor: Float?,
        config: ConfiguracionNota = DEFAULT_CONFIG
    ): String = if (valor != null && valor != 0f) display(valor, config) else "--"

    // ── Cálculos de promedio ────────────────────────────────────

    /**
     * Promedio ponderado: Σ(valor_i × peso_i) / Σ(peso_i).
     * Retorna null si no hay elementos.
     */
    fun promedioPonderado(
        valoresConPeso: List<Pair<Float, Float>>,
        config: ConfiguracionNota = DEFAULT_CONFIG
    ): Float? {
        if (valoresConPeso.isEmpty()) return null
        val totalPeso = valoresConPeso.sumOf { it.second.toBigDecimal() }
        if (totalPeso.compareTo(BigDecimal.ZERO) == 0) return null

        val sumaPonderada = valoresConPeso.fold(BigDecimal.ZERO) { acc, (valor, peso) ->
            acc + valor.toBigDecimal() * peso.toBigDecimal()
        }
        return redondearBD(sumaPonderada.divide(totalPeso, 10, RoundingMode.HALF_UP), config).toFloat()
    }

    /**
     * Promedio simple de una lista de valores.
     */
    fun promedioSimple(
        valores: List<Float>,
        config: ConfiguracionNota = DEFAULT_CONFIG
    ): Float? {
        if (valores.isEmpty()) return null
        val sum = valores.fold(BigDecimal.ZERO) { acc, v -> acc + v.toBigDecimal() }
        val avg = sum.divide(valores.size.toBigDecimal(), 10, RoundingMode.HALF_UP)
        return redondearBD(avg, config).toFloat()
    }

    // ── Cálculo de nota necesaria ──────────────────────────────

    /**
     * Calcula la nota que se necesita en los componentes restantes
     * para alcanzar [meta] como nota final.
     *
     * @param aporteExistente  Puntos ya acumulados hacia la nota final.
     * @param porcentajeRestante  Fracción del curso aún sin evaluar (0.0-1.0).
     * @param meta  Nota objetivo.
     * @param escalaMax  Máximo de la escala.
     * @param config  Configuración de redondeo.
     * @return Nota necesaria o null si no aplica.
     */
    fun notaNecesaria(
        aporteExistente: Float,
        porcentajeRestante: Float,
        meta: Float,
        escalaMax: Float,
        config: ConfiguracionNota = DEFAULT_CONFIG
    ): Float? {
        if (porcentajeRestante <= 0f) return null
        val necesita = (meta.toBigDecimal() - aporteExistente.toBigDecimal())
            .divide(porcentajeRestante.toBigDecimal(), 10, RoundingMode.HALF_UP)
        val resultado = redondearBD(necesita, config).toFloat()
        return if (resultado in 0f..escalaMax) resultado else null
    }

    // ── Simulación "¿Qué pasa si saco X?" ─────────────────────

    /**
     * Simula la nota final si el usuario obtiene [notasHipoteticas] en los
     * componentes que faltan.
     *
     * @param aportesExistentes  Lista de aportes reales ya obtenidos.
     * @param notasHipoteticas   Pares (nota_hipotética, porcentaje_componente) para los faltantes.
     * @param config             Configuración de redondeo.
     * @return Nota final proyectada.
     */
    fun simularNotaFinal(
        aportesExistentes: List<Float>,
        notasHipoteticas: List<Pair<Float, Float>>,
        config: ConfiguracionNota = DEFAULT_CONFIG
    ): Float {
        val sumaExistente = aportesExistentes.fold(BigDecimal.ZERO) { acc, v ->
            acc + v.toBigDecimal()
        }
        val sumaHipotetica = notasHipoteticas.fold(BigDecimal.ZERO) { acc, (nota, pct) ->
            acc + nota.toBigDecimal() * pct.toBigDecimal()
        }
        return redondearBD(sumaExistente + sumaHipotetica, config).toFloat()
    }

    // ── Promedio ponderado por créditos ─────────────────────────

    /**
     * Calcula el promedio ponderado por créditos.
     * Σ(promedio_i × créditos_i) / Σ(créditos_i)
     *
     * @param materiasConCreditos  Pares (promedio, créditos).
     * @param config               Configuración de redondeo.
     * @return Promedio ponderado o null si no hay datos.
     */
    fun promedioPonderadoCreditos(
        materiasConCreditos: List<Pair<Float, Int>>,
        config: ConfiguracionNota = DEFAULT_CONFIG
    ): Float? {
        val validas = materiasConCreditos.filter { it.second > 0 }
        if (validas.isEmpty()) return null
        val totalCreditos = validas.sumOf { it.second }
        if (totalCreditos == 0) return null

        val sumaPonderada = validas.fold(BigDecimal.ZERO) { acc, (prom, cred) ->
            acc + prom.toBigDecimal() * cred.toBigDecimal()
        }
        val resultado = sumaPonderada.divide(totalCreditos.toBigDecimal(), 10, RoundingMode.HALF_UP)
        return redondearBD(resultado, config).toFloat()
    }
}
