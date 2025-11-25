package com.cirin0.worktimetracker.features.auth.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.core.network.apiCall
import com.cirin0.worktimetracker.features.auth.data.api.AuthApi
import com.cirin0.worktimetracker.features.auth.data.model.LoginRequest
import com.cirin0.worktimetracker.features.auth.data.model.RegisterRequest
import com.cirin0.worktimetracker.features.auth.data.model.User
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class AuthRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val authApi: AuthApi
) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
    }

    val authToken: Flow<String?> = dataStore.data.map { prefs ->
        prefs[TOKEN_KEY]
    }

    val isAuthenticated: Flow<Boolean> = authToken.map { it != null }

    private suspend fun saveToken(token: String) {
        dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    suspend fun clearToken() {
        dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }

    suspend fun getToken(): String? {
        return dataStore.data.first()[TOKEN_KEY]
    }

    suspend fun login(email: String, password: String): ApiResponse<User> {
        return apiCall {
            val response = authApi.login(LoginRequest(email, password))
            saveToken(response.accessToken)
            response.user
        }
    }

    suspend fun register(name: String, email: String, password: String): ApiResponse<String> {
        return apiCall {
            val response = authApi.register(RegisterRequest(name, email, password))
            response.message
        }
    }

    suspend fun logout(): ApiResponse<String> {
        return apiCall {
            val response = authApi.logout()
            clearToken()
            response.message
        }
    }

    suspend fun getCurrentUser(): ApiResponse<User> {
        return apiCall {
            val user = authApi.getCurrentUser()
            user
        }
    }
}