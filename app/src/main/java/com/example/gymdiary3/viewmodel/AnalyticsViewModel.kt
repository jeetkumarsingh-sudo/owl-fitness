package com.example.gymdiary3.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdiary3.data.WorkoutSet
import com.example.gymdiary3.database.WorkoutDao
import com.example.gymdiary3.domain.analyzer.WorkoutAnalyzer
import com.example.gymdiary3.domain.service.RecommendationEngine
import com.example.gymdiary3.presentation.state.ExerciseUiState
import kotlinx.coroutines.flow.*

import com.example.gymdiary3.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    val exerciseName: String = savedStateHandle["exercise"] ?: ""

    init {
        if (exerciseName.isEmpty()) {
            android.util.Log.e("AnalyticsVM", "Missing exercise argument")
        }
    }

    // Single source of truth — one DB subscription
    private val exerciseSets: StateFlow<List<WorkoutSet>> = workoutRepository.getWorkouts()
        .map { allSets -> allSets.filter { it.exercise == exerciseName } }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exerciseUiState: StateFlow<ExerciseUiState?> = exerciseSets
        .map { sets ->
            if (sets.isEmpty()) null
            else {
                val stats = WorkoutAnalyzer.getExerciseStats(exerciseName, sets)
                ExerciseUiState(
                    exercise = stats.exercise,
                    trend = stats.trend,
                    trendLabel = WorkoutAnalyzer.getTrendLabel(stats.trend),
                    isPR = stats.isPR,
                    recommendation = RecommendationEngine.getRecommendation(stats),
                    best1RM = stats.best1RM,
                    totalVolume = stats.totalVolume
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val oneRMHistory: StateFlow<List<Pair<Long, Double>>> = exerciseSets
        .map { sets -> WorkoutAnalyzer.get1RMHistory(exerciseName, sets) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val volumeHistory: StateFlow<List<Pair<String, Double>>> = exerciseSets
        .map { sets -> WorkoutAnalyzer.getExerciseVolumeHistory(exerciseName, sets) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val globalVolumeHistory: StateFlow<List<Pair<String, Double>>> = workoutRepository.getSessionsWithSets()
        .map { WorkoutAnalyzer.getVolumeHistory(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
