package com.example.gymdiary3.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow
import com.example.gymdiary3.data.WorkoutSet
import com.example.gymdiary3.data.Exercise
import com.example.gymdiary3.data.WorkoutSession
import androidx.room.Update

@Dao
interface WorkoutDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutSet)

    @Insert
    suspend fun insertSession(session: WorkoutSession): Long

    @Update
    suspend fun updateSession(session: WorkoutSession)

    @Query("SELECT * FROM session WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Int): WorkoutSession

    @Query("SELECT * FROM WorkoutSet WHERE sessionId = :sessionId")
    suspend fun getWorkoutsBySession(sessionId: Int): List<WorkoutSet>

    @Query("SELECT * FROM session ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM WorkoutSet WHERE exercise = :exerciseName")
    suspend fun getAllByExercise(exerciseName: String): List<WorkoutSet>

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

    @Query("SELECT * FROM WorkoutSet WHERE exercise = :exerciseName ORDER BY date DESC LIMIT 1")
    suspend fun getLastSet(exerciseName: String): WorkoutSet?

    @Query("""
        SELECT COUNT(*) FROM WorkoutSet
        WHERE exercise = :exerciseName
        AND date(date / 1000, 'unixepoch', 'localtime') = date('now', 'localtime')
    """)
    suspend fun getTodaySetCount(exerciseName: String): Int
}
