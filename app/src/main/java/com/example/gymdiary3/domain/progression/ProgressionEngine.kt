package com.example.gymdiary3.domain.progression

import com.example.gymdiary3.core.util.WorkoutCalculations
import com.example.gymdiary3.domain.model.WorkoutSet
import com.example.gymdiary3.domain.settings.WeightUnit
import kotlin.math.max
import kotlin.math.roundToInt

enum class ProgressionStatus { PROGRESSING, STABLE, STALLING, REGRESSING, NEW }

/** One session's performance on one exercise. Weights in kg. */
data class SessionPerformance(
    val sessionKey: Long,
    val date: Long,
    val topWeight: Double,
    val topReps: Int,
    val bestE1rm: Double,
    val volume: Double,
    val sets: List<WorkoutSet>
) {
    val bestReps: Int get() = sets.maxOfOrNull { it.reps } ?: 0
}

/**
 * What to do next time, kept short enough to read in two seconds.
 * [reason] is null unless the action would otherwise be surprising.
 */
data class Recommendation(
    val weightKg: Double,
    val repsLow: Int,
    val repsHigh: Int,
    val sets: Int,
    val action: String,
    val reason: String? = null
) {
    val repsLabel: String get() = if (repsLow == repsHigh) "$repsLow" else "$repsLow–$repsHigh"
}

data class ExerciseProgression(
    val exercise: String,
    val status: ProgressionStatus,
    /** Chronological, oldest first. */
    val sessions: List<SessionPerformance>,
    /** Consecutive latest sessions with the same top weight. */
    val streakAtWeight: Int,
    val bestWeightKg: Double,
    val bestE1rmKg: Double,
    val recommendation: Recommendation?
) {
    val latest: SessionPerformance? get() = sessions.lastOrNull()
    val previous: SessionPerformance? get() = sessions.getOrNull(sessions.size - 2)
    val isBodyweight: Boolean get() = sessions.isNotEmpty() && sessions.all { it.topWeight <= 0.0 }
}

/**
 * Turns an exercise's real history into a status and a next action.
 *
 * Status, from the last sessions (latest = c, previous = b):
 *  - NEW          fewer than 2 sessions
 *  - PROGRESSING  top weight went up, or same weight for more reps
 *  - REGRESSING   top weight dropped and estimated 1RM fell more than 7%
 *  - STALLING     same top weight for [STALL_SESSIONS]+ sessions with no rep gain
 *  - STABLE       everything else (holding a weight, or a lighter day)
 *
 * Next action follows double progression inside a rep range inferred from
 * the user's own top-set reps: earn the top of the range, then add weight.
 */
object ProgressionEngine {

    const val STALL_SESSIONS = 3
    private const val REGRESSION_DROP = 0.93

    fun analyze(
        exercise: String,
        sets: List<WorkoutSet>,
        unit: String = "kg",
        excludeSessionId: Int? = null
    ): ExerciseProgression {
        val history = sessions(sets.filter { excludeSessionId == null || it.sessionId != excludeSessionId })
        if (history.isEmpty()) {
            return ExerciseProgression(exercise, ProgressionStatus.NEW, history, 0, 0.0, 0.0, null)
        }
        val streak = streakAtWeight(history)
        val status = classify(history, streak)
        return ExerciseProgression(
            exercise = exercise,
            status = status,
            sessions = history,
            streakAtWeight = streak,
            bestWeightKg = history.maxOf { it.topWeight },
            bestE1rmKg = history.maxOf { it.bestE1rm },
            recommendation = recommend(history, status, streak, unit)
        )
    }

    /** Groups sets into sessions (by session id, else by day), oldest first. */
    fun sessions(sets: List<WorkoutSet>): List<SessionPerformance> =
        sets.filter { it.reps > 0 }
            .groupBy { it.sessionId?.toLong() ?: (it.timestamp / DAY_MS + SESSIONLESS_OFFSET) }
            .map { (key, group) ->
                val top = group.maxWith(compareBy<WorkoutSet>({ it.weight }, { it.reps }))
                SessionPerformance(
                    sessionKey = key,
                    date = group.minOf { it.timestamp },
                    topWeight = top.weight,
                    topReps = top.reps,
                    bestE1rm = group.maxOf { WorkoutCalculations.calculate1RM(it.weight, it.reps) },
                    volume = group.sumOf { WorkoutCalculations.calculateVolume(it.weight, it.reps) },
                    sets = group.sortedBy { it.timestamp }
                )
            }
            .sortedBy { it.date }

    fun streakAtWeight(history: List<SessionPerformance>): Int {
        val last = history.lastOrNull() ?: return 0
        return history.asReversed().takeWhile { it.topWeight == last.topWeight }.count()
    }

    fun classify(history: List<SessionPerformance>, streak: Int = streakAtWeight(history)): ProgressionStatus {
        if (history.size < 2) return ProgressionStatus.NEW
        val c = history.last()
        val b = history[history.size - 2]
        return when {
            c.topWeight > b.topWeight -> ProgressionStatus.PROGRESSING
            c.topWeight == b.topWeight && c.topReps > b.topReps -> ProgressionStatus.PROGRESSING
            c.topWeight < b.topWeight && c.bestE1rm < b.bestE1rm * REGRESSION_DROP -> ProgressionStatus.REGRESSING
            streak >= STALL_SESSIONS && noRepGain(history.takeLast(STALL_SESSIONS)) -> ProgressionStatus.STALLING
            else -> ProgressionStatus.STABLE
        }
    }

