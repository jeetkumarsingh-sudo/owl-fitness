package com.example.gymdiary3.data.repository

import com.example.gymdiary3.core.database.dao.ProgramDao
import com.example.gymdiary3.data.mapper.toDomain
import com.example.gymdiary3.data.mapper.toEntity
import com.example.gymdiary3.domain.model.*
import com.example.gymdiary3.domain.repository.ProgramRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProgramRepositoryImpl @Inject constructor(
    private val programDao: ProgramDao
) : ProgramRepository {
    override fun getAllProgramDays(): Flow<List<ProgramDay>> =
        programDao.getAllProgramDays().map { list -> list.map { it.toDomain() } }

    override suspend fun insertProgramDay(day: ProgramDay): Long =
        programDao.insertProgramDay(day.toEntity())

    override suspend fun getProgramDayById(id: Int): ProgramDay? =
        programDao.getProgramDayById(id)?.toDomain()

    override suspend fun deleteProgramDay(day: ProgramDay) =
        programDao.deleteProgramDay(day.toEntity())

    override fun getExercisesForDay(dayId: Int): Flow<List<ProgramExercise>> =
        programDao.getExercisesForDay(dayId).map { list -> list.map { it.toDomain() } }

    override suspend fun insertProgramExercise(exercise: ProgramExercise): Long =
        programDao.insertProgramExercise(exercise.toEntity())

    override suspend fun deleteProgramExercise(exercise: ProgramExercise) =
        programDao.deleteProgramExercise(exercise.toEntity())

    override fun getScheduledSessions(): Flow<List<SessionSchedule>> =
        programDao.getScheduledSessions().map { list -> list.map { it.toDomain() } }

    override fun getSessionsInDateRange(start: Long, end: Long): Flow<List<SessionSchedule>> =
        programDao.getSessionsInDateRange(start, end).map { list -> list.map { it.toDomain() } }

    override suspend fun insertScheduledSession(session: SessionSchedule): Long =
        programDao.insertScheduledSession(session.toEntity())

    override suspend fun updateScheduledSession(session: SessionSchedule) =
        programDao.updateScheduledSession(session.toEntity())

    override fun getLogsForSession(sessionId: Int): Flow<List<SessionExerciseLog>> =
        programDao.getLogsForSession(sessionId).map { list -> list.map { it.toDomain() } }

    override suspend fun insertSessionExerciseLog(log: SessionExerciseLog): Long =
        programDao.insertSessionExerciseLog(log.toEntity())
}
