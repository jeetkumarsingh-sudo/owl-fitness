package com.example.gymdiary3.presentation.common

import com.example.gymdiary3.ui.design.WeekDay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Calendar maths shared by Home and History. Pure; [now] is injected for tests. */
object TrainingCalendar {

    const val DAY_MS = 24L * 60 * 60 * 1000

    fun startOfDay(millis: Long): Long = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    /**
     * Training weeks run Monday–Sunday regardless of locale, so a Sunday session
     * belongs to the week it finishes rather than starting an empty one.
     */
    fun startOfWeek(now: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = startOfDay(now) }
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) cal.add(Calendar.DAY_OF_MONTH, -1)
        return cal.timeInMillis
    }

    /** Whole calendar days between two instants (not elapsed hours / 24). */
    fun daysBetween(earlier: Long, later: Long): Int =
        Math.round((startOfDay(later) - startOfDay(earlier)).toDouble() / DAY_MS).toInt()

    /** Consecutive training days ending today — or yesterday, so the streak survives until a full day is missed. */
    fun streak(sessionStarts: List<Long>, now: Long): Int {
        val days = sessionStarts.map { startOfDay(it) }.toHashSet()
        var cursor = startOfDay(now)
        if (cursor !in days) cursor = startOfDay(cursor - DAY_MS / 2)
        var n = 0
        while (cursor in days) {
            n++
            cursor = startOfDay(cursor - DAY_MS / 2)
        }
        return n
    }

    fun week(sessionStarts: List<Long>, now: Long): List<WeekDay> {
        val weekStart = startOfWeek(now)
        val today = startOfDay(now)
        val trained = sessionStarts.map { startOfDay(it) }.toHashSet()
        // "EEE" then first letter: java.text has no reliable narrow day format.
        val fmt = SimpleDateFormat("EEE", Locale.getDefault())
        val cal = Calendar.getInstance().apply { timeInMillis = weekStart }
        return (0 until 7).map {
            val day = cal.timeInMillis
            cal.add(Calendar.DAY_OF_MONTH, 1)
            WeekDay(
                initial = fmt.format(Date(day)).take(1).uppercase(),
                done = day in trained,
                isToday = day == today,
                isFuture = day > today
            )
        }
    }

    /** "Today", "Yesterday", "3 days ago", or "Sep 28" after a week. */
    fun relativeDay(millis: Long, now: Long): String {
        val days = daysBetween(millis, now)
        return when {
            days <= 0 -> "Today"
            days == 1 -> "Yesterday"
            days < 7 -> "$days days ago"
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(millis))
        }
    }

    fun greeting(now: Long): String {
        val h = Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.HOUR_OF_DAY)
        return when {
            h < 12 -> "Good morning"
            h < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    fun dateLine(now: Long): String = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date(now))
}
