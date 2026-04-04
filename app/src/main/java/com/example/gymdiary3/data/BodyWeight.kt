package com.example.gymdiary3.data

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity
data class BodyWeight(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val date: Long,   // ✅ FIXED
    val weight: Double
)


