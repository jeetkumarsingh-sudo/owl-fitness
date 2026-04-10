package com.example.gymdiary3.domain

data class ExerciseStats(
    val exercise: String,
    val bestWeight: Double,
    val best1RM: Double,
    val totalVolume: Double,
    val lastSessionWeight: Double,
    val previousSessionWeight: Double,
    val trend: Double,
    val isPR: Boolean
)
