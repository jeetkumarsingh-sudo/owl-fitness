package com.example.gymdiary3.domain

import com.example.gymdiary3.core.util.WorkoutCalculations
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutCalculationsTest {

    @Test
    fun `calculate1RM is correct for standard input`() {
        val weight = 100.0
        val reps = 10
        val expected = 133.33 // 100 * (1 + 10/30)
        assertEquals(expected, WorkoutCalculations.calculate1RM(weight, reps), 0.01)
    }

    @Test
    fun `calculateVolume is correct`() {
        val weight = 100.0
        val reps = 10
        val expected = 1000.0
        assertEquals(expected, WorkoutCalculations.calculateVolume(weight, reps), 0.01)
    }
}
