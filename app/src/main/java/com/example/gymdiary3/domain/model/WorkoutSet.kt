package com.example.gymdiary3.domain.model

data class WorkoutSet(
    val id: Int = 0,
    val timestamp: Long,
    val muscle: String,
    val exercise: String,
    val setNumber: Int,
    val reps: Int,
    val weight: Double,
    val isAssisted: Boolean,
    val sessionId: Int? = null,
    val rpe: Float? = null,
    val notes: String? = null
)
