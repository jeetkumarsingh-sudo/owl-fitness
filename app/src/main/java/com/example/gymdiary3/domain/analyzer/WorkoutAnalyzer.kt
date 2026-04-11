package com.example.gymdiary3.domain.analyzer

import com.example.gymdiary3.data.SessionWithSets
import com.example.gymdiary3.data.WorkoutSet
import com.example.gymdiary3.domain.model.ExerciseStats
import com.example.gymdiary3.domain.util.WorkoutCalculations
import java.text.SimpleDateFormat
import java.util.*

object WorkoutAnalyzer {

    fun getExerciseStats(exercise: String, allSetsForExercise: List<WorkoutSet>): ExerciseStats {
        if (allSetsForExercise.isEmpty()) {
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

        val bestWeight = allSetsForExercise.maxOf { it.weight }
        val best1RM = allSetsForExercise.maxOf {
            WorkoutCalculations.calculate1RM(it.weight, it.reps)
        }
        val totalVolume = allSetsForExercise.sumOf { WorkoutCalculations.calculateVolume(it.weight, it.reps) }

        // Group sets into sessions. Use sessionId if available, otherwise fallback to date (per day)
        val sessions = allSetsForExercise
            .groupBy { set ->
                set.sessionId ?: (set.date / (24 * 60 * 60 * 1000))
            }
            .values
            .sortedBy { it.first().date }

        val last = sessions.lastOrNull()?.maxOfOrNull { it.weight } ?: 0.0
        val previous = if (sessions.size >= 2) {
            sessions[sessions.size - 2].maxOfOrNull { it.weight } ?: 0.0
        } else 0.0

        val trend = if (previous > 0) last - previous else 0.0
        val isPR = last >= bestWeight && last > 0 && sessions.size > 1 && last > previous

        return ExerciseStats(
            exercise = exercise,
            bestWeight = bestWeight,
            best1RM = best1RM,
            totalVolume = totalVolume,
            lastSessionWeight = last,
            previousSessionWeight = previous,
            trend = trend,
            isPR = isPR
        )
    }

    fun getTrendLabel(trend: Double): String {
        return when {
            trend > 0 -> "Progressing"
            trend < 0 -> "Regression"
            else -> "Stable"
        }
    }

    fun getSuggestedWeight(lastWeight: Double): Double {
        return when {
            lastWeight < 20 -> lastWeight + 1.25
            lastWeight < 50 -> lastWeight + 2.5
            else -> lastWeight + 5
        }
    }

    fun isValidSession(session: SessionWithSets): Boolean {
        return session.totalVolume > 0
    }

    fun isValidSet(weight: Double, reps: Int): Boolean {
        return weight >= 0 && reps > 0
    }

    fun filterValidSessions(sessions: List<SessionWithSets>): List<SessionWithSets> {
        return sessions.filter { isValidSession(it) }
    }

    fun getWeeklyVolume(sessions: List<SessionWithSets>): Map<String, Double> {
        val sdf = SimpleDateFormat("yyyy-'W'ww", Locale.getDefault())
        return sessions
            .groupBy { sdf.format(Date(it.session.startTime)) }
            .mapValues { (_, sessionList) ->
                sessionList.sumOf { it.totalVolume }
            }
    }

    fun getVolumeHistory(sessions: List<SessionWithSets>): List<Pair<String, Double>> {
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        return sessions.sortedBy { it.session.startTime }
            .groupBy { dateFormat.format(Date(it.session.startTime)) }
            .map { (date, sessionList) ->
                val totalVolume = sessionList.sumOf { it.totalVolume }
                val sortTime = sessionList.first().session.startTime
                Triple(date, totalVolume, sortTime)
            }
            .sortedBy { it.third }
            .map { it.first to it.second }
    }

    fun get1RMHistory(exercise: String, allSetsForExercise: List<WorkoutSet>): List<Pair<Long, Double>> {
        return allSetsForExercise
            .groupBy { set -> set.sessionId ?: (set.date / (24 * 60 * 60 * 1000)) }
            .map { (_, sets) ->
                val best1RM = sets.maxOf { 
                    WorkoutCalculations.calculate1RM(it.weight, it.reps)
                }
                val startTime = sets.minOf { it.date }
                Pair(startTime, best1RM)
            }
            .sortedBy { it.first }
    }

    fun getExerciseVolumeHistory(exercise: String, allSetsForExercise: List<WorkoutSet>): List<Pair<String, Double>> {
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        return allSetsForExercise
            .groupBy { set -> set.sessionId ?: (set.date / (24 * 60 * 60 * 1000)) }
            .map { (_, sets) ->
                val exerciseVolume = sets.sumOf { WorkoutCalculations.calculateVolume(it.weight, it.reps) }
                val startTime = sets.minOf { it.date }
                startTime to exerciseVolume
            }
            .sortedBy { it.first }
            .map { (time, volume) -> dateFormat.format(Date(time)) to volume }
    }
}
