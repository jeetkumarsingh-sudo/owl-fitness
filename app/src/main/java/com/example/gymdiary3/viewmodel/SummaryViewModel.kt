package com.example.gymdiary3.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdiary3.domain.model.SessionWithSets
import com.example.gymdiary3.domain.repository.SettingsRepository
import com.example.gymdiary3.domain.repository.WorkoutRepository
import com.example.gymdiary3.presentation.history.SummaryStateBuilder
import com.example.gymdiary3.presentation.history.SummaryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    workoutRepository: WorkoutRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val sessionId: Int = savedStateHandle.get<String>("sessionId")?.toIntOrNull() ?: 0

    val unit: StateFlow<String> = settingsRepository.userSettingsFlow.map { it.weightUnit }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "kg")

    /** The raw session (for sharing) and its screen state. */
    val data: StateFlow<Pair<SessionWithSets, SummaryUiState>?> = combine(
        workoutRepository.getSessionsWithSets(), unit
    ) { all, u ->
        all.firstOrNull { it.session.id == sessionId }?.let { it to SummaryStateBuilder.build(it, all, u) }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
