package com.example.gymdiary3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdiary3.domain.analyzer.WorkoutAnalyzer
import com.example.gymdiary3.domain.repository.SettingsRepository
import com.example.gymdiary3.domain.repository.WorkoutRepository
import com.example.gymdiary3.domain.usecase.analytics.GenerateFitnessInsightsUseCase
import com.example.gymdiary3.presentation.progress.ProgressStateBuilder
import com.example.gymdiary3.presentation.progress.ProgressUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ProgressOverviewViewModel @Inject constructor(
    workoutRepository: WorkoutRepository,
    settingsRepository: SettingsRepository,
    insights: GenerateFitnessInsightsUseCase,
) : ViewModel() {

    val unit: StateFlow<String> = settingsRepository.userSettingsFlow.map { it.weightUnit }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "kg")

    val state: StateFlow<ProgressUiState?> = combine(
        workoutRepository.getSessionsWithSets().map { WorkoutAnalyzer.filterValidSessions(it) },
        insights(),
        unit
    ) { sessions, engine, u -> ProgressStateBuilder.build(sessions, engine, u, System.currentTimeMillis()) }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
