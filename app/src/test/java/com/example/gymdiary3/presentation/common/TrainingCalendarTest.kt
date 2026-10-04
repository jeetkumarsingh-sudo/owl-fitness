package com.example.gymdiary3.presentation.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class TrainingCalendarTest {

    private fun at(day: Int, hour: Int) = Calendar.getInstance().apply {
        set(2026, Calendar.OCTOBER, day, hour, 0, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private val sundayEvening = at(4, 18) // Sun 4 Oct 2026

    @Test fun `training week runs monday to sunday`() {
        val week = TrainingCalendar.week(emptyList(), sundayEvening)
        assertEquals(7, week.size)
        assertEquals("M", week.first().initial)
        assertEquals("S", week.last().initial)
        assertTrue(week.last().isToday)
    }

    @Test fun `sessions earlier in the week count on sunday`() {
        val mon = Calendar.getInstance().apply { set(2026, Calendar.SEPTEMBER, 28, 18, 0, 0) }.timeInMillis
        val week = TrainingCalendar.week(listOf(mon, at(3, 18)), sundayEvening)
        assertTrue(week[0].done)   // Monday
        assertTrue(week[5].done)   // Saturday
        assertEquals(TrainingCalendar.startOfWeek(sundayEvening), TrainingCalendar.startOfDay(mon))
    }

    @Test fun `relative day uses calendar days not elapsed hours`() {
        // Tuesday 19:20 → Sunday 18:30 is 4.96 days elapsed but 5 calendar days.
        val tue = Calendar.getInstance().apply { set(2026, Calendar.SEPTEMBER, 29, 19, 20, 0) }.timeInMillis
        assertEquals("5 days ago", TrainingCalendar.relativeDay(tue, sundayEvening))
        assertEquals("Yesterday", TrainingCalendar.relativeDay(at(3, 23), at(4, 1)))
        assertEquals("Today", TrainingCalendar.relativeDay(at(4, 6), sundayEvening))
    }

    @Test fun `streak survives until a full day is missed`() {
        val starts = listOf(at(1, 18), at(2, 18), at(3, 18))
        assertEquals(3, TrainingCalendar.streak(starts, sundayEvening))      // today not trained yet
        assertEquals(0, TrainingCalendar.streak(listOf(at(1, 18)), sundayEvening))
    }
}
