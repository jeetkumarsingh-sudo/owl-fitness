package com.example.gymdiary3.intelligence.model

data class FitnessInsight(
    val type: InsightType,
    val message: String,
    val exerciseName: String? = null,
    val severity: InsightSeverity = InsightSeverity.INFO,
    val dataPoints: Map<String, Double> = emptyMap()
)

enum class InsightType {
    PLATEAU_DETECTED,
    STRENGTH_BREAKTHROUGH,
    CONSISTENT_PROGRESS,
    REGRESSION_DETECTED,
    VOLUME_SPIKE,
    VOLUME_DECLINE,
    OPTIMAL_VOLUME,
    DELOAD_RECOMMENDED,
    TRAINING_FREQUENCY_OPTIMAL,
    TRAINING_FREQUENCY_LOW,
    MISSED_SESSION_PATTERN,
    RECOVERY_CONCERN,
    FATIGUE_ACCUMULATION,
    BODYWEIGHT_INCREASING,
    BODYWEIGHT_STABLE,
    STRENGTH_TO_WEIGHT_IMPROVING,
    GOOD_SESSION_CONSISTENCY,
    FIRST_WEEK_COMPLETE
}

enum class InsightSeverity {
    INFO,
    POSITIVE,
    WARNING,
    ACTION_REQUIRED
}
