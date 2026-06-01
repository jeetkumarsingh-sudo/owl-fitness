package com.example.gymdiary3.domain.usecase.analytics

import com.example.gymdiary3.intelligence.model.FitnessInsight
import com.example.gymdiary3.domain.repository.WorkoutRepository
import com.example.gymdiary3.intelligence.FitnessIntelligenceEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GenerateFitnessInsightsUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val fitnessIntelligenceEngine: FitnessIntelligenceEngine
) {
    operator fun invoke(): Flow<List<FitnessInsight>> =
        workoutRepository.getSessionsWithSets()
            .map { sessions -> fitnessIntelligenceEngine.analyze(sessions) }
            .flowOn(Dispatchers.Default)
}
