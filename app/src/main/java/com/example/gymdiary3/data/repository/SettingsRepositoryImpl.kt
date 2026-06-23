package com.example.gymdiary3.data.repository

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.gymdiary3.domain.repository.SettingsRepository
import com.example.gymdiary3.domain.settings.UserSettings
import com.example.gymdiary3.domain.settings.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

import dagger.hilt.android.qualifiers.ApplicationContext

class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private object PreferencesKeys {
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val DEFAULT_REST_SECONDS = intPreferencesKey("default_rest_seconds")
        val BAR_WEIGHT = doublePreferencesKey("bar_weight")
    }

    override val userSettingsFlow: Flow<UserSettings> = context.dataStore.data
        .map { preferences ->
            UserSettings(
                weightUnit = preferences[PreferencesKeys.WEIGHT_UNIT] ?: "kg",
                defaultRestSeconds = preferences[PreferencesKeys.DEFAULT_REST_SECONDS] ?: 90,
                barWeight = preferences[PreferencesKeys.BAR_WEIGHT] ?: 20.0
            )
        }

    override suspend fun updateWeightUnit(unit: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEIGHT_UNIT] = unit
        }
    }

    override suspend fun updateDefaultRestSeconds(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_REST_SECONDS] = seconds
        }
    }

    override suspend fun updateBarWeight(weight: Double) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BAR_WEIGHT] = weight
        }
    }
}
