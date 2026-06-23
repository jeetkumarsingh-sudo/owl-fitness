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

import android.content.Context
import android.net.Uri
import com.example.gymdiary3.system.backup.BackupManager
// ...
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    suspend fun exportJson(context: Context): Uri? {
        return backupManager.exportJson(context)
    }

    suspend fun importJson(context: Context, uri: Uri): Result<Unit> {
        return backupManager.importJson(context, uri)
    }
// ...
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

    fun updateBarWeight(weight: Double) {
        viewModelScope.launch {
            repository.updateBarWeight(weight)
        }
    }
}
