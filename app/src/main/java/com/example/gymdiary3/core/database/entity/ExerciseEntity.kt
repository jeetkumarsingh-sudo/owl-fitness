package com.example.gymdiary3.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "Exercise"
)
data class ExerciseEntity(
    @PrimaryKey
    val name: String,
    val primaryMuscleGroup: String,
    val secondaryMuscleGroups: String = "",
    val equipment: String = "OTHER",
    val movementPattern: String = "ISOLATION",
    val trackingType: String = "WEIGHT_REPS",
    val isCustom: Boolean = false,
    val isArchived: Boolean = false
)
