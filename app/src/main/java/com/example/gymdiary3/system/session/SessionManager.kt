package com.example.gymdiary3.system.session

import com.example.gymdiary3.domain.model.WorkoutSession
import com.example.gymdiary3.domain.model.SessionWithSets
import com.example.gymdiary3.database.WorkoutDao
import com.example.gymdiary3.domain.analyzer.WorkoutAnalyzer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import com.example.gymdiary3.domain.repository.WorkoutRepository

class SessionManager(private val workoutRepository: WorkoutRepository) {
    private val _currentSessionId = MutableStateFlow<Int?>(null)
    val currentSessionId: StateFlow<Int?> = _currentSessionId.asStateFlow()

    private var currentStartTime: Long = 0L

    suspend fun startSession() {
        if (_currentSessionId.value != null) return
        currentStartTime = System.currentTimeMillis()
        val session = WorkoutSession(
            startTime = currentStartTime,
            endTime = null
        )
        val id = workoutRepository.insertSession(session).toInt()
        _currentSessionId.value = id
    }

    suspend fun endSession(onComplete: (Int) -> Unit) {
        val id = _currentSessionId.value ?: return
        
        val sessionWithSets = workoutRepository.getSessionWithSetsById(id)
        if (sessionWithSets == null || !WorkoutAnalyzer.isValidSession(sessionWithSets)) {
            val session = sessionWithSets?.session ?: workoutRepository.getSessionById(id)
            workoutRepository.deleteSession(session)
            _currentSessionId.value = null
            currentStartTime = 0L
            return
        }

        val session = WorkoutSession(
            id = id,
            startTime = currentStartTime,
            endTime = System.currentTimeMillis()
        )
        workoutRepository.updateSession(session)
        _currentSessionId.value = null
        currentStartTime = 0L
        onComplete(id)
    }
}
