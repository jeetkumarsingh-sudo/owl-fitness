package com.example.gymdiary3.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "BodyWeight")
data class BodyWeightEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long,
    val weight: Double
)
