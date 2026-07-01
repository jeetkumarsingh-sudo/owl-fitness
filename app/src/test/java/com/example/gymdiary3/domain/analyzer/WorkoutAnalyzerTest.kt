package com.example.gymdiary3.domain.analyzer

import com.example.gymdiary3.domain.model.WorkoutSet
import com.example.gymdiary3.domain.model.SessionWithSets
import com.example.gymdiary3.domain.model.WorkoutSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutAnalyzerTest {

    private val testSets = listOf(
        WorkoutSet(1, 1000L, "Chest", "Bench Press", 1, 10, 100.0, false, 1),
        WorkoutSet(2, 1001L, "Chest", "Bench Press", 2, 8, 100.0, false, 1),
        WorkoutSet(3, 2000L, "Chest", "Bench Press", 1, 10, 105.0, false, 2),
        WorkoutSet(4, 2001L, "Chest", "Bench Press", 2, 8, 105.0, false, 2),
        WorkoutSet(5, 3000L, "Chest", "Bench Press", 1, 10, 110.0, false, 3)
    )

    @Test
    fun `getExerciseStats returns correct best weight`() {
        val stats = WorkoutAnalyzer.getExerciseStats("Bench Press", testSets)
        assertEquals(110.0, stats.bestWeight, 0.01)
    }

    @Test
    fun `getExerciseStats returns correct total volume`() {
        val stats = WorkoutAnalyzer.getExerciseStats("Bench Press", testSets)
        val expectedVolume = (100.0 * 10) + (100.0 * 8) + (105.0 * 10) + (105.0 * 8) + (110.0 * 10)
        assertEquals(expectedVolume, stats.totalVolume, 0.01)
    }

    @Test
    fun `getExerciseStats detects PR when last session exceeds previous`() {
        val stats = WorkoutAnalyzer.getExerciseStats("Bench Press", testSets)
        assertTrue(stats.isPR)
        assertTrue(stats.trend > 0)
    }

    @Test
    fun `getExerciseStats handles empty input`() {
        val stats = WorkoutAnalyzer.getExerciseStats("Bench Press", emptyList())
        assertEquals(0.0, stats.bestWeight, 0.01)
        assertFalse(stats.isPR)
    }

    @Test
    fun `getExerciseStats handles single session`() {
        val singles = listOf(
            WorkoutSet(1, 1000L, "Chest", "Bench Press", 1, 10, 100.0, false, 1)
        )
        val stats = WorkoutAnalyzer.getExerciseStats("Bench Press", singles)
        assertEquals(100.0, stats.bestWeight, 0.01)
        assertEquals(0.0, stats.previousSessionWeight, 0.01)
        assertFalse(stats.isPR)
    }

    @Test
    fun `getTrendLabel returns correct labels`() {
        assertEquals("Progressing", WorkoutAnalyzer.getTrendLabel(5.0))
        assertEquals("Regression", WorkoutAnalyzer.getTrendLabel(-5.0))
        assertEquals("Stable", WorkoutAnalyzer.getTrendLabel(0.0))
    }

    @Test
    fun `getSuggestedWeight returns correct increments`() {
        assertEquals(11.25, WorkoutAnalyzer.getSuggestedWeight(10.0), 0.01)
        assertEquals(52.5, WorkoutAnalyzer.getSuggestedWeight(50.0), 0.01)
        assertEquals(105.0, WorkoutAnalyzer.getSuggestedWeight(100.0), 0.01)
    }

    @Test
    fun `isValidSession returns false for zero volume`() {
        val session = SessionWithSets(
            session = WorkoutSession(1, 1000L, 2000L),
            sets = listOf(
                WorkoutSet(1, 1000L, "Chest", "Bench Press", 1, 0, 0.0, false, 1)
            )
        )
        assertFalse(WorkoutAnalyzer.isValidSession(session))
    }

    @Test
    fun `isValidSet rejects negative weight`() {
        assertFalse(WorkoutAnalyzer.isValidSet(-1.0, 10))
        assertFalse(WorkoutAnalyzer.isValidSet(0.0, 0))
        assertTrue(WorkoutAnalyzer.isValidSet(0.0, 1))
    }

    @Test
    fun `get1RMHistory returns sorted pairs`() {
        val history = WorkoutAnalyzer.get1RMHistory(testSets)
        assertTrue(history.isNotEmpty())
        assertEquals(history.sortedBy { it.first }, history)
    }

    @Test
    fun `getVolumeHistory returns chronological entries`() {
        val sessions = listOf(
            SessionWithSets(WorkoutSession(1, 1000L, 2000L), testSets.take(2)),
            SessionWithSets(WorkoutSession(2, 3000L, 4000L), testSets.drop(2).take(2))
        )
        val volume = WorkoutAnalyzer.getVolumeHistory(sessions)
        assertTrue(volume.isNotEmpty())
    }
}
