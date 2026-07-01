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
}
