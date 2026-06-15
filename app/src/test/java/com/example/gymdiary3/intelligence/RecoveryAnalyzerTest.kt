package com.example.gymdiary3.intelligence

import com.example.gymdiary3.domain.model.WorkoutSession
import com.example.gymdiary3.domain.model.SessionWithSets
import com.example.gymdiary3.domain.model.WorkoutSet
import com.example.gymdiary3.intelligence.model.InsightType
import com.example.gymdiary3.intelligence.model.TrainingSnapshot
import com.example.gymdiary3.intelligence.rules.RecoveryAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryAnalyzerTest {

    private val analyzer = RecoveryAnalyzer()

    @Test
    fun `warns when 6 or more sessions in 7 days`() {
        val now = System.currentTimeMillis()
        val sessions = (1..6).map { i ->
            SessionWithSets(
                session = WorkoutSession(i, now - i * 86_400_000L, now - i * 86_400_000L + 3600_000L),
                sets = listOf(WorkoutSet(i, now - i * 86_400_000L, "Chest", "Bench", 1, 10, 100.0, false, i))
            )
        }
        val snapshot = TrainingSnapshot(
            allSessions = sessions,
            recentSessions = sessions,
            setsByExercise = mapOf("Bench" to sessions.flatMap { it.sets }),
            sessionsByWeek = emptyMap(),
            totalSetsAllTime = 6,
            trainingDaysLast28 = 6,
            averageSessionsPerWeek = 6.0,
            now = now
        )
        val insights = analyzer.evaluate(snapshot)
        assertEquals(1, insights.size)
        assertEquals(InsightType.RECOVERY_CONCERN, insights.first().type)
    }

    @Test
    fun `no warning with fewer sessions`() {
        val now = System.currentTimeMillis()
        val sessions = (1..3).map { i ->
            SessionWithSets(
                session = WorkoutSession(i, now - i * 86_400_000L, now - i * 86_400_000L + 3600_000L),
                sets = listOf(WorkoutSet(i, now - i * 86_400_000L, "Chest", "Bench", 1, 10, 100.0, false, i))
            )
        }
        val snapshot = TrainingSnapshot(
            allSessions = sessions,
            recentSessions = sessions,
            setsByExercise = emptyMap(),
            sessionsByWeek = emptyMap(),
            totalSetsAllTime = 3,
            trainingDaysLast28 = 3,
            averageSessionsPerWeek = 3.0,
            now = now
        )
        val insights = analyzer.evaluate(snapshot)
        assertTrue(insights.isEmpty())
    }
}
