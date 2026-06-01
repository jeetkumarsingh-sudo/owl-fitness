package com.example.gymdiary3.domain.model

data class WorkoutSession(
    val id: Int = 0,
    val startTime: Long,
    val endTime: Long?,
    val name: String? = null,
    val notes: String? = null
)
