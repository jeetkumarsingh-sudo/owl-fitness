package com.example.gymdiary3.viewmodel

import androidx.compose.ui.graphics.Color

data class ExerciseUiState(
    val exercise: String,
    val trend: Double,
    val trendLabel: String,
    val isPR: Boolean,
    val recommendation: String,
    val best1RM: Double
)
