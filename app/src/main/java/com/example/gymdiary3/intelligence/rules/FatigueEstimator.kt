package com.example.gymdiary3.intelligence.rules

import com.example.gymdiary3.intelligence.IntelligenceRule
import com.example.gymdiary3.intelligence.model.FitnessInsight
import com.example.gymdiary3.intelligence.model.InsightSeverity
import com.example.gymdiary3.intelligence.model.InsightType
import com.example.gymdiary3.intelligence.model.TrainingSnapshot

class FatigueEstimator : IntelligenceRule {

    override fun evaluate(snapshot: TrainingSnapshot): List<FitnessInsight> {
        val insights = mutableListOf<FitnessInsight>()
        val compoundExercises = listOf("Bench Press", "Deadlift", "Squat", "Overhead Press", "Pullups")

        for (exercise in compoundExercises) {
            val sets = snapshot.setsByExercise[exercise] ?: continue
            if (sets.size < 10) continue

            val recentSessions = sets
                .filter { it.weight > 0 }
                .groupBy { it.sessionId ?: (it.timestamp / 86_400_000) }
                .values
                .sortedBy { it.first().timestamp }
                .takeLast(5)

            if (recentSessions.size < 4) continue

            val maxWeights = recentSessions.map { session -> session.maxOf { it.weight } }
            val volumePerSession = recentSessions.map { session ->
                session.sumOf { it.weight * it.reps }
            }

            val peak = maxWeights.maxOrNull() ?: continue
            val weightDecline = maxWeights.last() < peak
            val volumeIncreasing = volumePerSession.last() > volumePerSession.first()

            if (weightDecline && volumeIncreasing) {
                insights.add(
                    FitnessInsight(
                        type = InsightType.FATIGUE_ACCUMULATION,
                        message = "Performance on $exercise is declining despite consistent volume. " +
                                  "Potential accumulated fatigue. Consider a deload week.",
                        exerciseName = exercise,
                        severity = InsightSeverity.WARNING,
                        dataPoints = mapOf(
                            "last_max_weight" to maxWeights.last(),
                            "peak_max_weight" to maxWeights.max()
                        )
                    )
                )
            }
        }
        return insights
    }
}
