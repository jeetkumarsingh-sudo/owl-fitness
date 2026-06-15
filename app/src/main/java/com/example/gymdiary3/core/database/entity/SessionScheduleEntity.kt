package com.example.gymdiary3.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "session_schedule",
    foreignKeys = [
        ForeignKey(
            entity = ProgramDayEntity::class,
            parentColumns = ["id"],
            childColumns = ["programDayId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("programDayId")]
)
data class SessionScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val date: Long,
    val programDayId: Int?,
    val status: String, // Planned, Done, Skipped
    val notes: String? = null
)
