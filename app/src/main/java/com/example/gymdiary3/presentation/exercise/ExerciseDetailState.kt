package com.example.gymdiary3.presentation.exercise

import com.example.gymdiary3.core.util.WorkoutCalculations
import com.example.gymdiary3.domain.analytics.TimeRange
import com.example.gymdiary3.domain.model.WorkoutSet
import com.example.gymdiary3.domain.progression.ProgressionEngine
import com.example.gymdiary3.domain.progression.ProgressionStatus
import com.example.gymdiary3.domain.progression.SessionPerformance
import com.example.gymdiary3.domain.settings.WeightFormatter
import com.example.gymdiary3.presentation.common.TrainingCalendar
import com.example.gymdiary3.presentation.format.Fmt
import com.example.gymdiary3.presentation.insight.InsightPresenter
import com.example.gymdiary3.presentation.insight.Tone
import com.example.gymdiary3.ui.design.chart.ChartKind
import com.example.gymdiary3.ui.design.chart.ChartPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

enum class ExerciseTab(val label: String) { STRENGTH("Strength"), VOLUME("Volume"), REPS("Reps") }

data class ExerciseDetailUiState(
    val exercise: String,
    val status: ProgressionStatus?,
    val statusText: String?,
    val bestWeight: String,
    val bestWeightDetail: String?,
    val e1rm: String,
    val lastSession: String?,
    val lastSessionDetail: String?,
    val next: NextSessionLine?,
    val sessions: List<SessionPerformance>,
    val recent: List<RecentRow>,
    val firstDate: Long?
)

data class NextSessionLine(val headline: String, val action: String, val reason: String?)
data class RecentRow(val key: Long, val date: String, val setsLine: String)

/** Everything a chart needs to explain itself. */
data class ChartSpec(
    val points: List<ChartPoint>,
    val yAxisTitle: String,
    val minSpan: Double,
    val kind: ChartKind,
    val headline: String?,
    val headlineTone: Tone,
    val emptyText: String,
    val summary: String
)

object ExerciseDetailStateBuilder {

    fun build(exercise: String, sets: List<WorkoutSet>, unit: String, now: Long): ExerciseDetailUiState {
        val p = ProgressionEngine.analyze(exercise, sets, unit)
        val heaviest = p.sessions.maxByOrNull { it.topWeight }
        val latest = p.latest
        val dateFmt = SimpleDateFormat("MMM d", Locale.getDefault())

        val bodyweight = p.isBodyweight
        val rec = p.recommendation
        return ExerciseDetailUiState(
            exercise = exercise,
            status = p.status.takeIf { it != ProgressionStatus.NEW },
            statusText = InsightPresenter.fromProgression(p, unit)?.state,
            bestWeight = when {
                heaviest == null -> "—"
                bodyweight -> "${p.sessions.maxOf { it.bestReps }} reps"
                else -> Fmt.weightUnit(heaviest.topWeight, unit)
            },
            bestWeightDetail = heaviest?.takeIf { !bodyweight }?.let { "× ${it.topReps}" },
            e1rm = if (bodyweight || p.bestE1rmKg <= 0) "—" else Fmt.estimate(p.bestE1rmKg, unit),
            lastSession = latest?.let { Fmt.set(it.topWeight, it.topReps, unit) },
            lastSessionDetail = latest?.let {
                "${it.sets.size} ${if (it.sets.size == 1) "set" else "sets"} · ${TrainingCalendar.relativeDay(it.date, now)}"
            },
            next = rec?.let {
                NextSessionLine(
                    headline = if (it.weightKg > 0) "${Fmt.weightUnit(it.weightKg, unit)} · ${it.sets} × ${it.repsLabel}"
                    else "${it.sets} × ${it.repsLabel} reps",
                    action = it.action,
                    reason = it.reason
                )
            },
            sessions = p.sessions,
            recent = p.sessions.asReversed().take(6).map { s ->
                RecentRow(s.sessionKey, dateFmt.format(Date(s.date)), s.sets.joinToString(" · ") { Fmt.set(it.weight, it.reps, unit) })
            },
            firstDate = p.sessions.firstOrNull()?.date
        )
    }
}

object ExerciseCharts {

    fun periodWords(range: TimeRange): String = when (range) {
        TimeRange.W4 -> "4 weeks"
        TimeRange.W8 -> "8 weeks"
        TimeRange.M3 -> "3 months"
        TimeRange.M6 -> "6 months"
        TimeRange.Y1 -> "12 months"
        TimeRange.ALL -> "all time"
        TimeRange.D7 -> "7 days"
        TimeRange.D30 -> "30 days"
    }

