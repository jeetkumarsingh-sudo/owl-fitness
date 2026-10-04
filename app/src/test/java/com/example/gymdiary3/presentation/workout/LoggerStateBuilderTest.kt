package com.example.gymdiary3.presentation.workout

import com.example.gymdiary3.domain.model.WorkoutSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoggerStateBuilderTest {

    private val day = 24L * 60 * 60 * 1000
    private val now = 1_790_000_000_000L
    private var id = 1

    private fun set(session: Int, daysAgo: Int, n: Int, w: Double, r: Int) = WorkoutSet(
        id = id++, timestamp = now - daysAgo * day + n * 60_000L, muscle = "Back", exercise = "Row",
        setNumber = n, reps = r, weight = w, isAssisted = false, sessionId = session
    )

    private val history = listOf(
        set(1, 10, 1, 50.0, 8), set(1, 10, 2, 50.0, 8), set(1, 10, 3, 50.0, 7),
        set(2, 5, 1, 52.5, 8), set(2, 5, 2, 52.5, 8), set(2, 5, 3, 52.5, 7),
    )

    @Test fun `before the first set the target pre-fills the inputs`() {
        val s = LoggerStateBuilder.build("Row", "Back", history, activeId = 3, unit = "kg", now = now)
        // 52.5 x 8 tops the inferred 6–8 range → add 2.5 kg, start at the bottom of the range.
        assertEquals(55.0, s.prefillWeightKg, 0.0)
        assertEquals(6, s.prefillReps)
        assertEquals(3, s.pendingSets.size)      // last session's sets are the plan
        assertFalse(s.targetMet)
    }

    @Test fun `mid-exercise the next set continues today's weight, not the target`() {
        val today = listOf(set(3, 0, 1, 57.5, 8), set(3, 0, 2, 57.5, 8))
        val s = LoggerStateBuilder.build("Row", "Back", history + today, activeId = 3, unit = "kg", now = now)
        assertEquals(57.5, s.prefillWeightKg, 0.0)
        assertEquals(8, s.prefillReps)           // last session's set 3 was at a different weight
        assertEquals(1, s.pendingSets.size)
        assertEquals(3, s.nextSetNumber)
        assertTrue(s.targetMet)                  // 57.5 beats the 55 target
        assertTrue(s.todaySets.first().isPr)
    }
}
