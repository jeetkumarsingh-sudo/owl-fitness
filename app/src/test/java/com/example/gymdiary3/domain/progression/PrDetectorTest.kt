package com.example.gymdiary3.domain.progression

import com.example.gymdiary3.domain.model.WorkoutSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrDetectorTest {

    private val day = 24L * 60 * 60 * 1000
    private fun s(session: Int, w: Double, r: Int) = WorkoutSet(
        timestamp = session * day, muscle = "Chest", exercise = "Bench Press",
        setNumber = 1, reps = r, weight = w, isAssisted = false, sessionId = session
    )

    @Test fun `a heavier weight is a PR, one more rep is not`() {
        // 40x9 → 40x10 (rep gain only) → 45x6 (new heaviest) → 45x7 (rep gain only)
        val events = PrDetector.events(listOf(s(1, 40.0, 9), s(2, 40.0, 10), s(3, 45.0, 6), s(4, 45.0, 7)))
        assertEquals(1, events.size)
        assertEquals(45.0, events.single().weightKg, 0.0)
        assertEquals(40.0, events.single().previousBestKg, 0.0)
    }

    @Test fun `the first session is a baseline`() {
        assertTrue(PrDetector.events(listOf(s(1, 100.0, 5))).isEmpty())
    }

    @Test fun `live check compares against the heaviest so far`() {
        assertTrue(PrDetector.isPr(s(5, 47.5, 3), previousBestKg = 45.0))
        assertFalse(PrDetector.isPr(s(5, 45.0, 12), previousBestKg = 45.0))
        assertFalse(PrDetector.isPr(s(5, 50.0, 5), previousBestKg = 0.0))
    }
}
