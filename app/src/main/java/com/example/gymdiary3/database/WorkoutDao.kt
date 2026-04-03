package com.example.gymdiary3.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.example.gymdiary3.data.WorkoutSet

@Dao
interface WorkoutDao {

    @Insert
    suspend fun insertWorkout(workout: WorkoutSet)

    @Query("SELECT * FROM WorkoutSet ORDER BY date DESC")
    fun getAllWorkouts(): Flow<List<WorkoutSet>>

    @Query("SELECT * FROM WorkoutSet")
    suspend fun getAllWorkoutsList(): List<WorkoutSet>

    @Query("SELECT * FROM WorkoutSet WHERE exercise = :exercise ORDER BY id DESC LIMIT 1")
    suspend fun getLastWorkout(exercise: String): WorkoutSet?
}