package com.example.gymdiary3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdiary3.domain.analyzer.WorkoutAnalyzer
import com.example.gymdiary3.domain.repository.SettingsRepository
import com.example.gymdiary3.domain.repository.WorkoutRepository
import com.example.gymdiary3.presentation.history.HistoryStateBuilder
import com.example.gymdiary3.presentation.history.HistoryUiState
import com.example.gymdiary3.system.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    settingsRepository: SettingsRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    init {
        viewModelScope.launch { workoutRepository.deleteEmptySessions() }
    }

    val state: StateFlow<HistoryUiState?> = combine(
        workoutRepository.getSessionsWithSets().map { WorkoutAnalyzer.filterValidSessions(it) },
        sessionManager.currentSessionId,
        settingsRepository.userSettingsFlow
    ) { sessions, active, s -> HistoryStateBuilder.build(sessions, active, s.weightUnit) }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun delete(id: Int) {
        viewModelScope.launch {
            if (sessionManager.currentSessionId.value == id) sessionManager.clearSessionManually()
            workoutRepository.deleteSessionById(id)
        }
    }
}
