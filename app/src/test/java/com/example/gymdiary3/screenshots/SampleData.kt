package com.example.gymdiary3.screenshots

import com.example.gymdiary3.domain.model.BodyWeight
import com.example.gymdiary3.domain.model.SessionWithSets
import com.example.gymdiary3.domain.model.WorkoutSession
import com.example.gymdiary3.domain.model.WorkoutSet
import java.util.Calendar

/**
 * Synthetic training history for screenshot tests. Generic exercise names, no
 * real user data. Ten weeks of push / pull / legs with double progression,
 * one stalling lift (Lat Pulldown) and one recent regression (Deadlift).
 */
object SampleData {

    /** Sunday 4 Oct 2026, 18:30 local. Every builder takes this as "now". */
    val now: Long = Calendar.getInstance().apply {
        set(2026, Calendar.OCTOBER, 4, 18, 30, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private const val DAY = 24L * 60 * 60 * 1000
    private const val MIN = 60_000L

    private data class Ex(
        val name: String, val muscle: String, val start: Double, val step: Double,
        val everyN: Int, val baseReps: Int, val sets: Int = 3,
        val stallAfter: Int? = null, val dropLast: Boolean = false
    )

    private val push = listOf(
        Ex("Bench Press", "Chest", 40.0, 2.5, 3, 6),
        Ex("Incline DB Press", "Chest", 24.0, 2.0, 3, 8),
        Ex("Overhead Press", "Shoulders", 25.0, 2.5, 4, 6),
        Ex("Lateral Raise", "Shoulders", 6.0, 1.0, 4, 12),
        Ex("Triceps Pushdown", "Triceps", 35.0, 2.5, 3, 10),
    )
    private val pull = listOf(
        Ex("Deadlift", "Back", 80.0, 5.0, 3, 5, dropLast = true),
        Ex("Lat Pulldown", "Back", 45.0, 2.5, 2, 8, stallAfter = 4),
        Ex("Seated Row", "Back", 45.0, 2.5, 3, 8),
        Ex("Face Pull", "Back", 35.0, 2.5, 4, 12),
        Ex("Hammer Curl", "Biceps", 10.0, 2.0, 4, 9),
    )
    private val legs = listOf(
        Ex("Squat", "Legs", 50.0, 2.5, 2, 6, sets = 4),
        Ex("Leg Press", "Legs", 60.0, 5.0, 3, 10),
        Ex("Leg Curl", "Legs", 45.0, 2.5, 3, 10),
        Ex("Calf Raise", "Legs", 30.0, 5.0, 4, 12),
    )

    /** Days-ago pattern each week (now is Sunday): Mon push, Tue pull, Thu legs, Sat push. */
    private val weekPattern = listOf(6 to push, 5 to pull, 3 to legs, 1 to push)

    val sessions: List<SessionWithSets> by lazy { build(includeToday = false) }

    /** Same history plus a workout in progress today (for the active-session states). */
    val sessionsWithActive: List<SessionWithSets> by lazy { build(includeToday = true) }

    const val ACTIVE_SESSION_ID = 999

    private fun build(includeToday: Boolean): List<SessionWithSets> {
        val plan = (9 downTo 0).flatMap { week -> weekPattern.map { (d, split) -> (week * 7 + d) to split } }
            .sortedByDescending { it.first }
        val countBySplit = mutableMapOf<List<Ex>, Int>()
        var id = 1
        var setId = 1
        val out = mutableListOf<SessionWithSets>()
        val totalBySplit = plan.groupingBy { it.second }.eachCount()

        for ((daysAgo, split) in plan) {
            val idx = countBySplit.getOrDefault(split, 0)
            countBySplit[split] = idx + 1
            val start = dayAt(daysAgo, 18, 0)
            val sets = mutableListOf<WorkoutSet>()
            var t = start + 5 * MIN
            for (ex in split) {
                val (w, r) = performance(ex, idx, totalBySplit.getValue(split))
                repeat(ex.sets) { n ->
                    sets += WorkoutSet(
                        id = setId++, timestamp = t, muscle = ex.muscle, exercise = ex.name,
                        setNumber = n + 1, reps = (r - n / 2).coerceAtLeast(1), weight = w,
                        isAssisted = false, sessionId = id
                    )
                    t += 3 * MIN
                }
                t += 2 * MIN
            }
            out += SessionWithSets(WorkoutSession(id, start, t + 4 * MIN), sets)
            id++
        }

        if (includeToday) {
            val start = now - 32 * MIN
            val sets = listOf(
                WorkoutSet(setId++, start + 4 * MIN, "Back", "Lat Pulldown", 1, 9, 50.0, false, ACTIVE_SESSION_ID),
                WorkoutSet(setId++, start + 7 * MIN, "Back", "Lat Pulldown", 2, 8, 50.0, false, ACTIVE_SESSION_ID),
                WorkoutSet(setId++, start + 10 * MIN, "Back", "Lat Pulldown", 3, 8, 50.0, false, ACTIVE_SESSION_ID),
                WorkoutSet(setId++, start + 15 * MIN, "Back", "Seated Row", 1, 8, 57.5, false, ACTIVE_SESSION_ID),
                WorkoutSet(setId++, start + 19 * MIN, "Back", "Seated Row", 2, 8, 57.5, false, ACTIVE_SESSION_ID),
            )
            out += SessionWithSets(WorkoutSession(ACTIVE_SESSION_ID, start, null), sets)
        }
        return out.sortedByDescending { it.session.startTime }
    }

    private fun performance(ex: Ex, idx: Int, total: Int): Pair<Double, Int> {
        val stalled = ex.stallAfter != null && idx >= total - ex.stallAfter
        val effective = if (stalled) total - ex.stallAfter!! - 1 else idx
        var w = ex.start + ex.step * (effective / ex.everyN)
        var r = ex.baseReps + (effective % ex.everyN)
        if (stalled) r = ex.baseReps + 1
        if (ex.dropLast && idx == total - 1) { w -= ex.step * 3; r = ex.baseReps - 1 }
        return w to r
    }

    /** Body weight every 2–3 days for 120 days, drifting 54 → 55.2 kg. */
    val bodyWeights: List<BodyWeight> by lazy {
        (0..48).map { i ->
            val daysAgo = 120 - i * 5 / 2
            val trend = 54.0 + 1.2 * (i / 48.0)
            val wobble = listOf(0.2, -0.1, 0.3, -0.2, 0.0, 0.1, -0.3)[i % 7]
            BodyWeight(id = i + 1, timestamp = dayAt(daysAgo, 7, 30), weight = Math.round((trend + wobble) * 10) / 10.0)
        }
    }

    val allSets: List<WorkoutSet> get() = sessions.flatMap { it.sets }

    private fun dayAt(daysAgo: Int, hour: Int, minute: Int): Long = Calendar.getInstance().apply {
        timeInMillis = now - daysAgo * DAY
        set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
