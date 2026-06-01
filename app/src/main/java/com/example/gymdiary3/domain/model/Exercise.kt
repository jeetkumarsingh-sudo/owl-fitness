package com.example.gymdiary3.domain.model

data class Exercise(
    val name: String,
    val primaryMuscleGroup: String,
    val secondaryMuscleGroups: List<String> = emptyList(),
    val equipment: EquipmentType = EquipmentType.OTHER,
    val movementPattern: MovementPattern = MovementPattern.ISOLATION,
    val trackingType: TrackingType = TrackingType.WEIGHT_REPS,
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
