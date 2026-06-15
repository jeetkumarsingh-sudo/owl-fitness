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
    fun `calculate1RM returns 0 for zero weight`() {
        assertEquals(0.0, WorkoutCalculations.calculate1RM(0.0, 10), 0.01)
    }

    @Test
    fun `calculate1RM returns 0 for zero reps`() {
        assertEquals(0.0, WorkoutCalculations.calculate1RM(100.0, 0), 0.01)
    }

    @Test
    fun `calculate1RM returns 0 for negative weight`() {
        assertEquals(0.0, WorkoutCalculations.calculate1RM(-10.0, 10), 0.01)
    }

    @Test
    fun `calculate1RM equals weight for 1 rep`() {
        assertEquals(103.33, WorkoutCalculations.calculate1RM(100.0, 1), 0.01)
    }

    @Test
    fun `calculateVolume is correct`() {
        val weight = 100.0
        val reps = 10
        assertEquals(1000.0, WorkoutCalculations.calculateVolume(weight, reps), 0.01)
    }

    @Test
    fun `calculateVolume returns 0 for zero weight`() {
        assertEquals(0.0, WorkoutCalculations.calculateVolume(0.0, 10), 0.01)
    }

    @Test
    fun `calculateVolume returns 0 for zero reps`() {
        assertEquals(0.0, WorkoutCalculations.calculateVolume(100.0, 0), 0.01)
    }

    @Test
    fun `calculateVolume returns 0 for negative weight`() {
        assertEquals(0.0, WorkoutCalculations.calculateVolume(-10.0, 10), 0.01)
    }

    @Test
    fun `calculateVolume returns 0 for bodyweight exercise`() {
        assertEquals(0.0, WorkoutCalculations.calculateVolume(0.0, 10), 0.01)
    }
}
