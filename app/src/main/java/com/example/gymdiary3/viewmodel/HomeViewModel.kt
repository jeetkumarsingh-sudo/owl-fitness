package com.example.gymdiary3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdiary3.domain.analyzer.WorkoutAnalyzer
import com.example.gymdiary3.domain.repository.SettingsRepository
import com.example.gymdiary3.domain.repository.WorkoutRepository
import com.example.gymdiary3.domain.usecase.analytics.GenerateFitnessInsightsUseCase
import com.example.gymdiary3.domain.usecase.workout.EndSessionUseCase
import com.example.gymdiary3.domain.usecase.workout.StartSessionUseCase
import com.example.gymdiary3.presentation.home.HomeStateBuilder
import com.example.gymdiary3.presentation.home.HomeUiState
import com.example.gymdiary3.system.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    settingsRepository: SettingsRepository,
    private val sessionManager: SessionManager,
    private val startSessionUseCase: StartSessionUseCase,
    private val endSessionUseCase: EndSessionUseCase,
    insights: GenerateFitnessInsightsUseCase,
) : ViewModel() {

    init {
        viewModelScope.launch { sessionManager.initialize() }
    }

    val state: StateFlow<HomeUiState?> = combine(
        workoutRepository.getSessionsWithSets().map { WorkoutAnalyzer.filterValidSessions(it) },
        sessionManager.currentSessionId,
        settingsRepository.userSettingsFlow,
        insights()
    ) { sessions, activeId, settings, engineInsights ->
        HomeStateBuilder.build(sessions, activeId, engineInsights, settings.weightUnit, System.currentTimeMillis())
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Live clock for the active session, ticking once a second. */
    val elapsedSeconds: StateFlow<Long> = sessionElapsedSeconds(sessionManager, workoutRepository)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun start(yesterday: Boolean, onStarted: () -> Unit) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            startSessionUseCase(if (yesterday) now - 24L * 60 * 60 * 1000 else now)
            onStarted()
        }
    }

    fun finish(onDone: (Int) -> Unit) {
        viewModelScope.launch { endSessionUseCase(onDone) }
    }
}
