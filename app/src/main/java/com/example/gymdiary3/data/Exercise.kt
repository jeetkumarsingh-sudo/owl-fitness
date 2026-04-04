package com.example.gymdiary3.data

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity
data class Exercise(
    @PrimaryKey
    val name: String,
    val muscle: String,
    val isCustom: Boolean = false
)
