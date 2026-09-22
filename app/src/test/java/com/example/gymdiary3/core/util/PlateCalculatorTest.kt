package com.example.gymdiary3.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlateCalculatorTest {

    @Test
    fun `kg - 100kg on a 20kg bar is 40 per side as 25 plus 15`() {
        val result = PlateCalculator.platesPerSide(100.0, 20.0, PlateCalculator.KG_PLATES)
        assertEquals(
            listOf(PlateCalculator.PlateCount(25.0, 1), PlateCalculator.PlateCount(15.0, 1)),
            result
        )
    }

    @Test
    fun `kg - uses multiple of the heaviest plate first`() {
        val result = PlateCalculator.platesPerSide(120.0, 20.0, PlateCalculator.KG_PLATES)
        assertEquals(listOf(PlateCalculator.PlateCount(25.0, 2)), result)
    }

    @Test
    fun `target at or below bar weight yields no plates`() {
        assertTrue(PlateCalculator.platesPerSide(20.0, 20.0, PlateCalculator.KG_PLATES).isEmpty())
        assertTrue(PlateCalculator.platesPerSide(15.0, 20.0, PlateCalculator.KG_PLATES).isEmpty())
    }

    @Test
    fun `fractional remainder resolves to smallest plates`() {
        val result = PlateCalculator.platesPerSide(23.5, 20.0, PlateCalculator.KG_PLATES)
        assertEquals(listOf(PlateCalculator.PlateCount(1.25, 1)), result)
    }

    @Test
    fun `lbs - 135lb on a 45lb bar is one 45 per side`() {
        val result = PlateCalculator.platesPerSide(135.0, 45.0, PlateCalculator.LBS_PLATES)
        assertEquals(listOf(PlateCalculator.PlateCount(45.0, 1)), result)
    }

    @Test
    fun `platesForUnit selects imperial set for lbs and metric otherwise`() {
        assertEquals(PlateCalculator.LBS_PLATES, PlateCalculator.platesForUnit("lbs"))
        assertEquals(PlateCalculator.LBS_PLATES, PlateCalculator.platesForUnit("LBS"))
        assertEquals(PlateCalculator.KG_PLATES, PlateCalculator.platesForUnit("kg"))
        assertEquals(PlateCalculator.KG_PLATES, PlateCalculator.platesForUnit("anything"))
    }
}
