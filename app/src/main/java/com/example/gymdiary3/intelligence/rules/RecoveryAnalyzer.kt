package com.example.gymdiary3.intelligence.rules

import com.example.gymdiary3.intelligence.IntelligenceRule
import com.example.gymdiary3.intelligence.model.FitnessInsight
import com.example.gymdiary3.intelligence.model.InsightSeverity
import com.example.gymdiary3.intelligence.model.InsightType
import com.example.gymdiary3.intelligence.model.TrainingSnapshot

class RecoveryAnalyzer : IntelligenceRule {

    override fun evaluate(snapshot: TrainingSnapshot): List<FitnessInsight> {
        val last7Days = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        val recentSessionCount = snapshot.allSessions
            .count { it.session.startTime >= last7Days }

        return if (recentSessionCount >= 6) {
            listOf(
                FitnessInsight(
                    type = InsightType.RECOVERY_CONCERN,
                    message = "$recentSessionCount sessions in 7 days. " +
                              "High training frequency without adequate rest may impair recovery. " +
                              "Ensure 1–2 full rest days per week.",
                    severity = InsightSeverity.WARNING,
                    dataPoints = mapOf("sessions_last_7_days" to recentSessionCount.toDouble())
                )
            )
        } else emptyList()
    }
}
