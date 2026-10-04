package com.example.gymdiary3.domain.recovery

import com.example.gymdiary3.domain.model.WorkoutSet
import kotlin.math.max

enum class RecoveryStatus { READY, NEARLY, RECOVERING, UNTRAINED }

data class MuscleRecovery(
    val muscle: String,
    val status: RecoveryStatus,
    /** 0..1 share of the recovery window that has elapsed. 1 when untrained. */
    val recovered: Float,
    val lastTrained: Long?,
    val hoursUntilReady: Int
)

enum class Split(val label: String, val muscles: List<String>) {
    PUSH("Push", listOf("Chest", "Shoulders", "Triceps")),
    PULL("Pull", listOf("Back", "Biceps")),
    LEGS("Lower body", listOf("Legs"))
}

data class TodayRecommendation(
    /** Null means rest. */
    val split: Split?,
    val title: String,
    val detail: String
)

/**
 * A simple, explainable recovery estimate from logged sets — no fake physiology.
 *
 * Each muscle group has a recovery window sized to the muscle (legs and back
 * longest), stretched a little after a high-volume session and shortened after
 * a light one. Status is the share of that window elapsed since the muscle was
 * last trained.
 */
object RecoveryEngine {

    val MUSCLES = listOf("Chest", "Back", "Legs", "Shoulders", "Biceps", "Triceps", "Abs")

    private val baseWindowHours = mapOf(
        "Legs" to 72, "Back" to 60, "Chest" to 60,
        "Shoulders" to 48, "Biceps" to 48, "Triceps" to 48, "Abs" to 36
    )

    fun windowHours(muscle: String, setsInLastSession: Int): Int {
        val base = baseWindowHours[muscle] ?: 48
        val factor = when {
            setsInLastSession >= 8 -> 1.15
            setsInLastSession <= 3 -> 0.75
            else -> 1.0
        }
        return (base * factor).toInt()
    }

    fun analyze(sets: List<WorkoutSet>, now: Long = System.currentTimeMillis()): List<MuscleRecovery> =
        MUSCLES.map { muscle ->
            val muscleSets = sets.filter { it.muscle == muscle && it.timestamp <= now }
            val last = muscleSets.maxOfOrNull { it.timestamp }
            if (last == null) {
                MuscleRecovery(muscle, RecoveryStatus.UNTRAINED, 1f, null, 0)
            } else {
                // Sets from the same visit: within 6h of the latest one for this muscle.
                val lastVisitSets = muscleSets.count { last - it.timestamp <= 6 * HOUR_MS }
                val window = windowHours(muscle, lastVisitSets)
                val hoursSince = ((now - last) / HOUR_MS).toInt()
                val share = (hoursSince.toFloat() / window).coerceIn(0f, 1f)
                MuscleRecovery(
                    muscle = muscle,
                    status = when {
                        share >= 1f -> RecoveryStatus.READY
                        share >= 0.7f -> RecoveryStatus.NEARLY
                        else -> RecoveryStatus.RECOVERING
                    },
                    recovered = share,
                    lastTrained = last,
                    hoursUntilReady = max(0, window - hoursSince)
                )
            }
        }

    /**
     * Suggests the split whose muscles are all ready and were trained longest
     * ago. Recommends rest after 6+ sessions in 7 days or when nothing is ready.
     */
    fun today(
        recovery: List<MuscleRecovery>,
        sessionStartsLast7Days: Int,
        now: Long = System.currentTimeMillis()
    ): TodayRecommendation {
        if (sessionStartsLast7Days >= 6) {
            return TodayRecommendation(null, "Rest day", "$sessionStartsLast7Days sessions in the last 7 days")
        }
        val byMuscle = recovery.associateBy { it.muscle }
        val scored = Split.entries.map { split ->
            val states = split.muscles.mapNotNull { byMuscle[it] }
            val minRecovered = states.minOfOrNull { it.recovered } ?: 1f
            val lastTrained = states.mapNotNull { it.lastTrained }.maxOrNull()
            Triple(split, minRecovered, lastTrained)
        }
        val ready = scored.filter { it.second >= 1f }
        val pick = (ready.ifEmpty { scored })
            .sortedWith(compareByDescending<Triple<Split, Float, Long?>> { it.second }
                .thenBy { it.third ?: Long.MIN_VALUE })
            .first()

        if (pick.second < 0.6f) {
            return TodayRecommendation(null, "Rest or light day", "Most muscles are still recovering")
        }
        val (split, _, lastTrained) = pick
        val detail = if (lastTrained == null) {
            "Not trained yet"
        } else {
            val days = calendarDaysBetween(lastTrained, now)
            "Last trained " + when (days) {
                0 -> "today"
                1 -> "yesterday"
                else -> "$days days ago"
            }
        }
        return TodayRecommendation(split, split.label, detail)
    }

    /** Calendar days, so "5 days ago" matches the date the user remembers. */
    private fun calendarDaysBetween(earlier: Long, later: Long): Int {
        fun midnight(t: Long) = java.util.Calendar.getInstance().apply {
            timeInMillis = t
            set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        return Math.round((midnight(later) - midnight(earlier)).toDouble() / DAY_MS).toInt()
    }

    private const val HOUR_MS = 60L * 60 * 1000
    private const val DAY_MS = 24 * HOUR_MS
}
