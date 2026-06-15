package com.example.gymdiary3.domain.model

data class ProgramDay(
    val id: Int = 0,
    val name: String,
    val dayNumber: Int,
    val sessionType: String,
    val plannedDuration: Int? = null,
    val primaryPriority: String? = null,
    val warmupNotes: String? = null
)

data class ProgramExercise(
    val id: Int = 0,
    val programDayId: Int,
    val exerciseName: String,
    val order: Int,
    val setsPlanned: Int,
    val repsPlanned: String,
    val restSeconds: Int,
    val notes: String? = null,
    val progressionRule: String? = null,
    val category: String? = null
)

data class SessionSchedule(
    val id: Int = 0,
    val title: String,
    val date: Long,
    val programDayId: Int?,
    val status: String,
    val notes: String? = null
)

data class SessionExerciseLog(
    val id: Int = 0,
    val sessionId: Int,
    val programExerciseId: Int?,
    val exerciseName: String,
    val order: Int,
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
