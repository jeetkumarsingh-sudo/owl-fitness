package com.example.gymdiary3.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdiary3.domain.model.WorkoutSet
import com.example.gymdiary3.domain.analyzer.WorkoutAnalyzer
import com.example.gymdiary3.presentation.state.ExerciseUiState
import com.example.gymdiary3.domain.repository.WorkoutRepository
import com.example.gymdiary3.domain.usecase.analytics.GenerateFitnessInsightsUseCase
import com.example.gymdiary3.intelligence.model.FitnessInsight
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*

@HiltViewModel
class ProgressViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    workoutRepository: WorkoutRepository,
    generateFitnessInsightsUseCase: GenerateFitnessInsightsUseCase,
) : ViewModel() {

    val exerciseName: String = savedStateHandle["exercise"] ?: ""

    // Single source of truth — one DB subscription
    private val exerciseSets: StateFlow<List<WorkoutSet>> = workoutRepository.getAllSets()
        .map { allSets -> allSets.filter { it.exercise == exerciseName } }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exerciseUiState: StateFlow<ExerciseUiState?> = exerciseSets
        .map { sets ->
            if (sets.isEmpty()) null
            else {
                val stats = WorkoutAnalyzer.getExerciseStats(exerciseName, sets)
                val recommendation = when {
                    stats.isPR -> "New personal record! Consider increasing weight next session."
                    stats.trend > 0 -> "Progressing well. Maintain current overload strategy."
                    stats.trend < 0 -> "Slight regression. Check recovery and nutrition."
                    else -> "Weight stable. Try increasing reps or weight next session."
                }
                ExerciseUiState(
                    exercise = stats.exercise,
                    trend = stats.trend,
                    trendLabel = WorkoutAnalyzer.getTrendLabel(stats.trend),
                    isPR = stats.isPR,
                    recommendation = recommendation,
                    best1RM = stats.best1RM,
                    totalVolume = stats.totalVolume
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val oneRMHistory: StateFlow<List<Pair<Long, Double>>> = exerciseSets
        .map { sets -> WorkoutAnalyzer.get1RMHistory(sets) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val volumeHistory: StateFlow<List<Pair<String, Double>>> = exerciseSets
        .map { sets -> WorkoutAnalyzer.getExerciseVolumeHistory(sets) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fitnessInsights: StateFlow<List<FitnessInsight>> = generateFitnessInsightsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
