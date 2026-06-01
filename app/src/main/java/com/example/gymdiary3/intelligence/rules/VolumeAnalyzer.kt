package com.example.gymdiary3.intelligence.rules

import com.example.gymdiary3.intelligence.IntelligenceRule
import com.example.gymdiary3.intelligence.model.FitnessInsight
import com.example.gymdiary3.intelligence.model.InsightSeverity
import com.example.gymdiary3.intelligence.model.InsightType
import com.example.gymdiary3.intelligence.model.TrainingSnapshot

class VolumeAnalyzer : IntelligenceRule {

    override fun evaluate(snapshot: TrainingSnapshot): List<FitnessInsight> {
        val insights = mutableListOf<FitnessInsight>()
        val weeks = snapshot.sessionsByWeek.entries
            .sortedByDescending { it.key }
            .take(8)

        if (weeks.size < 2) return emptyList()

        val currentWeekVolume = weeks.first().value.sumOf { it.totalVolume }
        val previousWeekVolume = weeks[1].value.sumOf { it.totalVolume }

        if (previousWeekVolume > 0) {
            val changePercent = ((currentWeekVolume - previousWeekVolume) / previousWeekVolume) * 100

            when {
                changePercent > 20 -> insights.add(
                    FitnessInsight(
                        type = InsightType.VOLUME_SPIKE,
                        message = "Volume increased ${changePercent.toInt()}% this week. " +
                                  "Large spikes increase injury risk. Consider sustaining rather than accelerating.",
                        severity = InsightSeverity.WARNING,
                        dataPoints = mapOf("volume_change_percent" to changePercent)
                    )
                )
                changePercent in 5.0..20.0 -> insights.add(
                    FitnessInsight(
                        type = InsightType.OPTIMAL_VOLUME,
                        message = "Volume increased ${changePercent.toInt()}% this week. " +
                                  "Progressive overload is on track.",
                        severity = InsightSeverity.POSITIVE,
                        dataPoints = mapOf("volume_change_percent" to changePercent)
                    )
                )
                changePercent < -20 -> insights.add(
                    FitnessInsight(
                        type = InsightType.VOLUME_DECLINE,
                        message = "Volume dropped ${(-changePercent).toInt()}% this week. " +
                                  "If intentional (deload), continue. If not, check consistency.",
                        severity = InsightSeverity.INFO,
                        dataPoints = mapOf("volume_change_percent" to changePercent)
                    )
                )
            }
        }
        return insights
    }
}
