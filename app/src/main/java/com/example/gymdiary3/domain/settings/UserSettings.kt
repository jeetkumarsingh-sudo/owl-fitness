package com.example.gymdiary3.domain.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

data class UserSettings(
    val weightUnit: String = "kg",
    val defaultRestSeconds: Int = 90
)

class UserSettingsRepository(private val context: Context) {
    private object PreferencesKeys {
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val DEFAULT_REST_SECONDS = intPreferencesKey("default_rest_seconds")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data
        .map { preferences ->
            UserSettings(
                weightUnit = preferences[PreferencesKeys.WEIGHT_UNIT] ?: "kg",
                defaultRestSeconds = preferences[PreferencesKeys.DEFAULT_REST_SECONDS] ?: 90
            )
        }

    suspend fun updateWeightUnit(unit: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEIGHT_UNIT] = unit
        }
    }

    suspend fun updateDefaultRestSeconds(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_REST_SECONDS] = seconds
        }
    }
}
