package com.example.gymdiary3.data.mapper

import com.example.gymdiary3.core.database.entity.*
import com.example.gymdiary3.domain.model.*

fun ProgramDayEntity.toDomain() = ProgramDay(
    id = id,
    name = name,
    dayNumber = dayNumber,
    sessionType = sessionType,
    plannedDuration = plannedDuration,
    primaryPriority = primaryPriority,
    warmupNotes = warmupNotes
)

fun ProgramDay.toEntity() = ProgramDayEntity(
    id = id,
    name = name,
    dayNumber = dayNumber,
    sessionType = sessionType,
    plannedDuration = plannedDuration,
    primaryPriority = primaryPriority,
    warmupNotes = warmupNotes
)

fun ProgramExerciseEntity.toDomain() = ProgramExercise(
    id = id,
    programDayId = programDayId,
    exerciseName = exerciseName,
    order = order,
    setsPlanned = setsPlanned,
    repsPlanned = repsPlanned,
    restSeconds = restSeconds,
    notes = notes,
    progressionRule = progressionRule,
    category = category
)

fun ProgramExercise.toEntity() = ProgramExerciseEntity(
    id = id,
    programDayId = programDayId,
    exerciseName = exerciseName,
    order = order,
    setsPlanned = setsPlanned,
    repsPlanned = repsPlanned,
    restSeconds = restSeconds,
    notes = notes,
    progressionRule = progressionRule,
    category = category
)

fun SessionScheduleEntity.toDomain() = SessionSchedule(
    id = id,
    title = title,
    date = date,
    programDayId = programDayId,
    status = status,
    notes = notes
)

fun SessionSchedule.toEntity() = SessionScheduleEntity(
    id = id,
    title = title,
    date = date,
    programDayId = programDayId,
    status = status,
    notes = notes
)

fun SessionExerciseLogEntity.toDomain() = SessionExerciseLog(
    id = id,
    sessionId = sessionId,
    programExerciseId = programExerciseId,
    exerciseName = exerciseName,
    order = order,
    set1Weight = set1Weight,
    set1Reps = set1Reps,
    set2Weight = set2Weight,
    set2Reps = set2Reps,
    set3Weight = set3Weight,
    set3Reps = set3Reps,
    set4Weight = set4Weight,
    set4Reps = set4Reps,
    set5Weight = set5Weight,
    set5Reps = set5Reps,
    notes = notes
)

fun SessionExerciseLog.toEntity() = SessionExerciseLogEntity(
    id = id,
    sessionId = sessionId,
    programExerciseId = programExerciseId,
    exerciseName = exerciseName,
    order = order,
    set1Weight = set1Weight,
    set1Reps = set1Reps,
    set2Weight = set2Weight,
    set2Reps = set2Reps,
    set3Weight = set3Weight,
    set3Reps = set3Reps,
    set4Weight = set4Weight,
    set4Reps = set4Reps,
    set5Weight = set5Weight,
    set5Reps = set5Reps,
    notes = notes
)
