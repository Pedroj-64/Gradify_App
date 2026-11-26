package com.notasapp.data.remote

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCheckerTest {
    @Test fun `una version mayor se detecta numero a numero, no como texto`() {
        assertTrue(UpdateChecker.esMasNueva("v2.3.0", "2.2.0"))
        assertTrue(UpdateChecker.esMasNueva("v2.10.0", "2.9.1"))   // como texto "2.10" < "2.9"
        assertTrue(UpdateChecker.esMasNueva("v3", "2.9.9"))
    }

    @Test fun `la misma version o una anterior no se ofrece`() {
        assertFalse(UpdateChecker.esMasNueva("v2.2.0", "2.2.0"))
        assertFalse(UpdateChecker.esMasNueva("v2.2", "2.2.0"))
        assertFalse(UpdateChecker.esMasNueva("v2.1.9", "2.2.0"))
        assertFalse(UpdateChecker.esMasNueva("v2.2.0-beta", "2.2.0"))
    }
}
