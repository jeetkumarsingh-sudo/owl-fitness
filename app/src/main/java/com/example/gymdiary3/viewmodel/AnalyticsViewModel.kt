package com.example.gymdiary3.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdiary3.database.WorkoutDao
import com.example.gymdiary3.domain.analyzer.WorkoutAnalyzer
import com.example.gymdiary3.domain.service.RecommendationEngine
import com.example.gymdiary3.presentation.state.ExerciseUiState
import kotlinx.coroutines.flow.*

class AnalyticsViewModel(
    savedStateHandle: SavedStateHandle,
    private val workoutDao: WorkoutDao
) : ViewModel() {

    val exerciseName: String = savedStateHandle["exercise"] ?: ""

    init {
        if (exerciseName.isEmpty()) {
            android.util.Log.e("AnalyticsVM", "Missing exercise argument")
        }
    }

    val exerciseUiState: StateFlow<ExerciseUiState?> = workoutDao.getWorkouts()
        .map { allSets ->
            val exerciseSets = allSets.filter { it.exercise == exerciseName }
            if (exerciseSets.isEmpty()) null
            else {
                val stats = WorkoutAnalyzer.getExerciseStats(exerciseName, exerciseSets)
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

    val oneRMHistory: StateFlow<List<Pair<Long, Double>>> = workoutDao.getWorkouts()
        .map { allSets ->
            val exerciseSets = allSets.filter { it.exercise == exerciseName }
            WorkoutAnalyzer.get1RMHistory(exerciseName, exerciseSets)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val volumeHistory: StateFlow<List<Pair<String, Double>>> = workoutDao.getWorkouts()
        .map { allSets ->
            val exerciseSets = allSets.filter { it.exercise == exerciseName }
            WorkoutAnalyzer.getExerciseVolumeHistory(exerciseName, exerciseSets)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val globalVolumeHistory: StateFlow<List<Pair<String, Double>>> = workoutDao.getSessionsWithSets()
        .map { WorkoutAnalyzer.getVolumeHistory(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
