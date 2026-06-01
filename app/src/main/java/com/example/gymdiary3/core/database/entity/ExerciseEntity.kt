package com.example.gymdiary3.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Exercise",
    indices = [
        Index(value = ["primaryMuscleGroup"]),
        Index(value = ["equipment"]),
        Index(value = ["trackingType"])
    ]
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

enum class EquipmentType {
    BARBELL, DUMBBELL, MACHINE, CABLE, BAND, BODYWEIGHT, ROPE, OTHER
}

enum class MovementPattern {
    HORIZONTAL_PUSH, VERTICAL_PUSH, HORIZONTAL_PULL, VERTICAL_PULL, SQUAT, HIP_HINGE, ISOLATION, CORE, CARRY
}

enum class TrackingType {
    WEIGHT_REPS, BODYWEIGHT_REPS, WEIGHT_REPS_RPE, TIME, DISTANCE, DISTANCE_TIME
}
