package com.example.gymdiary3.intelligence

import com.example.gymdiary3.domain.model.WorkoutSet
import com.example.gymdiary3.intelligence.model.InsightType
import com.example.gymdiary3.intelligence.model.TrainingSnapshot
import com.example.gymdiary3.intelligence.rules.PlateauDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlateauDetectorTest {

    private val detector = PlateauDetector()

    private fun snapshotWithExercise(
        exercise: String,
        weightSequence: List<Double>
    ): TrainingSnapshot {
        val allSets = weightSequence.mapIndexed { idx, weight ->
            WorkoutSet(
                id = idx,
                timestamp = System.currentTimeMillis() - (weightSequence.size - idx) * 86_400_000L,
                muscle = "Chest",
                exercise = exercise,
                setNumber = 1,
                reps = 8,
                weight = weight,
                isAssisted = false,
                sessionId = idx + 1,
                rpe = null,
                notes = null
            )
        }
        return TrainingSnapshot(
            allSessions = emptyList(),
            recentSessions = emptyList(),
            setsByExercise = mapOf(exercise to allSets),
            sessionsByWeek = emptyMap(),
            totalSetsAllTime = allSets.size,
            trainingDaysLast28 = allSets.size,
            averageSessionsPerWeek = 3.0,
            now = System.currentTimeMillis()
        )
    }

    @Test
    fun `detects plateau when weight unchanged for 3 sessions`() {
        val snapshot = snapshotWithExercise("Bench Press", listOf(80.0, 80.0, 80.0))
        val insights = detector.evaluate(snapshot)
        assertEquals(1, insights.size)
        assertEquals(InsightType.PLATEAU_DETECTED, insights.first().type)
    }

    @Test
    fun `no plateau when weight is progressing`() {
        val snapshot = snapshotWithExercise("Bench Press", listOf(75.0, 77.5, 80.0))
        val insights = detector.evaluate(snapshot)
        assertTrue(insights.isEmpty())
    }
}
