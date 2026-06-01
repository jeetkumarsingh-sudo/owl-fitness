package com.example.gymdiary3.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Exercise",
    indices = [
        Index(value = ["equipment"], name = "idx_exercise_equipment"),
        Index(value = ["primaryMuscleGroup"], name = "idx_exercise_muscle")
    ]
)
data class ExerciseEntity(
    @PrimaryKey
    val name: String,
    @ColumnInfo(defaultValue = "")
    val primaryMuscleGroup: String,
    @ColumnInfo(defaultValue = "")
    val secondaryMuscleGroups: String = "",
    @ColumnInfo(defaultValue = "OTHER")
    val equipment: String = "OTHER",
    @ColumnInfo(defaultValue = "ISOLATION")
    val movementPattern: String = "ISOLATION",
    @ColumnInfo(defaultValue = "WEIGHT_REPS")
    val trackingType: String = "WEIGHT_REPS",
    @ColumnInfo(defaultValue = "0")
    val isCustom: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val isArchived: Boolean = false
)
