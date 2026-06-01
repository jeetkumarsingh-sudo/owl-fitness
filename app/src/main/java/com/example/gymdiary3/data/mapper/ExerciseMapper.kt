package com.example.gymdiary3.data.mapper

import com.example.gymdiary3.core.database.entity.EquipmentType
import com.example.gymdiary3.core.database.entity.ExerciseEntity
import com.example.gymdiary3.core.database.entity.MovementPattern
import com.example.gymdiary3.core.database.entity.TrackingType
import com.example.gymdiary3.domain.model.Exercise

fun ExerciseEntity.toDomain(): Exercise = Exercise(
    name = name,
    primaryMuscleGroup = primaryMuscleGroup,
    secondaryMuscleGroups = if (secondaryMuscleGroups.isBlank()) emptyList()
                            else secondaryMuscleGroups.split(",").map { it.trim() },
    equipment = EquipmentType.entries.firstOrNull { it.name == equipment } ?: EquipmentType.OTHER,
    movementPattern = MovementPattern.entries.firstOrNull { it.name == movementPattern } ?: MovementPattern.ISOLATION,
    trackingType = TrackingType.entries.firstOrNull { it.name == trackingType } ?: TrackingType.WEIGHT_REPS,
    isCustom = isCustom,
    isArchived = isArchived
)

fun Exercise.toEntity(): ExerciseEntity = ExerciseEntity(
    name = name,
    primaryMuscleGroup = primaryMuscleGroup,
    secondaryMuscleGroups = secondaryMuscleGroups.joinToString(","),
    equipment = equipment.name,
    movementPattern = movementPattern.name,
    trackingType = trackingType.name,
    isCustom = isCustom,
    isArchived = isArchived
)
