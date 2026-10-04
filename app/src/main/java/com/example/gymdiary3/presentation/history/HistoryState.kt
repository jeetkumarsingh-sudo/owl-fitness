package com.example.gymdiary3.presentation.history

import com.example.gymdiary3.domain.history.SessionSplit
import com.example.gymdiary3.domain.model.SessionWithSets
import com.example.gymdiary3.domain.progression.PrDetector
import com.example.gymdiary3.presentation.common.TrainingCalendar
import com.example.gymdiary3.presentation.format.Fmt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HistoryUiState(
    val subtitle: String,
    val months: List<HistoryMonth>
) {
    val isEmpty: Boolean get() = months.isEmpty()
}

data class HistoryMonth(val key: String, val title: String, val summary: String, val entries: List<HistoryEntry>)

sealed interface HistoryEntry {
    val key: String
}

data class SessionEntry(
    val sessionId: Int,
    val day: String,
    val weekday: String,
    val title: String,
    val detail: String,
    val hasPr: Boolean
) : HistoryEntry {
    override val key get() = "s$sessionId"
}

data class RestEntry(val days: Int, val afterSessionId: Int) : HistoryEntry {
    override val key get() = "r$afterSessionId"
    val label get() = if (days == 1) "1 rest day" else "$days rest days"
}

/**
 * History as a timeline: newest first, grouped by month, each session named by
 * its split, and the rest days between sessions shown as one quiet line.
 */
object HistoryStateBuilder {

    fun build(sessions: List<SessionWithSets>, activeId: Int?, unit: String): HistoryUiState {
        val finished = sessions.filter { it.session.id != activeId }.sortedByDescending { it.session.startTime }
        val prSessions = PrDetector.events(sessions.flatMap { it.sets }).map { it.sessionKey }.toSet()

        val monthFmt = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val keyFmt = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val dayFmt = SimpleDateFormat("dd", Locale.getDefault())
        val weekdayFmt = SimpleDateFormat("EEE", Locale.getDefault())

        val months = finished.groupBy { keyFmt.format(Date(it.session.startTime)) }.map { (key, list) ->
            val entries = mutableListOf<HistoryEntry>()
            list.forEachIndexed { i, s ->
                entries += SessionEntry(
                    sessionId = s.session.id,
                    day = dayFmt.format(Date(s.session.startTime)),
                    weekday = weekdayFmt.format(Date(s.session.startTime)),
                    title = s.session.name?.takeIf { it.isNotBlank() } ?: SessionSplit.label(s.sets),
                    detail = listOf(
                        "${s.sets.size} sets",
                        Fmt.duration(s.duration),
                        "${Fmt.volume(s.totalVolume, unit)} ${Fmt.unitLabel(unit)}"
                    ).joinToString(" · "),
                    hasPr = s.session.id.toLong() in prSessions
                )
                // Rest between this session and the previous (older) one, within the month.
                val older = list.getOrNull(i + 1)
                if (older != null) {
                    val gap = TrainingCalendar.daysBetween(older.session.startTime, s.session.startTime) - 1
                    if (gap >= 1) entries += RestEntry(gap, s.session.id)
                }
            }
            val volume = list.sumOf { it.totalVolume }
            HistoryMonth(
                key = key,
                title = monthFmt.format(Date(list.first().session.startTime)),
                summary = "${list.size} ${if (list.size == 1) "workout" else "workouts"} · ${Fmt.volume(volume, unit)} ${Fmt.unitLabel(unit)}",
                entries = entries
            )
        }

        val n = finished.size
        return HistoryUiState(
            subtitle = if (n == 1) "1 workout" else "$n workouts",
            months = months
        )
    }
}
