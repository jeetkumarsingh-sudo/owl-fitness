package com.example.gymdiary3.domain.progression

import com.example.gymdiary3.core.util.WorkoutCalculations
import com.example.gymdiary3.domain.model.WorkoutSet

/** A session that lifted a heavier weight than any earlier session of that exercise. */
data class PrEvent(
    val exercise: String,
    val date: Long,
    val sessionKey: Long,
    val weightKg: Double,
    val reps: Int,
    val e1rmKg: Double,
    val previousBestKg: Double
)

/**
 * Personal records = a new heaviest weight for an exercise, which is what lifters
 * mean by a PR. Estimated-1RM gains (one more rep) happen almost every session
 * for a progressing lifter; flagging each as a record would make the marker
 * meaningless, so those surface as progression and "target met" instead.
 * The first session of an exercise is a baseline; bodyweight work is not counted.
 */
object PrDetector {

    private const val EPSILON = 0.01

    fun events(allSets: List<WorkoutSet>): List<PrEvent> =
        allSets.groupBy { it.exercise }
            .flatMap { (exercise, sets) -> eventsFor(exercise, sets) }
            .sortedBy { it.date }

    fun eventsFor(exercise: String, sets: List<WorkoutSet>): List<PrEvent> {
        val sessions = ProgressionEngine.sessions(sets.filter { it.weight > 0 })
        if (sessions.size < 2) return emptyList()
        var best = sessions.first().topWeight
        val out = mutableListOf<PrEvent>()
        for (s in sessions.drop(1)) {
            if (s.topWeight > best + EPSILON) {
                out += PrEvent(
                    exercise, s.date, s.sessionKey, s.topWeight, s.topReps,
                    WorkoutCalculations.calculate1RM(s.topWeight, s.topReps), best
                )
                best = s.topWeight
            }
        }
        return out
    }

    /** True when [set] is heavier than [previousBestKg] (earlier sessions and earlier sets today). */
    fun isPr(set: WorkoutSet, previousBestKg: Double): Boolean =
        set.weight > 0 && previousBestKg > 0 && set.weight > previousBestKg + EPSILON
}
