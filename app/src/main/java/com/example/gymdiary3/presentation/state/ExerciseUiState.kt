package com.example.gymdiary3.presentation.state

import androidx.compose.runtime.Stable

@Stable
data class ExerciseUiState(
    val exercise: String,
    val trend: Double,
    val trendLabel: String,
    val isPR: Boolean,
    val recommendation: String,
    val best1RM: Double
)
