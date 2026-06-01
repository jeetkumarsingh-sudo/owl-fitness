package com.example.gymdiary3.domain.repository

import com.example.gymdiary3.domain.settings.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val userSettingsFlow: Flow<UserSettings>
    suspend fun updateWeightUnit(unit: String)
    suspend fun updateDefaultRestSeconds(seconds: Int)
}
