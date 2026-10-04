package com.example.gymdiary3.presentation.body

import com.example.gymdiary3.domain.analytics.TimeRange
import com.example.gymdiary3.domain.model.BodyWeight
import com.example.gymdiary3.domain.model.WorkoutSet
import com.example.gymdiary3.domain.recovery.RecoveryEngine
import com.example.gymdiary3.domain.recovery.RecoveryStatus
import com.example.gymdiary3.domain.recovery.TodayRecommendation
import com.example.gymdiary3.domain.settings.WeightFormatter
import com.example.gymdiary3.presentation.common.TrainingCalendar
import com.example.gymdiary3.presentation.format.Fmt
import com.example.gymdiary3.ui.design.chart.ChartPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BodyUiState(
    val current: String?,
    val loggedToday: Boolean,
    /** Oldest first, kg. */
    val weights: List<BodyWeight>,
    val entries: List<WeightEntryRow>,
    val recovery: List<MuscleRecoveryRow>,
    val today: TodayRecommendation?,
    val hasTraining: Boolean
)

data class WeightEntryRow(val entry: BodyWeight, val date: String, val value: String, val delta: String?)

data class MuscleRecoveryRow(
    val muscle: String,
    val status: RecoveryStatus,
    val label: String,
    val fraction: Float
)

data class WeightRangeSummary(val change: String?, val period: String, val points: List<ChartPoint>)

object BodyStateBuilder {

    fun build(
        weights: List<BodyWeight>,
        sets: List<WorkoutSet>,
        sessionStarts: List<Long>,
        unit: String,
        now: Long
    ): BodyUiState {
        val sorted = weights.sortedBy { it.timestamp }
        val latest = sorted.lastOrNull()
        val fmt = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

        val entries = sorted.asReversed().mapIndexed { i, w ->
            val older = sorted.asReversed().getOrNull(i + 1)
            WeightEntryRow(
                entry = w,
                date = fmt.format(Date(w.timestamp)),
                value = Fmt.weightUnit(w.weight, unit),
                delta = older?.let { Fmt.signedWeight(w.weight - it.weight, unit) }
            )
        }

        val recovery = RecoveryEngine.analyze(sets, now)
        val last7 = sessionStarts.count { it >= now - 7 * TrainingCalendar.DAY_MS }

        return BodyUiState(
            current = latest?.let { Fmt.weightUnit(it.weight, unit) },
            loggedToday = latest != null && TrainingCalendar.startOfDay(latest.timestamp) == TrainingCalendar.startOfDay(now),
            weights = sorted,
            entries = entries,
            recovery = recovery.map { r ->
                MuscleRecoveryRow(
                    muscle = r.muscle,
                    status = r.status,
                    label = when (r.status) {
                        RecoveryStatus.READY -> "Ready"
                        RecoveryStatus.NEARLY -> "Ready in ${r.hoursUntilReady}\u00A0h"
                        RecoveryStatus.RECOVERING -> "Ready in ${r.hoursUntilReady}\u00A0h"
                        RecoveryStatus.UNTRAINED -> "Not trained yet"
                    },
                    fraction = r.recovered
                )
            },
            today = if (sets.isEmpty()) null else RecoveryEngine.today(recovery, last7, now),
            hasTraining = sets.isNotEmpty()
        )
    }
}

object BodyCharts {

    fun periodWords(range: TimeRange): String = when (range) {
        TimeRange.D7 -> "last 7 days"
        TimeRange.D30 -> "last 30 days"
        TimeRange.M3 -> "last 3 months"
        TimeRange.M6 -> "last 6 months"
        TimeRange.Y1 -> "last 12 months"
        else -> "all time"
    }

    /** Points in the user's unit and the change from the first to the last entry in range. */
    fun summary(weights: List<BodyWeight>, range: TimeRange, unit: String, now: Long): WeightRangeSummary {
        val start = range.start(now, weights.firstOrNull()?.timestamp)
        val dateFmt = SimpleDateFormat("MMM d", Locale.getDefault())
        val inRange = weights.filter { it.timestamp >= start }.sortedBy { it.timestamp }
        val points = inRange.map {
            ChartPoint(it.timestamp, WeightFormatter.fromKilograms(it.weight, unit), listOf(Fmt.weightUnit(it.weight, unit)))
        }
        val change = if (inRange.size >= 2) Fmt.signedWeight(inRange.last().weight - inRange.first().weight, unit) else null
        val period = if (inRange.size >= 2 && range == TimeRange.ALL) "since ${dateFmt.format(Date(inRange.first().timestamp))}"
        else periodWords(range)
        return WeightRangeSummary(change, period, points)
    }
}
