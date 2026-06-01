package com.example.gymdiary3.data.repository

import com.example.gymdiary3.data.WorkoutSet
import com.example.gymdiary3.data.WorkoutSession
import com.example.gymdiary3.data.SessionWithSets
import com.example.gymdiary3.database.WorkoutDao
import com.example.gymdiary3.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WorkoutRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao
) : WorkoutRepository {
    override fun getWorkouts(): Flow<List<WorkoutSet>> = workoutDao.getWorkouts()
    override fun getSessionsWithSets(): Flow<List<SessionWithSets>> = workoutDao.getSessionsWithSets()
    override suspend fun insertWorkout(workout: WorkoutSet) = workoutDao.insertWorkout(workout)
    override suspend fun insertSession(session: WorkoutSession): Long = workoutDao.insertSession(session)
    override suspend fun updateSession(session: WorkoutSession) = workoutDao.updateSession(session)
    override suspend fun deleteSession(session: WorkoutSession) = workoutDao.deleteSession(session)
    override suspend fun deleteEmptySessions() = workoutDao.deleteEmptySessions()
    override suspend fun getSessionWithSetsById(sessionId: Int): SessionWithSets? = workoutDao.getSessionWithSetsById(sessionId)
    override suspend fun getSessionById(sessionId: Int): WorkoutSession = workoutDao.getSessionById(sessionId)
    override suspend fun deleteSessionById(id: Int) = workoutDao.deleteSessionById(id)
    override suspend fun getLastSet(exerciseName: String): WorkoutSet? = workoutDao.getLastSet(exerciseName)
    override fun getLastThreeSets(exerciseName: String): Flow<List<WorkoutSet>> = workoutDao.getLastThreeSets(exerciseName)
    override suspend fun getTodaySetCount(exerciseName: String, dayStart: Long, dayEnd: Long): Int = workoutDao.getTodaySetCount(exerciseName, dayStart, dayEnd)
    override fun getSetsForExerciseInDateRange(exerciseName: String, weekStart: Long, weekEnd: Long): Flow<List<WorkoutSet>> = workoutDao.getSetsForExerciseInDateRange(exerciseName, weekStart, weekEnd)
    override fun getLastSessionSetsForExercise(exerciseName: String, currentSessionId: Int): Flow<List<WorkoutSet>> = workoutDao.getLastSessionSetsForExercise(exerciseName, currentSessionId)
    override suspend fun getHistoricBest1RM(exerciseName: String, excludeSessionId: Long): Double? = workoutDao.getHistoricBest1RM(exerciseName, excludeSessionId)
}
