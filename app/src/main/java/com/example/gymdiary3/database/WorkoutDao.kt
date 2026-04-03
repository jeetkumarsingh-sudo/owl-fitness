package com.example.gymdiary3.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.example.gymdiary3.data.WorkoutSet

@Dao
interface WorkoutDao {

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutSet)

    @Query("SELECT * FROM WorkoutSet ORDER BY date DESC")
    fun getWorkouts(): Flow<List<WorkoutSet>>

    @Query("SELECT * FROM WorkoutSet WHERE exercise = :exercise ORDER BY date DESC")
    fun getWorkoutsForExercise(exercise: String): Flow<List<WorkoutSet>>

    @Insert(onConflict = androidx.room.OnConflictStrategy.IGNORE)
    suspend fun insertExercise(exercise: com.example.gymdiary3.data.Exercise)

    @Query("SELECT * FROM Exercise WHERE muscle = :muscle")
    fun getExercisesByMuscle(muscle: String): Flow<List<com.example.gymdiary3.data.Exercise>>

    @androidx.room.Delete
    suspend fun deleteExercise(exercise: com.example.gymdiary3.data.Exercise)

    @Query("SELECT * FROM WorkoutSet")
    suspend fun getAllWorkoutsList(): List<WorkoutSet>
}