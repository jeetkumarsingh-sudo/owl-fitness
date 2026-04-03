package com.example.gymdiary3.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plan")
data class WorkoutPlan(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)

@Entity(tableName = "plan_exercise")
data class PlanExercise(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val planId: Int,
    val exerciseName: String
)
