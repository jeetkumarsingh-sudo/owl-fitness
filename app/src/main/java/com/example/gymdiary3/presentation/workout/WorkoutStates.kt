package com.example.gymdiary3.presentation.workout

import com.example.gymdiary3.core.util.WorkoutCalculations
import com.example.gymdiary3.domain.history.SessionSplit
import com.example.gymdiary3.domain.model.Exercise
import com.example.gymdiary3.domain.model.SessionWithSets
import com.example.gymdiary3.domain.model.WorkoutSet
import com.example.gymdiary3.domain.progression.PrDetector
import com.example.gymdiary3.domain.progression.ProgressionEngine
import com.example.gymdiary3.domain.progression.ProgressionStatus
import com.example.gymdiary3.domain.recovery.RecoveryEngine
import com.example.gymdiary3.presentation.common.TrainingCalendar
import com.example.gymdiary3.presentation.format.Fmt

/* ======================================================= active workout */

data class ActiveWorkoutUiState(
    val sessionId: Int?,
    val title: String,
    val exercises: List<SessionExerciseRow>,
    val totalSets: Int,
    val totalVolumeKg: Double,
    val upNextTitle: String?,
    val upNext: List<UpNextRow>
)

data class SessionExerciseRow(
    val exercise: String,
    val muscle: String,
    val setsLine: String,   // "50 × 9 · 50 × 8 · 50 × 8"
    val setCount: Int,
    val hasPr: Boolean
)

data class UpNextRow(val exercise: String, val muscle: String, val detail: String)

object ActiveWorkoutStateBuilder {

    private const val MAX_UP_NEXT = 5

    fun build(sessions: List<SessionWithSets>, activeId: Int?, unit: String, now: Long): ActiveWorkoutUiState {
        val current = sessions.firstOrNull { it.session.id == activeId }
        val todaySets = current?.sets.orEmpty().sortedBy { it.timestamp }
        val history = sessions.filter { it.session.id != activeId }
        val historySets = history.flatMap { it.sets }

        val rows = todaySets.groupBy { it.exercise }.map { (name, sets) ->
            val bestBefore = historySets.filter { it.exercise == name && it.weight > 0 }
                .maxOfOrNull { it.weight } ?: 0.0
            SessionExerciseRow(
                exercise = name,
                muscle = sets.first().muscle,
                setsLine = sets.joinToString(" · ") { Fmt.set(it.weight, it.reps, unit) },
                setCount = sets.size,
                hasPr = sets.any { PrDetector.isPr(it, bestBefore) }
            )
        }

        // What to suggest next: the split this session is turning into, else today's recommendation.
        val splitLabel = if (todaySets.isNotEmpty()) SessionSplit.label(todaySets) else {
            val last7 = history.count { it.session.startTime >= now - 7 * TrainingCalendar.DAY_MS }
            RecoveryEngine.today(RecoveryEngine.analyze(historySets, now), last7, now).split?.label
        }
        val done = rows.map { it.exercise }.toSet()
        val upNext = if (splitLabel == null) emptyList() else {
            history.filter { SessionSplit.label(it.sets) == splitLabel }
                .sortedByDescending { it.session.startTime }
                .flatMap { s -> s.sets.sortedBy { it.timestamp }.map { it.exercise to it.muscle } }
                .distinctBy { it.first }
                .filter { it.first !in done }
                .take(MAX_UP_NEXT)
                .map { (name, muscle) ->
                    val p = ProgressionEngine.analyze(name, historySets.filter { it.exercise == name }, unit)
                    val rec = p.recommendation
                    val detail = when {
                        rec != null && rec.weightKg > 0 -> "Next ${Fmt.weight(rec.weightKg, unit)} × ${rec.repsLabel}"
                        p.latest != null -> "Last ${Fmt.set(p.latest!!.topWeight, p.latest!!.topReps, unit)}"
                        else -> muscle
                    }
                    UpNextRow(name, muscle, detail)
                }
        }

        return ActiveWorkoutUiState(
            sessionId = activeId,
            title = if (todaySets.isEmpty()) "Workout" else SessionSplit.label(todaySets),
            exercises = rows,
            totalSets = todaySets.size,
            totalVolumeKg = todaySets.sumOf { WorkoutCalculations.calculateVolume(it.weight, it.reps) },
            upNextTitle = splitLabel?.let { if (rows.isEmpty()) "Suggested for $it" else "Up next" },
            upNext = upNext
        )
    }
}

/* =============================================================== logger */

data class LoggedSetRow(
    val id: Int,
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val rpe: Float?,
    val previous: String?,
    val isPr: Boolean
)

/** A set from last session not yet matched today — the plan for the rest of the exercise. */
data class PendingSetRow(val setNumber: Int, val previous: String)

data class LoggerUiState(
    val exercise: String,
    val muscle: String,
    val lastSessionLabel: String?,       // "Last session 5 days ago"
    val todaySets: List<LoggedSetRow>,
    val pendingSets: List<PendingSetRow>,
    val nextSetNumber: Int,
    val nextPrevious: String?,           // what set N did last time
    val targetLine: String?,             // "62.5 kg × 6–8"
    val targetReason: String?,
    val targetMet: Boolean,
    val status: ProgressionStatus?,
    val prefillWeightKg: Double,
    val prefillReps: Int,
    val bestWeightKg: Double             // heaviest so far including today, for live PR checks
)

