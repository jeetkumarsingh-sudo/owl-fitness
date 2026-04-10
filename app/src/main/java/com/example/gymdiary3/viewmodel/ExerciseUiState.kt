package com.example.gymdiary3.viewmodel

import androidx.compose.ui.graphics.Color

data class ExerciseUiState(
    val exercise: String,
    val trendLabel: String,
    val trendColor: Color,
    val isPR: Boolean,
    val recommendation: String,
    val best1RM: Double
)
