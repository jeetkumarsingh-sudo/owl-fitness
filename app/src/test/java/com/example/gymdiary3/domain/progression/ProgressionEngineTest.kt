package com.example.gymdiary3.domain.progression

import com.example.gymdiary3.domain.model.WorkoutSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressionEngineTest {

    private val day = 24L * 60 * 60 * 1000
    private val t0 = 1_780_000_000_000L

    /** Three working sets at [weight] with [reps], in session [s] on day [s]. */
    private fun session(s: Int, weight: Double, reps: Int, sets: Int = 3) = (1..sets).map {
        WorkoutSet(
            id = s * 10 + it, timestamp = t0 + s * day + it * 60_000L, muscle = "Back",
            exercise = "Lat Pulldown", setNumber = it, reps = reps, weight = weight,
            isAssisted = false, sessionId = s
        )
    }

    private fun analyze(vararg sessions: List<WorkoutSet>, unit: String = "kg") =
        ProgressionEngine.analyze("Lat Pulldown", sessions.flatMap { it }, unit)

    @Test fun `one session is new`() {
        assertEquals(ProgressionStatus.NEW, analyze(session(1, 50.0, 8)).status)
    }

    @Test fun `weight going up is progressing`() {
        assertEquals(ProgressionStatus.PROGRESSING, analyze(session(1, 45.0, 8), session(2, 47.5, 7)).status)
    }

    @Test fun `more reps at the same weight is progressing`() {
        assertEquals(ProgressionStatus.PROGRESSING, analyze(session(1, 50.0, 7), session(2, 50.0, 8)).status)
    }

    @Test fun `same weight and reps for three sessions is stalling`() {
        val p = analyze(session(1, 50.0, 8), session(2, 50.0, 8), session(3, 50.0, 8))
        assertEquals(ProgressionStatus.STALLING, p.status)
        assertEquals(3, p.streakAtWeight)
        assertEquals("Try +1 rep before adding weight", p.recommendation?.action)
    }

    @Test fun `two sessions at the same weight is stable`() {
        assertEquals(ProgressionStatus.STABLE, analyze(session(1, 12.0, 10), session(2, 12.0, 10)).status)
    }

    @Test fun `large drop in weight and estimated 1RM is regressing`() {
        val p = analyze(session(1, 60.0, 8), session(2, 50.0, 6))
        assertEquals(ProgressionStatus.REGRESSING, p.status)
        assertEquals(60.0, p.recommendation!!.weightKg, 0.0)
    }

    @Test fun `lighter day with similar strength is stable not regressing`() {
        // 60x5 → 57.5x8: est. 1RM goes up, so not a regression.
        assertEquals(ProgressionStatus.STABLE, analyze(session(1, 60.0, 5), session(2, 57.5, 8)).status)
    }

    @Test fun `hitting the top of the rep range adds weight`() {
        val p = analyze(session(1, 45.0, 7), session(2, 45.0, 8))
        val rec = p.recommendation!!
        assertEquals(47.5, rec.weightKg, 1e-9)
        assertEquals(6, rec.repsLow)
        assertEquals("Add 2.5 kg", rec.action)
        assertNotNull(rec.reason)
    }

    @Test fun `below the top of the range adds reps at the same weight`() {
        val rec = analyze(session(1, 12.0, 9), session(2, 12.0, 10)).recommendation!!
        assertEquals(12.0, rec.weightKg, 0.0)
        assertEquals(11, rec.repsLow)
        assertEquals(12, rec.repsHigh)
        assertNull(rec.reason)
    }

    @Test fun `five stalled sessions recommends a ten percent drop`() {
        val p = analyze(*(1..5).map { session(it, 50.0, 8) }.toTypedArray())
        assertEquals(ProgressionStatus.STALLING, p.status)
        assertEquals(45.0, p.recommendation!!.weightKg, 1e-9)
    }

    @Test fun `bodyweight exercises progress reps only`() {
        val rec = analyze(session(1, 0.0, 10), session(2, 0.0, 11)).recommendation!!
        assertEquals(0.0, rec.weightKg, 0.0)
        assertEquals(12, rec.repsLow)
    }

    @Test fun `current session can be excluded from the history`() {
        val sets = session(1, 50.0, 8) + session(2, 52.5, 8)
        val p = ProgressionEngine.analyze("Lat Pulldown", sets, excludeSessionId = 2)
        assertEquals(1, p.sessions.size)
        assertEquals(50.0, p.latest!!.topWeight, 0.0)
    }

    @Test fun `increments match available plates`() {
        assertEquals(1.25, ProgressionEngine.increment(7.5, "kg"), 0.0)
        assertEquals(2.5, ProgressionEngine.increment(45.0, "kg"), 0.0)
        assertEquals(5.0, ProgressionEngine.increment(90.0, "kg"), 0.0)
        // 100 kg ≈ 220 lb → 10 lb jump
        assertEquals(10.0, ProgressionEngine.increment(100.0, "lbs") * 2.20462262185, 1e-6)
    }

    @Test fun `round to step lands on loadable weights`() {
        assertEquals(45.0, ProgressionEngine.roundToStep(45.9, "kg"), 1e-9)
        assertEquals(47.5, ProgressionEngine.roundToStep(46.4, "kg"), 1e-9)
        val lb = ProgressionEngine.roundToStep(60.0, "lbs") * 2.20462262185
        assertEquals(0.0, lb % 5.0, 1e-6)
    }

    @Test fun `rep range follows the user's own top sets`() {
        val heavy = ProgressionEngine.sessions(session(1, 100.0, 4) + session(2, 100.0, 5))
        assertEquals(3..5, ProgressionEngine.repRange(heavy))
        val light = ProgressionEngine.sessions(session(1, 10.0, 12) + session(2, 10.0, 11))
        assertEquals(8..12, ProgressionEngine.repRange(light))
    }

    @Test fun `best values come from the whole history`() {
        val p = analyze(session(1, 50.0, 10), session(2, 55.0, 4))
        assertEquals(55.0, p.bestWeightKg, 0.0)
        assertTrue(p.bestE1rmKg > 60.0) // 50 x 10 → 66.7 beats 55 x 4 → 62.3
    }
}
