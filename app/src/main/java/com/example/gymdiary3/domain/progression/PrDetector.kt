package com.example.gymdiary3.domain.progression

import com.example.gymdiary3.core.util.WorkoutCalculations
import com.example.gymdiary3.domain.model.WorkoutSet

/** A session whose best estimated 1RM beat every earlier session of that exercise. */
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
 * Personal records by estimated 1RM (Epley), the same measure the rest of the
 * app uses. The first session of an exercise is a baseline, not a PR, and
 * bodyweight-only work is not counted.
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
        var best = sessions.first().bestE1rm
        val out = mutableListOf<PrEvent>()
        for (s in sessions.drop(1)) {
            if (s.bestE1rm > best + EPSILON) {
                val top = s.sets.maxBy { WorkoutCalculations.calculate1RM(it.weight, it.reps) }
                out += PrEvent(exercise, s.date, s.sessionKey, top.weight, top.reps, s.bestE1rm, best)
                best = s.bestE1rm
            }
        }
        return out
    }

    /**
     * True when [set] beats [previousBestE1rm] — the best from earlier sessions
     * and earlier sets today. Used to flag a PR the moment it is logged.
     */
    fun isPr(set: WorkoutSet, previousBestE1rm: Double): Boolean =
        set.weight > 0 && previousBestE1rm > 0 &&
            WorkoutCalculations.calculate1RM(set.weight, set.reps) > previousBestE1rm + EPSILON
}
