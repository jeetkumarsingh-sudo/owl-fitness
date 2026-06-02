package com.example.gymdiary3.domain.usecase.workout

import com.example.gymdiary3.system.session.SessionManager
import javax.inject.Inject

class StartSessionUseCase @Inject constructor(
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(sessionDateMillis: Long = System.currentTimeMillis()) {
        sessionManager.startSession(sessionDateMillis)
    }
}
