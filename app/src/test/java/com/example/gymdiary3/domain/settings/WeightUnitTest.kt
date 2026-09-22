package com.example.gymdiary3.domain.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class WeightUnitTest {
    @Test
    fun `fromKilograms converts kilograms to pounds`() {
        assertEquals(220.5, WeightFormatter.fromKilograms(100.0, "lbs"), 0.05)
    }

    @Test
    fun `toKilograms converts pounds to kilograms`() {
        assertEquals(100.0, WeightFormatter.toKilograms(220.462, "lbs"), 0.05)
    }

    @Test
    fun `unknown preference falls back to kilograms`() {
        assertEquals(42.0, WeightFormatter.fromKilograms(42.0, "stones"), 0.0)
    }

    @Test
    fun `formatFromKilograms includes converted unit label`() {
        assertEquals("220 lbs", WeightFormatter.formatFromKilograms(100.0, "lbs", decimals = 0))
    }

    @Test
    fun `formatFromKilograms omits unit label when includeUnit is false`() {
        assertEquals("70", WeightFormatter.formatFromKilograms(70.0, "kg", decimals = 0, includeUnit = false))
        assertEquals("154", WeightFormatter.formatFromKilograms(70.0, "lbs", decimals = 0, includeUnit = false))
    }

    @Test
    fun `label maps preference to symbol`() {
        assertEquals("kg", WeightFormatter.label("kg"))
        assertEquals("lbs", WeightFormatter.label("lbs"))
        assertEquals("kg", WeightFormatter.label("unknown"))
    }
}
