package com.example.gymdiary3.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val startTime: Long,
    val endTime: Long?
)
