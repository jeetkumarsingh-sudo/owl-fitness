package com.example.gymdiary3.domain.repository

import com.example.gymdiary3.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ProgramRepository {
    // Program Days
    fun getAllProgramDays(): Flow<List<ProgramDay>>
    suspend fun insertProgramDay(day: ProgramDay): Long
    suspend fun getProgramDayById(id: Int): ProgramDay?
    suspend fun deleteProgramDay(day: ProgramDay)

    // Program Exercises
    fun getExercisesForDay(dayId: Int): Flow<List<ProgramExercise>>
    suspend fun insertProgramExercise(exercise: ProgramExercise): Long
    suspend fun deleteProgramExercise(exercise: ProgramExercise)

    // Session Schedule
    fun getScheduledSessions(): Flow<List<SessionSchedule>>
    fun getSessionsInDateRange(start: Long, end: Long): Flow<List<SessionSchedule>>
    suspend fun insertScheduledSession(session: SessionSchedule): Long
    suspend fun updateScheduledSession(session: SessionSchedule)

    // Session Exercise Logs
    fun getLogsForSession(sessionId: Int): Flow<List<SessionExerciseLog>>
    suspend fun insertSessionExerciseLog(log: SessionExerciseLog): Long
}
