package com.example.gymdiary3.domain.service

import com.example.gymdiary3.domain.model.ExerciseStats

object RecommendationEngine {

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
}