    fun spec(
        sessions: List<SessionPerformance>,
        tab: ExerciseTab,
        range: TimeRange,
        unit: String,
        now: Long
    ): ChartSpec {
        val start = range.start(now, sessions.firstOrNull()?.date)
        val inRange = sessions.filter { it.date >= start }
        val u = Fmt.unitLabel(unit)
        val period = periodWords(range)
        fun disp(kg: Double) = WeightFormatter.fromKilograms(kg, unit)

        return when (tab) {
            ExerciseTab.STRENGTH -> {
                val points = inRange.filter { it.bestE1rm > 0 }.map { s ->
                    val best = s.sets.maxBy { WorkoutCalculations.calculate1RM(it.weight, it.reps) }
                    ChartPoint(
                        s.date, disp(s.bestE1rm),
                        listOf(Fmt.set(best.weight, best.reps, unit), "Est. 1RM ${Fmt.estimate(s.bestE1rm, unit)}")
                    )
                }
                val pct = change(points)
                ChartSpec(
                    points = points,
                    yAxisTitle = "Estimated 1RM ($u)",
                    minSpan = max(if (u == "kg") 5.0 else 10.0, (points.maxOfOrNull { it.value } ?: 0.0) * 0.1),
                    kind = ChartKind.Line,
                    headline = pct?.let { if (abs(it) < 1.0) "No change over $period" else "${Fmt.percent(it)} over $period" },
                    headlineTone = toneFor(pct),
                    emptyText = "No sessions in this range",
                    summary = summarise("Estimated 1RM", points, u)
                )
            }
            ExerciseTab.VOLUME -> {
                val points = inRange.map { s ->
                    ChartPoint(s.date, disp(s.volume), listOf("${Fmt.volume(s.volume, unit)} $u", "${s.sets.size} sets"))
                }
                val total = inRange.sumOf { it.volume }
                ChartSpec(
                    points = points,
                    yAxisTitle = "Volume per session ($u)",
                    minSpan = 1.0,
                    kind = ChartKind.Bars,
                    headline = if (points.isEmpty()) null
                    else "${Fmt.volume(total, unit)} $u over $period · ${points.size} ${if (points.size == 1) "session" else "sessions"}",
                    headlineTone = Tone.NEUTRAL,
                    emptyText = "No sessions in this range",
                    summary = summarise("Volume per session", points, u)
                )
            }
            ExerciseTab.REPS -> {
                val weight = inRange.lastOrNull()?.topWeight ?: sessions.lastOrNull()?.topWeight ?: 0.0
                val at = if (weight > 0) "at ${Fmt.weightUnit(weight, unit)}" else "(bodyweight)"
                val points = inRange.mapNotNull { s ->
                    val atW = s.sets.filter { abs(it.weight - weight) < 0.01 }
                    atW.maxOfOrNull { it.reps }?.let { r -> ChartPoint(s.date, r.toDouble(), listOf(Fmt.set(weight, r, unit))) }
                }
                val first = points.firstOrNull()?.value?.toInt()
                val last = points.lastOrNull()?.value?.toInt()
                ChartSpec(
                    points = points,
                    yAxisTitle = "Best reps $at",
                    minSpan = 4.0,
                    kind = ChartKind.Line,
                    headline = if (points.size >= 2) "$first → $last reps $at" else null,
                    headlineTone = when {
                        first == null || last == null -> Tone.NEUTRAL
                        last > first -> Tone.POSITIVE
                        last < first -> Tone.WARNING
                        else -> Tone.NEUTRAL
                    },
                    emptyText = "No sessions $at in this range",
                    summary = summarise("Best reps $at", points, "reps")
                )
            }
        }
    }

    private fun change(points: List<ChartPoint>): Double? =
        if (points.size < 2 || points.first().value <= 0) null
        else (points.last().value - points.first().value) / points.first().value * 100

    private fun toneFor(pct: Double?): Tone = when {
        pct == null -> Tone.NEUTRAL
        pct >= 1.0 -> Tone.POSITIVE
        pct <= -3.0 -> Tone.DANGER
        else -> Tone.NEUTRAL
    }

    private fun summarise(what: String, points: List<ChartPoint>, unit: String): String =
        if (points.isEmpty()) "$what: no data in this range"
        else "$what, ${points.size} sessions, from ${Fmt.trim(points.first().value)} to ${Fmt.trim(points.last().value)} $unit"
}
