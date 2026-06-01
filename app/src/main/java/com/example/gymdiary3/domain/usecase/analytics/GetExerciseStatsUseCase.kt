package com.example.gymdiary3.domain.usecase.analytics

import com.example.gymdiary3.domain.analyzer.WorkoutAnalyzer
import com.example.gymdiary3.domain.model.ExerciseStats
import com.example.gymdiary3.domain.repository.WorkoutRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetExerciseStatsUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    operator fun invoke(exerciseName: String): Flow<ExerciseStats> =
        workoutRepository.getAllSets()
            .map { allSets -> allSets.filter { it.exercise == exerciseName } }
            .distinctUntilChanged()
            .map { sets -> WorkoutAnalyzer.getExerciseStats(exerciseName, sets) }
            .flowOn(Dispatchers.Default)
}
