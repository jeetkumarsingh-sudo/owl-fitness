package com.example.gymdiary3.domain.analyzer

import com.example.gymdiary3.data.SessionWithSets
import com.example.gymdiary3.domain.model.ExerciseStats
import com.example.gymdiary3.domain.util.WorkoutCalculations
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

    fun get1RMHistory(exercise: String, sessions: List<SessionWithSets>): List<Pair<Long, Double>> {
        return sessions
            .sortedBy { it.session.startTime }
            .mapNotNull { sessionWithSets ->
                val setsForExercise = sessionWithSets.sets.filter { it.exercise == exercise }
                if (setsForExercise.isEmpty()) return@mapNotNull null
                
                val best1RM = setsForExercise.maxOf { 
                    WorkoutCalculations.calculate1RM(it.weight, it.reps)
                }
                Pair(sessionWithSets.session.startTime, best1RM)
            }
    }
}
