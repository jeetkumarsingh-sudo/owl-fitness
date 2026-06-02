package com.example.gymdiary3.intelligence.rules

import com.example.gymdiary3.intelligence.IntelligenceRule
import com.example.gymdiary3.intelligence.model.FitnessInsight
import com.example.gymdiary3.intelligence.model.InsightSeverity
import com.example.gymdiary3.intelligence.model.InsightType
import com.example.gymdiary3.intelligence.model.TrainingSnapshot

class ProgressionAnalyzer : IntelligenceRule {

    override fun evaluate(snapshot: TrainingSnapshot): List<FitnessInsight> {
        val insights = mutableListOf<FitnessInsight>()

        for ((exercise, sets) in snapshot.setsByExercise) {
            if (sets.size < 6) continue

            val sessions = sets
                .filter { it.weight > 0 }
                .groupBy { it.sessionId ?: (it.timestamp / 86_400_000) }
                .values
                .sortedBy { it.first().timestamp }

            if (sessions.size < 4) continue

            val firstHalf = sessions.take(sessions.size / 2)
            val secondHalf = sessions.drop(sessions.size / 2)

            val firstHalfWeights = firstHalf.mapNotNull { it.maxOfOrNull { s -> s.weight } }
            val secondHalfWeights = secondHalf.mapNotNull { it.maxOfOrNull { s -> s.weight } }

            if (firstHalfWeights.isEmpty() || secondHalfWeights.isEmpty()) continue

            val avgFirst = firstHalfWeights.average()
            val avgSecond = secondHalfWeights.average()

            val progressPercent = if (avgFirst > 0) ((avgSecond - avgFirst) / avgFirst) * 100 else 0.0

            if (progressPercent > 10) {
                insights.add(
                    FitnessInsight(
                        type = InsightType.CONSISTENT_PROGRESS,
                        message = "$exercise: ${progressPercent.toInt()}% strength increase over recorded history. " +
                                  "Progression rate is above average.",
                        exerciseName = exercise,
                        severity = InsightSeverity.POSITIVE,
                        dataPoints = mapOf("progress_percent" to progressPercent)
                    )
                )
            }
        }
        return insights
    }
}
