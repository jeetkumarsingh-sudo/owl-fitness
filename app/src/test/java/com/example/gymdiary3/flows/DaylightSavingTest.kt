package com.example.gymdiary3.flows

import com.example.gymdiary3.presentation.common.TrainingCalendar
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/**
 * Edge case "time zones and daylight saving" (test plan X-E1): rest-day lines,
 * streaks and "Yesterday" labels count calendar days, so a 23- or 25-hour day
 * must not shift them.
 */
class DaylightSavingTest {

    private val original = TimeZone.getDefault()
    @Before fun newYork() = TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
    @After fun restore() = TimeZone.setDefault(original)

    private fun at(month: Int, day: Int, hour: Int) = Calendar.getInstance().apply {
        set(2026, month, day, hour, 0, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    @Test fun `X-E1 the clocks going back keeps Saturday to Monday at two days`() {
        // DST ends Sun 1 Nov 2026 at 02:00: that Sunday has 25 hours.
        assertEquals(2, TrainingCalendar.daysBetween(at(Calendar.OCTOBER, 31, 20), at(Calendar.NOVEMBER, 2, 7)))
        assertEquals("Yesterday", TrainingCalendar.relativeDay(at(Calendar.OCTOBER, 31, 23), at(Calendar.NOVEMBER, 1, 6)))
    }

    @Test fun `X-E2 the clocks going forward keeps a late session and an early one a day apart`() {
        // DST starts Sun 8 Mar 2026 at 02:00: that Sunday has 23 hours.
        assertEquals(1, TrainingCalendar.daysBetween(at(Calendar.MARCH, 7, 23), at(Calendar.MARCH, 8, 6)))
        assertEquals(2, TrainingCalendar.streak(listOf(at(Calendar.MARCH, 7, 23), at(Calendar.MARCH, 8, 6)), at(Calendar.MARCH, 8, 20)))
    }

    @Test fun `X-E3 the training week still starts on Monday after a DST change`() {
        val monday = TrainingCalendar.startOfWeek(at(Calendar.NOVEMBER, 4, 12))
        assertEquals(at(Calendar.NOVEMBER, 2, 0), monday)
    }
}
