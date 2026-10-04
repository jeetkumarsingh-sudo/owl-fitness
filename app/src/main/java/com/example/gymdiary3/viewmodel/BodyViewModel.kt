package com.example.gymdiary3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdiary3.domain.model.BodyWeight
import com.example.gymdiary3.domain.repository.BodyWeightRepository
import com.example.gymdiary3.domain.repository.SettingsRepository
import com.example.gymdiary3.domain.repository.WorkoutRepository
import com.example.gymdiary3.domain.settings.WeightFormatter
import com.example.gymdiary3.presentation.body.BodyStateBuilder
import com.example.gymdiary3.presentation.body.BodyUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/** Body tab: body weight and muscle recovery. */
@HiltViewModel
class BodyViewModel @Inject constructor(
    private val bodyWeightRepository: BodyWeightRepository,
    workoutRepository: WorkoutRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val unit: StateFlow<String> = settingsRepository.userSettingsFlow.map { it.weightUnit }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "kg")

    val state: StateFlow<BodyUiState?> = combine(
        bodyWeightRepository.getWeights(),
        workoutRepository.getSessionsWithSets(),
        unit
    ) { weights, sessions, u ->
        BodyStateBuilder.build(
            weights, sessions.flatMap { it.sets }, sessions.map { it.session.startTime }, u, System.currentTimeMillis()
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** One entry per day: logging again today updates today's entry. [value] is in the user's unit. */
    fun log(value: Double) {
        viewModelScope.launch {
            val kg = WeightFormatter.toKilograms(value, unit.value)
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val existing = bodyWeightRepository.getWeightBetween(todayStart, todayStart + 86_400_000L).firstOrNull()
            if (existing != null) bodyWeightRepository.updateWeight(existing.copy(weight = kg))
            else bodyWeightRepository.insertWeight(BodyWeight(timestamp = System.currentTimeMillis(), weight = kg))
        }
    }

    fun delete(entry: BodyWeight) {
        viewModelScope.launch { bodyWeightRepository.deleteWeight(entry) }
    }
}
