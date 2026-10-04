package com.example.gymdiary3.presentation.insight

import com.example.gymdiary3.domain.model.WorkoutSet
import com.example.gymdiary3.domain.progression.ProgressionEngine
import com.example.gymdiary3.intelligence.model.FitnessInsight
import com.example.gymdiary3.intelligence.model.InsightSeverity
import com.example.gymdiary3.intelligence.model.InsightType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InsightPresenterTest {

    private val day = 24L * 60 * 60 * 1000
    private val now = 1_790_000_000_000L

    private fun stalled(exercise: String, weight: Double, sessions: Int, daysAgo: Int = 1) =
        ProgressionEngine.analyze(exercise, (1..sessions).flatMap { s ->
            (1..3).map {
                WorkoutSet(
                    timestamp = now - (daysAgo + (sessions - s) * 3) * day + it * 60_000L,
                    muscle = "Back", exercise = exercise, setNumber = it, reps = 8,
                    weight = weight, isAssisted = false, sessionId = s
                )
            }
        })

    @Test fun `stalling renders as tag, exercise, state, action — no sentence`() {
        val row = InsightPresenter.fromProgression(stalled("Lat Pulldown", 50.0, 3), "kg")!!
        assertEquals("Stalling", row.tag)
        assertEquals("Lat Pulldown", row.title)
        assertEquals("50 kg · 3 sessions", row.state)
        assertEquals("Try +1 rep before adding weight", row.action)
        assertEquals(Tone.WARNING, row.tone)
    }

    @Test fun `plateau insights from the engine are not duplicated`() {
        val plateau = FitnessInsight(InsightType.PLATEAU_DETECTED, "long message", "Lat Pulldown", InsightSeverity.INFO)
        assertNull(InsightPresenter.fromEngine(plateau, "kg"))
    }

    @Test fun `volume spike reads as a percentage`() {
        val spike = FitnessInsight(
            InsightType.VOLUME_SPIKE, "x", severity = InsightSeverity.WARNING,
            dataPoints = mapOf("volume_change_percent" to 34.6)
        )
        assertEquals("+35% vs last week", InsightPresenter.fromEngine(spike, "kg")!!.state)
    }

    @Test fun `stale exercises do not produce insights and the most urgent comes first`() {
        val rows = InsightPresenter.build(
            progressions = listOf(stalled("Lat Pulldown", 50.0, 4), stalled("Old Lift", 30.0, 4, daysAgo = 60)),
            engineInsights = listOf(
                FitnessInsight(
                    InsightType.TRAINING_FREQUENCY_OPTIMAL, "x", severity = InsightSeverity.POSITIVE,
                    dataPoints = mapOf("avg_sessions_per_week" to 3.5)
                )
            ),
            unit = "kg", now = now
        )
        assertTrue(rows.none { it.title == "Old Lift" })
        assertEquals("Lat Pulldown", rows.first().title)
    }

    @Test fun `pounds users see pounds`() {
        val row = InsightPresenter.fromProgression(stalled("Lat Pulldown", 50.0, 3), "lbs")!!
        assertTrue(row.state, row.state.startsWith("110.23 lbs") || row.state.startsWith("110.2 lbs"))
    }
}
