package com.example.gymdiary3.presentation.progress

import com.example.gymdiary3.domain.progression.ProgressionStatus
import com.example.gymdiary3.screenshots.SampleData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressStateBuilderTest {

    private val state = ProgressStateBuilder.build(SampleData.sessions, emptyList(), "kg", SampleData.now)
    private val attention = setOf(ProgressionStatus.STALLING, ProgressionStatus.REGRESSING)

    @Test
    fun `lifts needing a change are listed first`() {
        val firstCalm = state.lifts.indexOfFirst { it.status !in attention }
        val lastAttention = state.lifts.indexOfLast { it.status in attention && it.action != null }
        assertTrue("sample has a stalling and a regressing lift", lastAttention >= 1)
        assertTrue("attention rows precede the rest", lastAttention < firstCalm)
    }

    @Test
    fun `only attention rows carry an action`() {
        // A stale stall (not trained for 3+ weeks) may have none; a progressing or stable lift never does.
        state.lifts.filter { it.status !in attention }.forEach { assertNull(it.exercise, it.action) }
        assertNotNull(state.lifts.first().action)
    }

    @Test
    fun `sample stall and regression are the lifts flagged`() {
        val flagged = state.lifts.filter { it.action != null }.associate { it.exercise to it.status }
        assertEquals(ProgressionStatus.STALLING, flagged["Lat Pulldown"])
        assertEquals(ProgressionStatus.REGRESSING, flagged["Deadlift"])
    }
}
