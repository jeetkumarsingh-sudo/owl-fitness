package com.example.gymdiary3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdiary3.data.BodyWeight
import com.example.gymdiary3.domain.repository.BodyWeightRepository
import com.example.gymdiary3.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

@HiltViewModel
class BodyWeightViewModel @Inject constructor(
    private val bodyWeightRepository: BodyWeightRepository,
    val settingsRepository: SettingsRepository
) : ViewModel() {

    val allWeights: StateFlow<List<BodyWeight>> = bodyWeightRepository.getWeights()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val latestBodyWeight: StateFlow<com.example.gymdiary3.data.BodyWeight?> = bodyWeightRepository.getWeights()
        .map { it.maxByOrNull { bw -> bw.timestamp } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun insertWeight(weight: Double) {
        viewModelScope.launch {
            val todayStart = getStartOfDayTimestamp()
            val todayEnd = todayStart + 86_400_000L
            val existing = bodyWeightRepository.getWeightBetween(todayStart, todayEnd).firstOrNull()
            
            if (existing != null) {
                bodyWeightRepository.updateWeight(existing.copy(weight = weight))
            } else {
                val bodyWeight = BodyWeight(
                    timestamp = System.currentTimeMillis(),
                    weight = weight
                )
                bodyWeightRepository.insertWeight(bodyWeight)
            }
        }
    }

    fun deleteWeight(bodyWeight: BodyWeight) {
        viewModelScope.launch {
            bodyWeightRepository.deleteWeight(bodyWeight)
        }
    }

    private fun getStartOfDayTimestamp(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
