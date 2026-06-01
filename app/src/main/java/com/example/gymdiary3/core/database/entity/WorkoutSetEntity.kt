package com.example.gymdiary3.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "WorkoutSet",
    indices = [
        Index("exercise"),
        Index("sessionId"),
        Index("timestamp")
    ]
)
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true)
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
