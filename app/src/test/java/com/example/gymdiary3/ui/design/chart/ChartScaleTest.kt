package com.example.gymdiary3.ui.design.chart

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChartScaleTest {

    @Test fun `small changes cannot fill the whole axis`() {
        // 19.5 → 20.5 kg with a 5 kg minimum span
        val s = ChartScale.valueScale(listOf(19.5, 20.0, 20.5), minSpan = 5.0, includeZero = false)
        assertTrue("span ${s.span} should be at least 5", s.span >= 5.0)
        assertTrue(s.min <= 19.5 && s.max >= 20.5)
    }

    @Test fun `bars start at zero`() {
        val s = ChartScale.valueScale(listOf(3200.0, 5600.0), minSpan = 1.0, includeZero = true)
        assertEquals(0.0, s.min, 0.0)
        assertTrue(s.max >= 5600.0)
    }

    @Test fun `non-negative data never gets a negative axis`() {
        val s = ChartScale.valueScale(listOf(1.0, 2.0), minSpan = 20.0, includeZero = false)
        assertTrue(s.min >= 0.0)
    }

    @Test fun `ticks are nice round numbers inside the range`() {
        val s = ChartScale.valueScale(listOf(17.5, 21.0), minSpan = 5.0, includeZero = false)
        s.ticks.forEach { assertTrue(it >= s.min - 1e-9 && it <= s.max + 1e-9) }
        val step = s.ticks[1] - s.ticks[0]
        assertTrue(step in listOf(1.0, 2.0, 2.5, 5.0))
    }

    @Test fun `nice number picks 1 2 2_5 5 10 multiples`() {
        assertEquals(5.0, ChartScale.niceNumber(4.2, round = true), 0.0)
        assertEquals(100.0, ChartScale.niceNumber(87.0, round = false), 0.0)
    }

    @Test fun `time ticks land on midnights in order`() {
        val day = 24L * 60 * 60 * 1000
        val start = 1_788_000_000_000L
        val ticks = ChartScale.timeTicks(start, start + 56 * day, 4)
        assertEquals(4, ticks.size)
        assertTrue(ticks.zipWithNext().all { (a, b) -> b > a })
        ticks.forEach { assertEquals(it, ChartScale.snapToMidnight(it)) }
    }

    @Test fun `time ticks never fall outside the range`() {
        val day = 24L * 60 * 60 * 1000
        val end = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.OCTOBER, 4, 18, 30, 0) // evening: nearest midnight is tomorrow
        }.timeInMillis
        val start = end - 56 * day
        val ticks = ChartScale.timeTicks(start, end, 4)
        assertTrue(ticks.all { it in start..end })
    }

    @Test fun `percent change handles a zero base`() {
        assertEquals(null, ChartScale.percentChange(0.0, 10.0))
        assertEquals(8.0, ChartScale.percentChange(50.0, 54.0)!!, 1e-9)
    }
}
