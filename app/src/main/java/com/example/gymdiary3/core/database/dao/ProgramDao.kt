package com.example.gymdiary3.core.database.dao

import androidx.room.*
import com.example.gymdiary3.core.database.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {
    // Program Days
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgramDay(day: ProgramDayEntity): Long

    @Query("SELECT * FROM program_days ORDER BY dayNumber ASC")
    fun getAllProgramDays(): Flow<List<ProgramDayEntity>>

    @Query("SELECT * FROM program_days WHERE id = :id")
    suspend fun getProgramDayById(id: Int): ProgramDayEntity?

    @Delete
    suspend fun deleteProgramDay(day: ProgramDayEntity)

    // Program Exercises
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgramExercise(exercise: ProgramExerciseEntity): Long

    @Query("SELECT * FROM program_exercises WHERE programDayId = :dayId ORDER BY `order` ASC")
    fun getExercisesForDay(dayId: Int): Flow<List<ProgramExerciseEntity>>

    @Delete
    suspend fun deleteProgramExercise(exercise: ProgramExerciseEntity)

    // Session Schedule
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledSession(session: SessionScheduleEntity): Long

    @Query("SELECT * FROM session_schedule ORDER BY date ASC")
    fun getScheduledSessions(): Flow<List<SessionScheduleEntity>>

    @Query("SELECT * FROM session_schedule WHERE date >= :start AND date <= :end")
    fun getSessionsInDateRange(start: Long, end: Long): Flow<List<SessionScheduleEntity>>

    @Update
    suspend fun updateScheduledSession(session: SessionScheduleEntity)

    // Session Exercise Logs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionExerciseLog(log: SessionExerciseLogEntity): Long

    @Query("SELECT * FROM session_exercise_logs WHERE sessionId = :sessionId ORDER BY `order` ASC")
    fun getLogsForSession(sessionId: Int): Flow<List<SessionExerciseLogEntity>>
}
