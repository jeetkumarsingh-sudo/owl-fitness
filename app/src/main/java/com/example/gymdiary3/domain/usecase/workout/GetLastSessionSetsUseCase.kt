package com.example.gymdiary3.domain.usecase.workout

import com.example.gymdiary3.domain.model.WorkoutSet
import com.example.gymdiary3.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLastSessionSetsUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    operator fun invoke(exerciseName: String, currentSessionId: Int): Flow<List<WorkoutSet>> =
        workoutRepository.getLastSessionSetsForExercise(exerciseName, currentSessionId)
}
