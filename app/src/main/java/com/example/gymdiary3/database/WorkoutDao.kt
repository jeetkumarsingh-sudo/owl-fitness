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
import androidx.room.Transaction
import com.example.gymdiary3.data.SessionWithSets

@Dao
interface WorkoutDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutSet)

    @Insert
    suspend fun insertSession(session: WorkoutSession): Long

    @Update
    suspend fun updateSession(session: WorkoutSession)

    @Delete
    suspend fun deleteSession(session: WorkoutSession)

    @Query("DELETE FROM session WHERE id NOT IN (SELECT DISTINCT sessionId FROM WorkoutSet WHERE sessionId IS NOT NULL)")
    suspend fun deleteEmptySessions()

    @Transaction
    @Query("SELECT * FROM session ORDER BY startTime DESC")
    fun getSessionsWithSets(): Flow<List<SessionWithSets>>

    @Transaction
    @Query("SELECT * FROM session WHERE id = :sessionId")
    suspend fun getSessionWithSetsById(sessionId: Int): SessionWithSets?

    @Query("SELECT * FROM session WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Int): WorkoutSession

    @Query("DELETE FROM session WHERE id = :id")
    suspend fun deleteSessionById(id: Int)

    @Query("SELECT * FROM WorkoutSet ORDER BY date DESC")
    fun getWorkouts(): Flow<List<WorkoutSet>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercise(exercise: Exercise)

    @Query("SELECT * FROM Exercise WHERE muscle = :muscle")
    fun getExercisesByMuscle(muscle: String): Flow<List<Exercise>>

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Query("SELECT * FROM Exercise")
    suspend fun getAllExercisesList(): List<Exercise>

    @Query("SELECT * FROM WorkoutSet WHERE exercise = :exerciseName ORDER BY date DESC LIMIT 1")
    suspend fun getLastSet(exerciseName: String): WorkoutSet?

    @Query("SELECT weight FROM BodyWeight ORDER BY date DESC LIMIT 1")
    fun getLatestBodyWeightFlow(): Flow<Double?>

    @Query("SELECT * FROM WorkoutSet WHERE exercise = :exerciseName ORDER BY date DESC LIMIT 3")
    fun getLastThreeSets(exerciseName: String): Flow<List<WorkoutSet>>

    @Query("SELECT * FROM BodyWeight ORDER BY date DESC")
    suspend fun getAllBodyWeightsList(): List<com.example.gymdiary3.data.BodyWeight>

    @Query("""
        SELECT COUNT(*) FROM WorkoutSet
        WHERE exercise = :exerciseName
        AND date(date / 1000, 'unixepoch', 'localtime') = date('now', 'localtime')
    """)
    suspend fun getTodaySetCount(exerciseName: String): Int
}
