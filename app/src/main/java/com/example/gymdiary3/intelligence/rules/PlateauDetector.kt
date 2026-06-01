package com.example.gymdiary3.intelligence.rules

import com.example.gymdiary3.intelligence.IntelligenceRule
import com.example.gymdiary3.intelligence.model.FitnessInsight
import com.example.gymdiary3.intelligence.model.InsightSeverity
import com.example.gymdiary3.intelligence.model.InsightType
import com.example.gymdiary3.intelligence.model.TrainingSnapshot

class PlateauDetector : IntelligenceRule {

    private val MIN_SESSIONS_FOR_PLATEAU = 3

    override fun evaluate(snapshot: TrainingSnapshot): List<FitnessInsight> {
        return snapshot.setsByExercise.mapNotNull { (exercise, sets) ->
            val sessionMaxWeights = sets
                .filter { it.weight > 0 }
                .groupBy { it.sessionId ?: (it.timestamp / 86_400_000) }
                .values
                .map { it.maxOf { s -> s.weight } }
                .takeLast(6)

            if (sessionMaxWeights.size < MIN_SESSIONS_FOR_PLATEAU) return@mapNotNull null

            val recentSessions = sessionMaxWeights.takeLast(MIN_SESSIONS_FOR_PLATEAU)
            val allSameWeight = recentSessions.all { it == recentSessions.first() }
            val noWeightIncrease = sessionMaxWeights.last() <= sessionMaxWeights.first()

            if (allSameWeight && noWeightIncrease) {
                val stagnantSessions = sessionMaxWeights.count { it == sessionMaxWeights.last() }
                FitnessInsight(
                    type = InsightType.PLATEAU_DETECTED,
                    message = "$exercise has stalled at ${sessionMaxWeights.last().toInt()}kg " +
                              "for $stagnantSessions sessions. " +
                              "Consider adding reps, tempo, or a technique variation.",
                    exerciseName = exercise,
                    severity = if (stagnantSessions >= 4) InsightSeverity.WARNING else InsightSeverity.INFO,
                    dataPoints = mapOf(
                        "stagnant_sessions" to stagnantSessions.toDouble(),
                        "current_max_weight" to sessionMaxWeights.last()
                    )
                )
            } else null
        }
    }
}
