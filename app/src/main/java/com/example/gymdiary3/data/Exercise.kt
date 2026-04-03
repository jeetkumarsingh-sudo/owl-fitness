package com.example.gymdiary3.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Exercise(
    @PrimaryKey
    val name: String,
    val muscle: String,
    val isCustom: Boolean = false
)
