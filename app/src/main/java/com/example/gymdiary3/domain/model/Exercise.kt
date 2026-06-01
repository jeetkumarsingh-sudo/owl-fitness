package com.example.gymdiary3.domain.model

import com.example.gymdiary3.core.database.entity.EquipmentType
import com.example.gymdiary3.core.database.entity.MovementPattern
import com.example.gymdiary3.core.database.entity.TrackingType

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
