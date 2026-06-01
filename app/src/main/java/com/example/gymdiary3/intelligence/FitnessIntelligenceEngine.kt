package com.example.gymdiary3.intelligence

import com.example.gymdiary3.domain.model.SessionWithSets
import com.example.gymdiary3.intelligence.model.FitnessInsight
import com.example.gymdiary3.intelligence.model.TrainingSnapshot
import com.example.gymdiary3.intelligence.rules.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FitnessIntelligenceEngine @Inject constructor() {

    private val rules: List<IntelligenceRule> = listOf(
        PlateauDetector(),
        ProgressionAnalyzer(),
        VolumeAnalyzer(),
        FatigueEstimator(),
        RecoveryAnalyzer(),
        TrainingFrequencyAnalyzer()
    )

    fun analyze(sessions: List<SessionWithSets>): List<FitnessInsight> {
        if (sessions.isEmpty()) return emptyList()
        val snapshot = TrainingSnapshot.from(sessions)
        return rules.flatMap { rule -> rule.evaluate(snapshot) }
    }
}
