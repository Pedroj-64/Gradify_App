package com.notasapp.domain.model

import androidx.annotation.StringRes
import com.notasapp.R

/** Plantilla de escala de notas por país: rellena mínimo, máximo y nota de aprobación en el wizard. */
enum class ScaleTemplate(
    @StringRes val label: Int,
    val tipo: TipoEscala,
    val min: Float,
    val max: Float,
    val aprobacion: Float
) {
    CO(R.string.scale_co, TipoEscala.NUMERICO_5, 0f, 5f, 3f),
    MX(R.string.scale_mx, TipoEscala.NUMERICO_10, 0f, 10f, 6f),
    ES(R.string.scale_es, TipoEscala.NUMERICO_10, 0f, 10f, 5f),
    CL(R.string.scale_cl, TipoEscala.PERSONALIZADO, 1f, 7f, 4f),
    PE(R.string.scale_pe, TipoEscala.PERSONALIZADO, 0f, 20f, 11f),
    VE(R.string.scale_ve, TipoEscala.PERSONALIZADO, 0f, 20f, 10f),
    AR(R.string.scale_ar, TipoEscala.PERSONALIZADO, 1f, 10f, 4f),
    BR(R.string.scale_br, TipoEscala.NUMERICO_10, 0f, 10f, 6f),
    CR(R.string.scale_cr, TipoEscala.NUMERICO_100, 0f, 100f, 70f),
    CUSTOM(R.string.scale_custom, TipoEscala.PERSONALIZADO, 0f, 10f, 5f)
}
