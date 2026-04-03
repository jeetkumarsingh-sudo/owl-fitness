package com.example.gymdiary3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdiary3.data.BodyWeight
import com.example.gymdiary3.database.BodyWeightDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BodyWeightViewModel(private val bodyWeightDao: BodyWeightDao) : ViewModel() {

    val allWeights: StateFlow<List<BodyWeight>> = bodyWeightDao.getWeights()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insertWeight(weight: Double) {
        viewModelScope.launch {
            val bodyWeight = BodyWeight(
                date = System.currentTimeMillis(),
                weight = weight
            )
            bodyWeightDao.insertWeight(bodyWeight)
        }
    }
}
