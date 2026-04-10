package com.example.gymdiary3.utils

object WorkoutCalculations {

    fun calculate1RM(weight: Double, reps: Int): Double {
        if (weight <= 0.0 || reps <= 0) return 0.0
        return weight * (1 + reps / 30.0)
    }

    fun calculateVolume(weight: Double, reps: Int): Double {
        if (weight <= 0.0 || reps <= 0) return 0.0
        return weight * reps
    }
}
