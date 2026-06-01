package com.example.gymdiary3.intelligence.model

import com.example.gymdiary3.domain.model.SessionWithSets
import com.example.gymdiary3.domain.model.WorkoutSet
import java.util.concurrent.TimeUnit

data class TrainingSnapshot(
    val allSessions: List<SessionWithSets>,
    val recentSessions: List<SessionWithSets>,          // last 28 days
    val setsByExercise: Map<String, List<WorkoutSet>>,
    val sessionsByWeek: Map<String, List<SessionWithSets>>,
    val totalSetsAllTime: Int,
    val trainingDaysLast28: Int,
    val averageSessionsPerWeek: Double,
    val now: Long
) {
    companion object {
        fun from(sessions: List<SessionWithSets>): TrainingSnapshot {
            val now = System.currentTimeMillis()
            val cutoff28Days = now - TimeUnit.DAYS.toMillis(28)
            val recent = sessions.filter { it.session.startTime >= cutoff28Days }
            val allSets = sessions.flatMap { it.sets }
            val sdf = java.text.SimpleDateFormat("yyyy-'W'ww", java.util.Locale.getDefault())

            return TrainingSnapshot(
                allSessions = sessions,
                recentSessions = recent,
                setsByExercise = allSets.groupBy { it.exercise },
                sessionsByWeek = sessions.groupBy { sdf.format(java.util.Date(it.session.startTime)) },
                totalSetsAllTime = allSets.size,
                trainingDaysLast28 = recent.size,
                averageSessionsPerWeek = if (recent.isEmpty()) 0.0 else recent.size / 4.0,
                now = now
            )
        }
    }
}
