package com.notasapp.ui.materia.detail

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GradeInputTest {

    @Test fun `el teclado solo deja pasar digitos con decimal opcional`() {
        listOf("", "0", "4", "4.5", "4,5", "3.75", "100", "4.").forEach {
            assertTrue("'$it' debería aceptarse", GRADE_INPUT.matches(it))
        }
        listOf("NaN", "1e5", "Infinity", "5f", "-1", "4.555", "1.2.3", "abc", " 4").forEach {
            assertFalse("'$it' debería rechazarse", GRADE_INPUT.matches(it))
        }
    }

    @Test fun `un cero real es valido y fuera de rango se marca`() {
        assertFalse(isGradeInvalid("0", 5f))
        assertFalse(isGradeInvalid("5", 5f))
        assertFalse(isGradeInvalid("4,5", 5f))     // coma decimal
        assertFalse(isGradeInvalid("", 5f))        // vacío = sin nota, no es error
        assertTrue(isGradeInvalid("5.01", 5f))
        assertTrue(isGradeInvalid("50", 5f))
        assertTrue(isGradeInvalid(".", 5f))        // aún sin dígitos
    }

    @Test fun `el formato de edicion no inventa ceros ni redondea`() {
        org.junit.Assert.assertEquals("4", 4.0f.toInputString())
        org.junit.Assert.assertEquals("3.75", 3.75f.toInputString())
        org.junit.Assert.assertEquals("0", 0f.toInputString())
        org.junit.Assert.assertEquals("4.2", 4.2f.toInputString())
    }
}
