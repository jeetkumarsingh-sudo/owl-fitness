package com.example.gymdiary3.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "session_exercise_logs",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProgramExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["programExerciseId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("sessionId"), Index("programExerciseId")]
)
data class SessionExerciseLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sessionId: Int,
    val programExerciseId: Int?,
    val exerciseName: String,
    val order: Int,
    
    // Performed sets (simplified as per requirements, supporting up to 5 sets in columns)
    val set1Weight: Double? = null,
    val set1Reps: Int? = null,
    val set2Weight: Double? = null,
    val set2Reps: Int? = null,
    val set3Weight: Double? = null,
    val set3Reps: Int? = null,
    val set4Weight: Double? = null,
    val set4Reps: Int? = null,
    val set5Weight: Double? = null,
    val set5Reps: Int? = null,
    
    val notes: String? = null
)
