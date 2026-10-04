package com.example.gymdiary3.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdiary3.domain.repository.SettingsRepository
import com.example.gymdiary3.domain.repository.WorkoutRepository
import com.example.gymdiary3.presentation.exercise.ExerciseDetailStateBuilder
import com.example.gymdiary3.presentation.exercise.ExerciseDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    workoutRepository: WorkoutRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val exercise: String = Uri.decode(savedStateHandle.get<String>("exercise").orEmpty())

    private val sets = workoutRepository.getAllSets().map { all -> all.filter { it.exercise == exercise } }

    /** The muscle this exercise is logged under, for opening the logger from here. */
    var muscle: String = ""
        private set

    val unit: StateFlow<String> = settingsRepository.userSettingsFlow.map { it.weightUnit }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "kg")

    val state: StateFlow<ExerciseDetailUiState?> = combine(sets, unit) { s, u ->
        muscle = s.maxByOrNull { it.timestamp }?.muscle.orEmpty()
        ExerciseDetailStateBuilder.build(exercise, s, u, System.currentTimeMillis())
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
