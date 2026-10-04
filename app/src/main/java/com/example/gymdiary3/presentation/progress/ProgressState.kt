package com.example.gymdiary3.presentation.progress

import com.example.gymdiary3.domain.model.SessionWithSets
import com.example.gymdiary3.domain.progression.ExerciseProgression
import com.example.gymdiary3.domain.progression.PrDetector
import com.example.gymdiary3.domain.progression.ProgressionEngine
import com.example.gymdiary3.domain.progression.ProgressionStatus
import com.example.gymdiary3.domain.recovery.RecoveryEngine
import com.example.gymdiary3.intelligence.model.FitnessInsight
import com.example.gymdiary3.presentation.common.TrainingCalendar
import com.example.gymdiary3.presentation.format.Fmt
import com.example.gymdiary3.presentation.insight.InsightPresenter
import com.example.gymdiary3.presentation.insight.InsightRow
import com.example.gymdiary3.presentation.insight.Tone
import com.example.gymdiary3.ui.design.chart.ChartPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ProgressUiState(
    val weekVolume: String,
    val weekVolumeDelta: String?,
    val weekVolumeTone: Tone,
    val perWeek: String,
    val prCount30: Int,
    /** One bar per training week, value in the user's unit; the screen filters by range. */
    val weeklyVolume: List<ChartPoint>,
    val firstSessionAt: Long?,
    val lifts: List<LiftRow>,
    val records: List<RecordRow>,
    val balance: List<BalanceRow>,
    val insights: List<InsightRow>
) {
    val isEmpty: Boolean get() = firstSessionAt == null
}

data class LiftRow(
    val exercise: String,
    val status: ProgressionStatus,
    val state: String,
    val change: String?,
    val changeTone: Tone,
    /** What to do next session; set only when the lift needs a change (stalling or regressing). */
    val action: String? = null
)

data class RecordRow(val exercise: String, val set: String, val whenLabel: String)
data class BalanceRow(val muscle: String, val sets: Int, val fraction: Float)

object ProgressStateBuilder {

    private const val TREND_DAYS = 56
    private const val BALANCE_DAYS = 28
    /** A stalling or regressing lift trained within this window is listed first, with its next action. */
    private const val ATTENTION_DAYS = 21

    fun build(
        sessions: List<SessionWithSets>,
        engineInsights: List<FitnessInsight>,
        unit: String,
        now: Long
    ): ProgressUiState {
        val day = TrainingCalendar.DAY_MS
        val allSets = sessions.flatMap { it.sets }
        val weekFmt = SimpleDateFormat("MMM d", Locale.getDefault())

        // Weekly volume, Monday-start weeks.
        val byWeek = sessions.groupBy { TrainingCalendar.startOfWeek(it.session.startTime) }.toSortedMap()
        val weekly = byWeek.map { (start, list) ->
            val vol = list.sumOf { it.totalVolume }
            ChartPoint(
                time = start + 3 * day + day / 2, // centre of the week
                value = com.example.gymdiary3.domain.settings.WeightFormatter.fromKilograms(vol, unit),
                lines = listOf(
                    "${Fmt.volume(vol, unit)} ${Fmt.unitLabel(unit)}",
                    "Week of ${weekFmt.format(Date(start))} · ${list.size} ${if (list.size == 1) "workout" else "workouts"}"
                )
            )
        }
        val thisWeekStart = TrainingCalendar.startOfWeek(now)
        val lastWeekStart = TrainingCalendar.startOfWeek(thisWeekStart - day / 2)
        val thisVol = byWeek[thisWeekStart]?.sumOf { it.totalVolume } ?: 0.0
        val lastVol = byWeek[lastWeekStart]?.sumOf { it.totalVolume } ?: 0.0
        val delta = if (lastVol > 0) (thisVol - lastVol) / lastVol * 100 else null

        // Consistency: average sessions per week over the last 4 full weeks.
        val fourWeeksAgo = TrainingCalendar.startOfWeek(thisWeekStart - 28 * day)
        val recentCount = sessions.count { it.session.startTime in fourWeeksAgo until thisWeekStart }
        val perWeek = recentCount / 4.0

        val prs = PrDetector.events(allSets)

        val progressions = allSets.groupBy { it.exercise }.map { (name, sets) -> ProgressionEngine.analyze(name, sets, unit) }
        fun needsAttention(p: ExerciseProgression) =
            (p.status == ProgressionStatus.STALLING || p.status == ProgressionStatus.REGRESSING) &&
                p.latest!!.date >= now - ATTENTION_DAYS * day
        val lifts = progressions
            .filter { it.latest != null }
            .sortedWith(compareByDescending<ExerciseProgression> { needsAttention(it) }
                .thenByDescending { it.latest!!.date })
            .map { p ->
                val window = p.sessions.filter { it.date >= now - TREND_DAYS * day }
                val pct = if (window.size >= 2 && window.first().bestE1rm > 0) {
                    (window.last().bestE1rm - window.first().bestE1rm) / window.first().bestE1rm * 100
                } else null
                val row = InsightPresenter.fromProgression(p, unit)
                LiftRow(
                    exercise = p.exercise,
                    status = p.status,
                    state = row?.state ?: Fmt.set(p.latest!!.topWeight, p.latest!!.topReps, unit),
                    change = pct?.let { "${Fmt.percent(it)} · 8 wk" },
                    changeTone = when {
                        pct == null -> Tone.NEUTRAL
                        pct >= 1.0 -> Tone.POSITIVE
                        pct <= -3.0 -> Tone.DANGER
                        else -> Tone.NEUTRAL
                    },
                    action = row?.action?.takeIf { needsAttention(p) }
                )
            }

        val balanceSets = allSets.filter { it.timestamp >= now - BALANCE_DAYS * day }
        val counts = RecoveryEngine.MUSCLES.map { m -> m to balanceSets.count { it.muscle == m } }
            .filter { it.second > 0 }.sortedByDescending { it.second }
        val maxSets = counts.maxOfOrNull { it.second } ?: 1

        return ProgressUiState(
            weekVolume = "${Fmt.volume(thisVol, unit)} ${Fmt.unitLabel(unit)}",
            weekVolumeDelta = delta?.let { "${Fmt.percent(it)} vs last week" },
            weekVolumeTone = when {
                delta == null -> Tone.NEUTRAL
                delta >= 5 -> Tone.POSITIVE
                delta <= -20 -> Tone.WARNING
                else -> Tone.NEUTRAL
            },
            perWeek = if (perWeek % 1.0 == 0.0) perWeek.toInt().toString() else "%.1f".format(perWeek),
            prCount30 = prs.count { it.date >= now - 30 * day },
            weeklyVolume = weekly,
            firstSessionAt = sessions.minOfOrNull { it.session.startTime },
            lifts = lifts,
            records = prs.asReversed().take(5).map {
                RecordRow(it.exercise, Fmt.weightUnit(it.weightKg, unit) + " × ${it.reps}", TrainingCalendar.relativeDay(it.date, now))
            },
            balance = counts.map { (m, n) -> BalanceRow(m, n, n.toFloat() / maxSets) },
            insights = engineInsights.mapNotNull { InsightPresenter.fromEngine(it, unit) }
                .sortedByDescending { it.priority }.take(3)
        )
    }
}
