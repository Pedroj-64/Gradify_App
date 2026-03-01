package com.notasapp.domain.model

/**
 * Modelo de dominio para un semestre/período académico.
 *
 * Agrupa materias por período y calcula promedios agregados.
 * No es una entidad Room — se construye dinámicamente desde la lista de materias.
 *
 * @property periodo       Identificador del semestre, ej: "2026-1".
 * @property materias      Materias que pertenecen a este período.
 */
data class Semestre(
    val periodo: String,
    val materias: List<Materia>
) {
    /** Número de materias en este semestre. */
    val totalMaterias: Int get() = materias.size

    /** Materias con al menos una nota ingresada. */
    val materiasConNotas: List<Materia> get() = materias.filter { it.promedio != null }

    /**
     * Promedio simple del semestre (sin ponderar por créditos).
     * Null si no hay materias con notas.
     */
    val promedioSimple: Float?
        get() {
            val conNotas = materiasConNotas
            if (conNotas.isEmpty()) return null
            return conNotas.mapNotNull { it.promedio }.average().toFloat()
        }

    /**
     * Promedio ponderado por créditos.
     * Σ(promedio_i × créditos_i) / Σ(créditos_i)
     * Null si no hay materias con notas y créditos.
     */
    val promedioPonderado: Float?
        get() {
            val validas = materiasConNotas.filter { it.creditos > 0 }
            if (validas.isEmpty()) return null
            val totalCreditos = validas.sumOf { it.creditos }
            if (totalCreditos == 0) return null
            val sumaPonderada = validas.sumOf { (it.promedio!! * it.creditos).toDouble() }
            return (sumaPonderada / totalCreditos).toFloat()
        }

    /** Total de créditos del semestre. */
    val totalCreditos: Int get() = materias.sumOf { it.creditos }

    /** Créditos aprobados (materias con promedio ≥ notaAprobacion). */
    val creditosAprobados: Int
        get() = materias.filter { it.aprobado }.sumOf { it.creditos }

    /** Materias aprobadas. */
    val aprobadas: Int get() = materias.count { it.aprobado }

    /** Materias reprobadas (con nota pero no aprobadas). */
    val reprobadas: Int get() = materiasConNotas.count { !it.aprobado }

    /** Display legible del promedio ponderado. */
    val promedioPonderadoDisplay: String
        get() = promedioPonderado?.let { "%.2f".format(it) } ?: promedioSimple?.let { "%.2f".format(it) } ?: "--"
}
