package com.example.gymdiary3.intelligence.rules

import com.example.gymdiary3.intelligence.IntelligenceRule
import com.example.gymdiary3.intelligence.model.FitnessInsight
import com.example.gymdiary3.intelligence.model.InsightSeverity
import com.example.gymdiary3.intelligence.model.InsightType
import com.example.gymdiary3.intelligence.model.TrainingSnapshot

class TrainingFrequencyAnalyzer : IntelligenceRule {

    override fun evaluate(snapshot: TrainingSnapshot): List<FitnessInsight> {
        val insights = mutableListOf<FitnessInsight>()
        
        if (snapshot.averageSessionsPerWeek > 0 && snapshot.averageSessionsPerWeek < 2.0) {
            insights.add(
                FitnessInsight(
                    type = InsightType.TRAINING_FREQUENCY_LOW,
                    message = "Training frequency is low (avg. ${"%.1f".format(snapshot.averageSessionsPerWeek)} sessions/week). " +
                              "Consistency is key for long-term progress.",
                    severity = InsightSeverity.INFO,
                    dataPoints = mapOf("avg_sessions_per_week" to snapshot.averageSessionsPerWeek)
                )
            )
        } else if (snapshot.averageSessionsPerWeek >= 3.0 && snapshot.averageSessionsPerWeek <= 5.0) {
            insights.add(
                FitnessInsight(
                    type = InsightType.TRAINING_FREQUENCY_OPTIMAL,
                    message = "Training frequency is optimal for most lifters. Keep up the great work!",
                    severity = InsightSeverity.POSITIVE,
                    dataPoints = mapOf("avg_sessions_per_week" to snapshot.averageSessionsPerWeek)
                )
            )
        }
        
        return insights
    }
}
