package com.example.gymdiary3.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow
import com.example.gymdiary3.data.WorkoutSet
import com.example.gymdiary3.data.Exercise

@Dao
interface WorkoutDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutSet)

    @Query("SELECT * FROM WorkoutSet ORDER BY date DESC")
    fun getWorkouts(): Flow<List<WorkoutSet>>

    @Query("SELECT * FROM WorkoutSet WHERE exercise = :exercise ORDER BY date DESC")
    fun getWorkoutsForExercise(exercise: String): Flow<List<WorkoutSet>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercise(exercise: Exercise)

    @Query("SELECT * FROM Exercise WHERE muscle = :muscle")
    fun getExercisesByMuscle(muscle: String): Flow<List<Exercise>>

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Query("SELECT * FROM WorkoutSet")
    suspend fun getAllWorkoutsList(): List<WorkoutSet>

    @Query("SELECT * FROM Exercise")
    suspend fun getAllExercisesList(): List<Exercise>

    @Query("SELECT DISTINCT dayType FROM Exercise")
    fun getAllDayTypes(): Flow<List<String>>

    @Query("SELECT * FROM Exercise WHERE dayType = :dayType")
    fun getExercisesByDayType(dayType: String): Flow<List<Exercise>>
}