    /** No improvement across the recent window. A rep gain long ago does not excuse a current plateau. */
    private fun noRepGain(window: List<SessionPerformance>): Boolean =
        window.last().topReps <= window.first().topReps

    /** The user's working rep range, inferred from recent top-set reps. */
    fun repRange(history: List<SessionPerformance>): IntRange {
        val recent = history.takeLast(4).map { it.topReps }.sorted()
        val typical = recent[recent.size / 2]
        return when {
            typical <= 5 -> 3..5
            typical <= 8 -> 6..8
            typical <= 12 -> 8..12
            else -> 12..15
        }
    }

    fun recommend(
        history: List<SessionPerformance>,
        status: ProgressionStatus,
        streak: Int,
        unit: String
    ): Recommendation? {
        val c = history.lastOrNull() ?: return null
        val range = repRange(history)
        val workingSets = max(1, c.sets.count { it.weight >= c.topWeight * 0.9 }).coerceAtMost(5)

        if (c.topWeight <= 0.0) {
            // Bodyweight: progress reps only.
            val target = c.topReps + 1
            return Recommendation(0.0, target, target + 1, workingSets, "+1 rep")
        }

        val repsUp = (c.topReps + 1).coerceAtMost(range.last)
        return when (status) {
            ProgressionStatus.NEW ->
                Recommendation(c.topWeight, repsUp, repsUp, workingSets, "+1 rep")

            ProgressionStatus.PROGRESSING, ProgressionStatus.STABLE ->
                if (c.topReps >= range.last) {
                    val next = c.topWeight + increment(c.topWeight, unit)
                    Recommendation(
                        next, range.first, range.last.coerceAtMost(range.first + 2), workingSets,
                        "Add ${formatIncrement(next - c.topWeight, unit)}",
                        reason = "Hit ${c.topReps} reps last time"
                    )
                } else {
                    val high = (c.topReps + 2).coerceAtMost(range.last).coerceAtLeast(repsUp)
                    Recommendation(c.topWeight, repsUp, high, workingSets, "+1–2 reps")
                }

            ProgressionStatus.STALLING ->
                if (streak >= STALL_SESSIONS + 2) {
                    val drop = roundToStep(c.topWeight * 0.9, unit)
                    Recommendation(
                        drop, range.last - 1, range.last, workingSets,
                        "Drop 10% and rebuild",
                        reason = "Same weight for $streak sessions"
                    )
                } else {
                    Recommendation(c.topWeight, repsUp, repsUp, workingSets, "Try +1 rep before adding weight")
                }

            ProgressionStatus.REGRESSING -> {
                val b = history[history.size - 2]
                Recommendation(b.topWeight, b.topReps, b.topReps, workingSets, "Repeat your previous best")
            }
        }
    }

    /** Next load jump, sized to the weight and to the plates a gym actually has. */
    fun increment(weightKg: Double, unit: String): Double =
        when (WeightUnit.fromPreference(unit)) {
            WeightUnit.Kilograms -> when {
                weightKg < 10.0 -> 1.25
                weightKg < 60.0 -> 2.5
                else -> 5.0
            }
            WeightUnit.Pounds -> {
                val lb = weightKg * LB_PER_KG
                val incLb = when {
                    lb < 25.0 -> 2.5
                    lb < 135.0 -> 5.0
                    else -> 10.0
                }
                incLb / LB_PER_KG
            }
        }

    /** Rounds to a loadable weight: 1.25/2.5 kg steps, or 2.5/5 lb steps. */
    fun roundToStep(weightKg: Double, unit: String): Double =
        when (WeightUnit.fromPreference(unit)) {
            WeightUnit.Kilograms -> {
                val step = if (weightKg < 10.0) 1.25 else 2.5
                (weightKg / step).roundToInt() * step
            }
            WeightUnit.Pounds -> {
                val lb = weightKg * LB_PER_KG
                val step = if (lb < 25.0) 2.5 else 5.0
                ((lb / step).roundToInt() * step) / LB_PER_KG
            }
        }

    private fun formatIncrement(deltaKg: Double, unit: String): String =
        when (WeightUnit.fromPreference(unit)) {
            WeightUnit.Kilograms -> "${trim(deltaKg)} kg"
            WeightUnit.Pounds -> "${trim(deltaKg * LB_PER_KG)} lb"
        }

    private fun trim(v: Double): String {
        val r = (v * 100).roundToInt() / 100.0
        return if (r % 1.0 == 0.0) r.toInt().toString() else r.toString()
    }

    private const val DAY_MS = 24L * 60 * 60 * 1000
    /** Keeps day-keys for session-less sets from colliding with real session ids. */
    private const val SESSIONLESS_OFFSET = 1_000_000_000L
    private const val LB_PER_KG = 2.20462262185
}
