package com.example.gymdiary3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdiary3.data.BodyWeight
import com.example.gymdiary3.database.BodyWeightDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class BodyWeightViewModel(private val bodyWeightDao: BodyWeightDao) : ViewModel() {

    val allWeights: StateFlow<List<BodyWeight>> = bodyWeightDao.getWeights()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insertWeight(weight: Double) {
        viewModelScope.launch {
            val todayStart = getStartOfDayTimestamp()
            val todayEnd = todayStart + 86_400_000L
            val existing = bodyWeightDao.getWeightBetween(todayStart, todayEnd).firstOrNull()
            
            if (existing != null) {
                bodyWeightDao.updateWeight(existing.copy(weight = weight))
            } else {
                val bodyWeight = BodyWeight(
                    date = System.currentTimeMillis(),
                    weight = weight
                )
                bodyWeightDao.insertWeight(bodyWeight)
            }
        }
    }

    fun deleteWeight(bodyWeight: BodyWeight) {
        viewModelScope.launch {
            bodyWeightDao.deleteWeight(bodyWeight)
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
