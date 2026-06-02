package com.example.gymdiary3.data.repository

import com.example.gymdiary3.core.database.dao.WorkoutDao
import com.example.gymdiary3.data.mapper.toDomain
import com.example.gymdiary3.data.mapper.toEntity
import com.example.gymdiary3.domain.model.SessionWithSets
import com.example.gymdiary3.domain.model.WorkoutSession
import com.example.gymdiary3.domain.model.WorkoutSet
import com.example.gymdiary3.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WorkoutRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao
) : WorkoutRepository {
    override fun getAllSets(): Flow<List<WorkoutSet>> =
        workoutDao.getWorkouts().map { list -> list.map { it.toDomain() } }
    
    override fun getSessionsWithSets(): Flow<List<SessionWithSets>> = 
        workoutDao.getSessionsWithSets().map { list -> list.map { it.toDomain() } }
    
    override suspend fun insertSet(set: WorkoutSet) = 
        workoutDao.insertWorkout(set.toEntity())
    
    override suspend fun insertSession(session: WorkoutSession): Long = 
        workoutDao.insertSession(session.toEntity())
    
    override suspend fun updateSession(session: WorkoutSession) = 
        workoutDao.updateSession(session.toEntity())
    
    override suspend fun deleteSession(session: WorkoutSession) = 
        workoutDao.deleteSession(session.toEntity())
    
    override suspend fun deleteEmptySessions() = workoutDao.deleteEmptySessions()
    
    override suspend fun getSessionWithSetsById(sessionId: Int): SessionWithSets? = 
        workoutDao.getSessionWithSetsById(sessionId)?.toDomain()
    
    override suspend fun getSessionById(sessionId: Int): WorkoutSession? =
        workoutDao.getSessionById(sessionId)?.toDomain()
    
    override suspend fun deleteSessionById(id: Int) = workoutDao.deleteSessionById(id)
    
    override suspend fun getLastSet(exerciseName: String): WorkoutSet? = 
        workoutDao.getLastSet(exerciseName)?.toDomain()
    
    override fun getLastThreeSets(exerciseName: String): Flow<List<WorkoutSet>> = 
        workoutDao.getLastThreeSets(exerciseName).map { list -> list.map { it.toDomain() } }
    
    override suspend fun getTodaySetCount(exerciseName: String, dayStart: Long, dayEnd: Long): Int = 
        workoutDao.getTodaySetCount(exerciseName, dayStart, dayEnd)
    
    override fun getSetsForExerciseInDateRange(exerciseName: String, weekStart: Long, weekEnd: Long): Flow<List<WorkoutSet>> = 
        workoutDao.getSetsForExerciseInDateRange(exerciseName, weekStart, weekEnd).map { list -> list.map { it.toDomain() } }
    
    override fun getLastSessionSetsForExercise(exerciseName: String, currentSessionId: Int): Flow<List<WorkoutSet>> = 
        workoutDao.getLastSessionSetsForExercise(exerciseName, currentSessionId).map { list -> list.map { it.toDomain() } }
    
    override suspend fun getHistoricBest1RM(exerciseName: String, excludeSessionId: Long): Double? = 
        workoutDao.getHistoricBest1RM(exerciseName, excludeSessionId)
}
