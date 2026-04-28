package com.cirin0.worktimetracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val PRE_WORK_NOTIFICATION_ENABLED = booleanPreferencesKey("pre_work_notification_enabled")
        val PRE_WORK_NOTIFICATION_MINUTES = intPreferencesKey("pre_work_notification_minutes")
    }

    val isDarkMode = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE]
        }

    val isPreWorkNotificationEnabled = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.PRE_WORK_NOTIFICATION_ENABLED] ?: false
        }

    val preWorkNotificationMinutes = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.PRE_WORK_NOTIFICATION_MINUTES] ?: 15
        }

    suspend fun setDarkMode(isDarkMode: Boolean?) {
        context.dataStore.edit { preferences ->
            when (isDarkMode) {
                null -> preferences.remove(PreferencesKeys.IS_DARK_MODE)
                else -> preferences[PreferencesKeys.IS_DARK_MODE] = isDarkMode
            }
        }
    }

    suspend fun setPreWorkNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PRE_WORK_NOTIFICATION_ENABLED] = enabled
        }
    }

    suspend fun setPreWorkNotificationMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PRE_WORK_NOTIFICATION_MINUTES] = minutes
        }
    }
}
