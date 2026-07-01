package com.example.gymdiary3.system.session

import com.example.gymdiary3.domain.model.WorkoutSession
import com.example.gymdiary3.domain.analyzer.WorkoutAnalyzer
import com.example.gymdiary3.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(private val workoutRepository: WorkoutRepository) {
    private val _currentSessionId = MutableStateFlow<Int?>(null)
    val currentSessionId: StateFlow<Int?> = _currentSessionId.asStateFlow()

    private var currentStartTime: Long = 0L

    suspend fun initialize() {
        if (_currentSessionId.value != null) return
        val activeSession = workoutRepository.getActiveSession()
        if (activeSession != null) {
            // Safety: If an active session is found but it's older than 12 hours, clear it
            val isStale = (System.currentTimeMillis() - activeSession.startTime) > 12 * 60 * 60 * 1000
            if (isStale) {
                // End it automatically or delete if empty
                val sessionWithSets = workoutRepository.getSessionWithSetsById(activeSession.id)
                if (sessionWithSets == null || sessionWithSets.sets.isEmpty()) {
                    workoutRepository.deleteSession(activeSession)
                } else {
                    workoutRepository.updateSession(activeSession.copy(endTime = activeSession.startTime + 60 * 60 * 1000))
                }
                _currentSessionId.value = null
                currentStartTime = 0L
            } else {
                _currentSessionId.value = activeSession.id
                currentStartTime = activeSession.startTime
            }
        }
    }

    suspend fun startSession(
        sessionDateMillis: Long = System.currentTimeMillis(),
        name: String? = null,
        notes: String? = null
    ) {
        if (_currentSessionId.value != null) return
        currentStartTime = sessionDateMillis
        val session = WorkoutSession(
            startTime = currentStartTime,
            endTime = null,
            name = name,
            notes = notes
        )
        val id = workoutRepository.insertSession(session).toInt()
        _currentSessionId.value = id
    }

    fun clearSessionManually() {
        _currentSessionId.value = null
        currentStartTime = 0L
    }

    suspend fun endSession(onComplete: (Int) -> Unit) {
        val id = _currentSessionId.value ?: return

        val sessionWithSets = workoutRepository.getSessionWithSetsById(id)
        
        // Safety: If duration is > 24 hours, don't delete it even if empty, something is weird
        val duration = System.currentTimeMillis() - currentStartTime
        val isExtremelyLong = duration > 24 * 60 * 60 * 1000
        
        if (!isExtremelyLong && (sessionWithSets == null || !WorkoutAnalyzer.isValidSession(sessionWithSets))) {
            // Delete empty or invalid session
            val session = sessionWithSets?.session ?: workoutRepository.getSessionById(id)
            if (session != null) {
                workoutRepository.deleteSession(session)
            }
            _currentSessionId.value = null
            currentStartTime = 0L
            onComplete(-1) 
            return
        }

        val existingSession = sessionWithSets?.session ?: workoutRepository.getSessionById(id)
        if (existingSession != null) {
            val session = existingSession.copy(endTime = System.currentTimeMillis())
            workoutRepository.updateSession(session)
        }

        _currentSessionId.value = null
        currentStartTime = 0L
        onComplete(id)
    }
}
