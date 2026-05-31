package com.example.gymdiary3.data

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    tableName = "WorkoutSet",
    indices = [
        Index(value = ["exercise"]),
        Index(value = ["sessionId"]),
        Index(value = ["timestamp"])
    ]
)
data class WorkoutSet(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val timestamp: Long,

    val muscle: String,
    val exercise: String,
    val setNumber: Int,
    val reps: Int,
    val weight: Double,
    val isAssisted: Boolean,
    val sessionId: Int? = null
)