package com.example.gymdiary3.core.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.example.gymdiary3.core.database.entity.*
import com.example.gymdiary3.core.database.relation.SessionWithSets

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM WorkoutSet ORDER BY timestamp DESC")
    fun getWorkouts(): Flow<List<WorkoutSetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutSetEntity)

    @Insert
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Delete
    suspend fun deleteSession(session: WorkoutSessionEntity)

    @Query("DELETE FROM session WHERE id NOT IN (SELECT DISTINCT sessionId FROM WorkoutSet WHERE sessionId IS NOT NULL)")
    suspend fun deleteEmptySessions()

    @Transaction
    @Query("SELECT * FROM session ORDER BY startTime DESC")
    fun getSessionsWithSets(): Flow<List<SessionWithSets>>

    @Transaction
    @Query("SELECT * FROM session WHERE id = :sessionId")
    suspend fun getSessionWithSetsById(sessionId: Int): SessionWithSets?

    @Query("SELECT * FROM session WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Int): WorkoutSessionEntity

    @Query("DELETE FROM session WHERE id = :id")
    suspend fun deleteSessionById(id: Int)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Query("SELECT * FROM Exercise WHERE muscle = :muscle")
    fun getExercisesByMuscle(muscle: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM Exercise")
    suspend fun getAllExercisesList(): List<ExerciseEntity>

    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)

    @Query("SELECT * FROM WorkoutSet WHERE exercise = :exerciseName ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastSet(exerciseName: String): WorkoutSetEntity?

    @Query("SELECT * FROM WorkoutSet WHERE exercise = :exerciseName ORDER BY timestamp DESC LIMIT 3")
    fun getLastThreeSets(exerciseName: String): Flow<List<WorkoutSetEntity>>

    @Query("SELECT COUNT(*) FROM WorkoutSet WHERE exercise = :exerciseName AND timestamp >= :dayStart AND timestamp < :dayEnd")
    suspend fun getTodaySetCount(exerciseName: String, dayStart: Long, dayEnd: Long): Int

    @Query("SELECT * FROM WorkoutSet WHERE exercise = :exerciseName AND timestamp >= :weekStart AND timestamp < :weekEnd ORDER BY timestamp ASC")
    fun getSetsForExerciseInDateRange(exerciseName: String, weekStart: Long, weekEnd: Long): Flow<List<WorkoutSetEntity>>

    @Query("SELECT MAX(weight * (1 + reps / 30.0)) FROM WorkoutSet WHERE exercise = :exerciseName AND sessionId != :excludeSessionId AND weight > 0")
    suspend fun getHistoricBest1RM(exerciseName: String, excludeSessionId: Long): Double?

    @Query("""
        SELECT * FROM WorkoutSet 
        WHERE exercise = :exerciseName 
        AND sessionId = (
            SELECT sessionId FROM WorkoutSet 
            WHERE exercise = :exerciseName 
            AND sessionId IS NOT NULL 
            AND sessionId != :currentSessionId
            ORDER BY timestamp DESC 
            LIMIT 1
        )
        ORDER BY setNumber ASC
    """)
    fun getLastSessionSetsForExercise(exerciseName: String, currentSessionId: Int): Flow<List<WorkoutSetEntity>>

    @Query("SELECT weight FROM BodyWeight ORDER BY timestamp DESC LIMIT 1")
    fun getLatestBodyWeightFlow(): Flow<Double?>

    @Query("SELECT * FROM BodyWeight ORDER BY timestamp DESC")
    suspend fun getAllBodyWeightsList(): List<BodyWeightEntity>
}
