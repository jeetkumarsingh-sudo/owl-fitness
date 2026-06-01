package com.example.gymdiary3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdiary3.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repository: SettingsRepository) : ViewModel() {
    val userSettings = repository.userSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.gymdiary3.domain.settings.UserSettings())

    val defaultRestSeconds: StateFlow<Int> = repository.userSettingsFlow
        .map { it.defaultRestSeconds }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 90)

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
