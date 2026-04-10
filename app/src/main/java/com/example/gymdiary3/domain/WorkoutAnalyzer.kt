package com.example.gymdiary3.domain

import com.example.gymdiary3.data.SessionWithSets
import com.example.gymdiary3.utils.WorkoutCalculations
import java.text.SimpleDateFormat
import java.util.*

object WorkoutAnalyzer {

    fun getExerciseStats(exercise: String, sessions: List<SessionWithSets>): ExerciseStats {
        val sets = sessions.flatMap { it.sets }
            .filter { it.exercise == exercise }

        if (sets.isEmpty()) {
            return ExerciseStats(
                exercise = exercise,
                bestWeight = 0.0,
                best1RM = 0.0,
                totalVolume = 0.0,
                lastSessionWeight = 0.0,
                previousSessionWeight = 0.0,
                trend = 0.0,
                isPR = false
            )
        }

        val bestWeight = sets.maxOfOrNull { it.weight } ?: 0.0

        val best1RM = sets.maxOfOrNull {
            WorkoutCalculations.calculate1RM(it.weight, it.reps)
        } ?: 0.0

        val totalVolume = sets.sumOf { WorkoutCalculations.calculateVolume(it.weight, it.reps) }

        val sortedSessions = sessions
            .filter { session -> session.sets.any { it.exercise == exercise } }
            .sortedBy { it.session.startTime }

        val last = sortedSessions.lastOrNull()
            ?.sets?.filter { it.exercise == exercise }
            ?.maxOfOrNull { it.weight } ?: 0.0

        val previous = if (sortedSessions.size >= 2) {
            sortedSessions[sortedSessions.size - 2]
                .sets.filter { it.exercise == exercise }
                .maxOfOrNull { it.weight } ?: 0.0
        } else 0.0

        val trend = if (previous > 0) last - previous else 0.0
        
        // Robust PR detection: A PR is when the current max weight is >= historical max weight, 
        // AND we have at least one previous session to compare against.
        val isPR = last >= bestWeight && last > 0 && sortedSessions.size > 1 && last > previous

        return ExerciseStats(
            exercise,
            bestWeight,
            best1RM,
            totalVolume,
            last,
            previous,
            trend,
            isPR
        )
    }

    fun getTrendLabel(trend: Double): String {
        return when {
            trend > 0 -> "Progressing"
            trend < 0 -> "Regression"
            else -> "Stable"
        }
    }

    fun getRecommendation(stats: ExerciseStats): String {
        return when {
            stats.isPR -> "New Personal Record! Maintain and consolidate."
            stats.trend > 0 -> "Solid progress. Attempt +2.5kg next time."
            stats.trend < -5 -> "Significant drop. Consider a deload week."
            stats.trend < 0 -> "Slight regression. Focus on form and recovery."
            stats.lastSessionWeight > 0 && stats.previousSessionWeight > 0 && stats.trend == 0.0 -> 
                "Plateau detected. Increase intensity or volume."
            stats.lastSessionWeight > 0 -> "Establish a baseline for a few sessions."
            else -> "Start logging to see recommendations."
        }
    }

    fun getWeeklyVolume(sessions: List<SessionWithSets>): Map<String, Double> {
        val sdf = SimpleDateFormat("yyyy-ww", Locale.getDefault())
        return sessions.groupBy {
            sdf.format(Date(it.session.startTime))
        }.mapValues { (_, sessionsInWeek) ->
            sessionsInWeek.flatMap { it.sets }.sumOf { 
                WorkoutCalculations.calculateVolume(it.weight, it.reps)
            }
        }
    }
}
