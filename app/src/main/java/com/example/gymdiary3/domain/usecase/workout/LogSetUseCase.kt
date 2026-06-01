package com.example.gymdiary3.domain.usecase.workout

import com.example.gymdiary3.domain.model.WorkoutSet
import com.example.gymdiary3.domain.repository.SettingsRepository
import com.example.gymdiary3.domain.repository.WorkoutRepository
import com.example.gymdiary3.system.timer.RestTimerManager
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class LogSetUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val settingsRepository: SettingsRepository,
    private val restTimerManager: RestTimerManager
) {
    suspend operator fun invoke(
        sessionId: Int,
        muscle: String,
        exercise: String,
        setNumber: Int,
        reps: Int,
        weight: Double,
        isAssisted: Boolean,
        rpe: Float? = null,
        notes: String? = null
    ) {
        require(reps > 0) { "Reps must be > 0" }
        require(weight >= 0) { "Weight must be >= 0" }

        val set = WorkoutSet(
            id = 0,
            timestamp = System.currentTimeMillis(),
            muscle = muscle,
            exercise = exercise,
            setNumber = setNumber,
            reps = reps,
            weight = weight,
            isAssisted = isAssisted,
            sessionId = sessionId,
            rpe = rpe,
            notes = notes
        )
        workoutRepository.insertSet(set)

        val restSeconds = settingsRepository.userSettingsFlow
            .firstOrNull()?.defaultRestSeconds ?: 90
        restTimerManager.startTimer(restSeconds)
    }
}
