package com.example.gymdiary3.intelligence

import com.example.gymdiary3.domain.model.SessionWithSets
import com.example.gymdiary3.domain.model.WorkoutSession
import com.example.gymdiary3.domain.model.WorkoutSet
import com.example.gymdiary3.intelligence.model.InsightType
import com.example.gymdiary3.intelligence.model.TrainingSnapshot
import com.example.gymdiary3.intelligence.rules.VolumeAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

class VolumeAnalyzerTest {

    private val analyzer = VolumeAnalyzer()

    @Test
    fun `detects volume spike when current week volume is 20 percent higher`() {
        val now = System.currentTimeMillis()
        val currentWeekSession = SessionWithSets(
            session = WorkoutSession(1, now, now + 1000),
            sets = listOf(WorkoutSet(1, now, "Chest", "Bench", 1, 10, 150.0, false, 1))
        )
        val lastWeekSession = SessionWithSets(
            session = WorkoutSession(2, now - TimeUnit.DAYS.toMillis(7), now - TimeUnit.DAYS.toMillis(7) + 1000),
            sets = listOf(WorkoutSet(2, now - TimeUnit.DAYS.toMillis(7), "Chest", "Bench", 1, 10, 100.0, false, 2))
        )

        val snapshot = TrainingSnapshot.from(listOf(currentWeekSession, lastWeekSession))
        val insights = analyzer.evaluate(snapshot)

        assertEquals(1, insights.size)
        assertEquals(InsightType.VOLUME_SPIKE, insights.first().type)
    }
}
