package com.example.gymdiary3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdiary3.domain.settings.UserSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: UserSettingsRepository) : ViewModel() {
    val userSettings = repository.userSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.gymdiary3.domain.settings.UserSettings())

    fun updateWeightUnit(unit: String) {
        viewModelScope.launch {
            repository.updateWeightUnit(unit)
        }
    }

    fun updateDefaultRestSeconds(seconds: Int) {
        viewModelScope.launch {
            repository.updateDefaultRestSeconds(seconds)
        }
    }
}
