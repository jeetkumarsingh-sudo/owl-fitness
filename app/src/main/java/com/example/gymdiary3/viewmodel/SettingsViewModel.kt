package com.example.gymdiary3.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdiary3.data.FileHandler
import com.example.gymdiary3.domain.analyzer.WorkoutAnalyzer
import com.example.gymdiary3.domain.repository.BodyWeightRepository
import com.example.gymdiary3.domain.repository.SettingsRepository
import com.example.gymdiary3.domain.repository.WorkoutRepository
import com.example.gymdiary3.domain.settings.UserSettings
import com.example.gymdiary3.system.backup.BackupManager
import com.example.gymdiary3.system.export.ExportFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val backupManager: BackupManager,
    private val workoutRepository: WorkoutRepository,
    private val bodyWeightRepository: BodyWeightRepository,
) : ViewModel() {

    val userSettings: StateFlow<UserSettings> = repository.userSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    suspend fun exportJson(context: Context): Uri? = backupManager.exportJson(context)

    suspend fun importJson(context: Context, uri: Uri): Result<Unit> = backupManager.importJson(context, uri)

    /** All workouts and body weight as CSV in the cache dir; null when there is nothing to export. */
    suspend fun exportCsv(context: Context): Uri? = withContext(Dispatchers.IO) {
        val sessions = WorkoutAnalyzer.filterValidSessions(workoutRepository.getSessionsWithSets().first())
        if (sessions.isEmpty()) return@withContext null
        val unit = repository.userSettingsFlow.first().weightUnit // the stored value, not the UI's possibly-initial copy
        val csv = ExportFormatter.buildCsv(sessions, bodyWeightRepository.getAllWeights(), unit)
        FileHandler.writeToCache(context, csv)
    }

    fun updateWeightUnit(unit: String) { viewModelScope.launch { repository.updateWeightUnit(unit) } }
    fun updateDefaultRestSeconds(seconds: Int) { viewModelScope.launch { repository.updateDefaultRestSeconds(seconds) } }
    fun updateBarWeight(weight: Double) { viewModelScope.launch { repository.updateBarWeight(weight) } }
}