object LoggerStateBuilder {

    fun build(
        exercise: String,
        muscle: String,
        exerciseSets: List<WorkoutSet>,
        activeId: Int?,
        unit: String,
        now: Long
    ): LoggerUiState {
        val today = exerciseSets.filter { activeId != null && it.sessionId == activeId }.sortedBy { it.timestamp }
        val history = exerciseSets.filter { activeId == null || it.sessionId != activeId }
        val progression = ProgressionEngine.analyze(exercise, history, unit)
        val prev = progression.latest
        val prevSets = prev?.sets.orEmpty().sortedBy { it.timestamp }

        var running = history.filter { it.weight > 0 }.maxOfOrNull { it.weight } ?: 0.0
        val rows = today.mapIndexed { i, s ->
            val pr = PrDetector.isPr(s, running)
            running = maxOf(running, s.weight)
            LoggedSetRow(
                id = s.id, setNumber = i + 1, weightKg = s.weight, reps = s.reps, rpe = s.rpe,
                previous = prevSets.getOrNull(i)?.let { Fmt.set(it.weight, it.reps, unit) },
                isPr = pr
            )
        }

        val nextIndex = rows.size
        val prevForNext = prevSets.getOrNull(nextIndex)
        val rec = progression.recommendation
        val lastToday = today.lastOrNull()

        // Mid-exercise, the next set continues today's work (same weight; last session's
        // reps for this set if it was done at that weight). Before the first set, use the target.
        val (weight, reps) = when {
            lastToday != null -> lastToday.weight to (
                prevForNext?.takeIf { kotlin.math.abs(it.weight - lastToday.weight) < 0.01 }?.reps ?: lastToday.reps
            )
            rec != null -> rec.weightKg to rec.repsLow
            prevForNext != null -> prevForNext.weight to prevForNext.reps
            else -> 0.0 to 0
        }

        // Only a progression target can be "met"; going heavier than a deload is not meeting it.
        val isProgressionTarget = rec != null && rec.weightKg >= (prev?.topWeight ?: 0.0) - 0.01
        val targetMet = rec != null && isProgressionTarget && today.any { s ->
            s.weight > rec.weightKg + 0.01 || (s.weight >= rec.weightKg - 0.01 && s.reps >= rec.repsLow)
        }

        return LoggerUiState(
            exercise = exercise,
            muscle = muscle,
            lastSessionLabel = prev?.let { "Last session ${TrainingCalendar.relativeDay(it.date, now).lowercase()}" },
            todaySets = rows,
            pendingSets = prevSets.drop(rows.size).mapIndexed { i, s ->
                PendingSetRow(rows.size + i + 1, Fmt.set(s.weight, s.reps, unit))
            },
            nextSetNumber = nextIndex + 1,
            nextPrevious = prevForNext?.let { Fmt.set(it.weight, it.reps, unit) },
            targetLine = rec?.let {
                if (it.weightKg > 0) "${Fmt.weightUnit(it.weightKg, unit)} × ${it.repsLabel}" else "${it.repsLabel} reps"
            },
            targetReason = rec?.reason ?: rec?.action?.takeIf { progression.status == ProgressionStatus.STALLING },
            targetMet = targetMet,
            status = progression.status.takeIf { it != ProgressionStatus.NEW },
            prefillWeightKg = weight,
            prefillReps = reps,
            bestWeightKg = running
        )
    }
}

/* =============================================================== picker */

data class PickerRow(val name: String, val muscle: String, val detail: String, val exercise: Exercise?)

data class PickerUiState(
    val recent: List<PickerRow>,
    val all: List<PickerRow>,
    val muscles: List<String>
)

object PickerStateBuilder {

    private const val RECENT = 6

    fun build(
        library: List<Exercise>,
        allSets: List<WorkoutSet>,
        query: String,
        muscle: String?,
        unit: String,
        now: Long
    ): PickerUiState {
        val lastByExercise = allSets.groupBy { it.exercise }.mapValues { (_, s) -> s.maxBy { it.timestamp } }
        val known = library.associateBy { it.name }
        // Union of the library and anything that appears in history (e.g. after an import).
        val names = (library.map { it.name to it.primaryMuscleGroup } +
            lastByExercise.values.map { it.exercise to it.muscle })
            .distinctBy { it.first.lowercase() }

        fun row(name: String, m: String): PickerRow {
            val last = lastByExercise[name]
            val detail = if (last != null) {
                "${Fmt.set(last.weight, last.reps, unit)} · ${TrainingCalendar.relativeDay(last.timestamp, now)}"
            } else m
            return PickerRow(name, m, detail, known[name])
        }

        val q = query.trim().lowercase()
        val filtered = names
            .filter { (n, m) -> (muscle == null || m == muscle) && (q.isEmpty() || n.lowercase().contains(q)) }
            .sortedBy { it.first.lowercase() }
            .map { (n, m) -> row(n, m) }

        val recent = if (q.isEmpty() && muscle == null) {
            lastByExercise.values.sortedByDescending { it.timestamp }.take(RECENT).map { row(it.exercise, it.muscle) }
        } else emptyList()

        return PickerUiState(recent, filtered, RecoveryEngine.MUSCLES)
    }
}
