package com.example.gymdiary3.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WorkoutSet(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val date: Long,   // ✅ FIXED

    val muscle: String,
    val exercise: String,
    val set: Int,
    val reps: Int,
    val weight: Double,
    val support: Boolean
)