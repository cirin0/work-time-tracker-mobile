package com.cirin0.worktimetracker.features.auth.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.cirin0.worktimetracker.core.database.dao.CompanyDao
import com.cirin0.worktimetracker.core.database.dao.LeaveRequestDao
import com.cirin0.worktimetracker.core.database.dao.TimeEntryDao
import com.cirin0.worktimetracker.core.database.dao.TimesheetDao
import com.cirin0.worktimetracker.core.database.dao.UserDao
import com.cirin0.worktimetracker.core.database.dao.WorkScheduleDao
import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.core.network.apiCall
import com.cirin0.worktimetracker.core.utils.Constants
import com.cirin0.worktimetracker.features.auth.data.api.AuthApi
import com.cirin0.worktimetracker.features.auth.data.model.FcmTokenRequest
import com.cirin0.worktimetracker.features.auth.data.model.LoginRequest
import com.cirin0.worktimetracker.features.auth.data.model.RegisterRequest
import com.cirin0.worktimetracker.features.auth.data.model.RegisterResponse
import com.cirin0.worktimetracker.features.auth.data.model.ResendVerificationRequest
import com.cirin0.worktimetracker.features.auth.data.model.VerifyEmailRequest
import com.cirin0.worktimetracker.features.profile.data.model.User
import com.google.firebase.messaging.FirebaseMessaging
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Singleton
class AuthRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val authApi: AuthApi,
    private val userDao: UserDao,
    private val timeEntryDao: TimeEntryDao,
    private val timesheetDao: TimesheetDao,
    private val companyDao: CompanyDao,
    private val workScheduleDao: WorkScheduleDao,
    private val leaveRequestDao: LeaveRequestDao
) {
    companion object {
        private const val TAG = "AuthRepository"
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val authToken: Flow<String?> = dataStore.data.map { prefs ->
        prefs[Constants.AUTH_TOKEN_KEY]
    }

    val isAuthenticated: Flow<Boolean> = authToken.map { it != null }

    private suspend fun saveToken(token: String) {
        dataStore.edit { prefs ->
            prefs[Constants.AUTH_TOKEN_KEY] = token
        }
    }

    suspend fun clearToken() {
        dataStore.edit { prefs ->
            prefs.remove(Constants.AUTH_TOKEN_KEY)
        }
    }

    suspend fun getToken(): String? {
        return dataStore.data.first()[Constants.AUTH_TOKEN_KEY]
    }

    suspend fun login(email: String, password: String): ApiResponse<User> {
        val result = apiCall {
            authApi.login(LoginRequest(email, password))
        }

        when (result) {
            is ApiResponse.Success -> {
                try {
                    saveToken(result.data.accessToken)
                } catch (e: Exception) {
                    return ApiResponse.Error("Failed to save authentication token: ${e.message}")
                }
                applicationScope.launch {
                    try {
                        updateFcmTokenAfterLogin()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to update FCM token after login", e)
                    }
                }
                return ApiResponse.Success(result.data.user)
            }

            is ApiResponse.Error -> return result
            is ApiResponse.Loading -> return result
        }
    }

    suspend fun sendFcmToken(token: String): ApiResponse<String> {
        if (getToken() == null) {
            return ApiResponse.Error("User not authenticated")
        }

        return apiCall {
            val response = authApi.sendFcmToken(FcmTokenRequest(token))
            response.message
        }
    }

    private suspend fun updateFcmTokenAfterLogin() {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            sendFcmToken(token)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update FCM token after login", e)
        }
    }

    suspend fun register(
        name: String,
        email: String,
        password: String
    ): ApiResponse<RegisterResponse> {
        return apiCall {
            authApi.register(RegisterRequest(name, email, password))
        }
    }

    suspend fun verifyEmail(email: String, code: String): ApiResponse<String> {
        return apiCall {
            val response = authApi.verifyEmail(VerifyEmailRequest(email, code))
            response.message
        }
    }

    suspend fun resendVerificationCode(email: String): ApiResponse<String> {
        return apiCall {
            val response = authApi.resendVerificationCode(ResendVerificationRequest(email))
            response.message
        }
    }

    private suspend fun clearAllFeatureCaches() {
        userDao.clearCache()
        timeEntryDao.clearCache()
        timesheetDao.clearCache()
        companyDao.clearCache()
        workScheduleDao.clearCache()
        leaveRequestDao.clearCache()
    }

    suspend fun logout(): ApiResponse<String> {
        clearToken()
        clearAllFeatureCaches()

        @Suppress("UNUSED_EXPRESSION")
        apiCall {
            authApi.logout()
        }

        return ApiResponse.Success("Logged out successfully")
    }
}