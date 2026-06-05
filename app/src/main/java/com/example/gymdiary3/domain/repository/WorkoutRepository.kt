package com.example.gymdiary3.domain.repository

import com.example.gymdiary3.domain.model.WorkoutSet
import com.example.gymdiary3.domain.model.WorkoutSession
import com.example.gymdiary3.domain.model.SessionWithSets
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun getAllSets(): Flow<List<WorkoutSet>>
    fun getSessionsWithSets(): Flow<List<SessionWithSets>>
    suspend fun insertSet(set: WorkoutSet)
    suspend fun insertSession(session: WorkoutSession): Long
    suspend fun updateSession(session: WorkoutSession)
    suspend fun deleteSession(session: WorkoutSession)
    suspend fun deleteEmptySessions()
    suspend fun getSessionWithSetsById(sessionId: Int): SessionWithSets?
    fun getSessionWithSetsFlowById(sessionId: Int): Flow<SessionWithSets?>
    fun getSessionFlowById(sessionId: Int): Flow<WorkoutSession?>
    suspend fun getActiveSession(): WorkoutSession?
    suspend fun getSessionById(sessionId: Int): WorkoutSession?
    suspend fun deleteSessionById(id: Int)
    suspend fun getLastSet(exerciseName: String): WorkoutSet?
    fun getLastThreeSets(exerciseName: String): Flow<List<WorkoutSet>>
    suspend fun getTodaySetCount(exerciseName: String, dayStart: Long, dayEnd: Long): Int
    suspend fun getSessionSetCount(exerciseName: String, sessionId: Int): Int
    fun getSetsForExerciseInDateRange(exerciseName: String, weekStart: Long, weekEnd: Long): Flow<List<WorkoutSet>>
    fun getLastSessionSetsForExercise(exerciseName: String, currentSessionId: Int): Flow<List<WorkoutSet>>
    suspend fun getHistoricBest1RM(exerciseName: String, excludeSessionId: Long): Double?
}
