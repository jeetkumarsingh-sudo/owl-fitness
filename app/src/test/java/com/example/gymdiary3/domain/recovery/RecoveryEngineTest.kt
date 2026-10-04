package com.example.gymdiary3.domain.recovery

import com.example.gymdiary3.domain.model.WorkoutSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecoveryEngineTest {

    private val hour = 60L * 60 * 1000
    private val now = 1_790_000_000_000L

    private fun sets(muscle: String, hoursAgo: Int, count: Int = 4) = (1..count).map {
        WorkoutSet(
            timestamp = now - hoursAgo * hour - it * 60_000L, muscle = muscle, exercise = "$muscle ex",
            setNumber = it, reps = 8, weight = 40.0, isAssisted = false, sessionId = 1
        )
    }

    private fun status(all: List<MuscleRecovery>, muscle: String) = all.first { it.muscle == muscle }

    @Test fun `untrained muscle is reported as untrained and fully recovered`() {
        val r = status(RecoveryEngine.analyze(emptyList(), now), "Chest")
        assertEquals(RecoveryStatus.UNTRAINED, r.status)
        assertEquals(1f, r.recovered, 0f)
    }

    @Test fun `legs trained yesterday are still recovering`() {
        val r = status(RecoveryEngine.analyze(sets("Legs", 24), now), "Legs")
        assertEquals(RecoveryStatus.RECOVERING, r.status)
        assertEquals(48, r.hoursUntilReady)
    }

    @Test fun `biceps trained three days ago are ready`() {
        assertEquals(RecoveryStatus.READY, status(RecoveryEngine.analyze(sets("Biceps", 72), now), "Biceps").status)
    }

    @Test fun `high volume stretches the window`() {
        assertEquals(82, RecoveryEngine.windowHours("Legs", 10))
        assertEquals(54, RecoveryEngine.windowHours("Legs", 2))
    }

    @Test fun `today picks the ready split trained longest ago`() {
        val history = sets("Chest", 30) + sets("Shoulders", 30) + sets("Triceps", 30) + // push: yesterday
            sets("Back", 80) + sets("Biceps", 80) +                                    // pull: 3+ days
            sets("Legs", 120)                                                            // legs: 5 days
        val today = RecoveryEngine.today(RecoveryEngine.analyze(history, now), sessionStartsLast7Days = 3, now = now)
        assertEquals(Split.LEGS, today.split)
        assertEquals("Lower body", today.title)
    }

    @Test fun `six sessions in a week recommends rest`() {
        val today = RecoveryEngine.today(RecoveryEngine.analyze(emptyList(), now), sessionStartsLast7Days = 6, now = now)
        assertNull(today.split)
        assertEquals("Rest day", today.title)
    }

    @Test fun `everything trained this morning recommends a rest or light day`() {
        val history = RecoveryEngine.MUSCLES.flatMap { sets(it, 2) }
        val today = RecoveryEngine.today(RecoveryEngine.analyze(history, now), sessionStartsLast7Days = 1, now = now)
        assertNull(today.split)
    }
}
