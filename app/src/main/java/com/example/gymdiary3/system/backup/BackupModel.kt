package com.example.gymdiary3.system.backup

import kotlinx.serialization.Serializable

@Serializable
data class GymDiaryBackup(
    val version: Int = 1,
    val exportedAt: Long,
    val sessions: List<SessionBackup>,
    val bodyWeights: List<BodyWeightBackup>,
    val exercises: List<ExerciseBackup>
)

@Serializable
data class SessionBackup(
    val id: Int,
    val startTime: Long,
    val endTime: Long?,
    val name: String?,
    val notes: String?,
    val sets: List<SetBackup>
)

@Serializable
data class SetBackup(
    val setNumber: Int,
    val exercise: String,
    val muscle: String,
    val reps: Int,
    val weight: Double,
    val isAssisted: Boolean,
    val rpe: Float?,
    val notes: String?,
    val timestamp: Long
)

@Serializable
data class BodyWeightBackup(val timestamp: Long, val weight: Double)

@Serializable
data class ExerciseBackup(
    val name: String,
    val primaryMuscleGroup: String,
    val equipment: String,
    val movementPattern: String,
    val trackingType: String,
    val isCustom: Boolean
)
