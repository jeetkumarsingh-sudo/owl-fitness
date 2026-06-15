package com.example.gymdiary3.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "program_exercises",
    foreignKeys = [
        ForeignKey(
            entity = ProgramDayEntity::class,
            parentColumns = ["id"],
            childColumns = ["programDayId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("programDayId")]
)
data class ProgramExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val programDayId: Int,
    val exerciseName: String,
    val order: Int,
    val setsPlanned: Int,
    val repsPlanned: String,
    val restSeconds: Int,
    val notes: String? = null,
    val progressionRule: String? = null,
    val category: String? = null // Warm-up, Primary Lift, etc.
)
