package com.example.gymdiary3.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "program_days")
data class ProgramDayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val dayNumber: Int,
    val sessionType: String, // Push, Pull, Legs, etc.
    val plannedDuration: Int? = null,
    val primaryPriority: String? = null,
    val warmupNotes: String? = null
)
