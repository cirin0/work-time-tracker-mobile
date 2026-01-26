package com.cirin0.worktimetracker.core.network

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cirin0.worktimetracker.core.utils.Constants
import com.cirin0.worktimetracker.features.auth.data.model.RefreshResponse
import com.google.gson.Gson
import jakarta.inject.Named
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRefreshInterceptor @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @param:Named("active_domain") private val domain: String
) : Interceptor {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
    }

    private val refreshMutex = Mutex()
    private val gson = Gson()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code == 401 && !isAuthEndpoint(request.url.encodedPath)) {
            response.close()

            val newToken = runBlocking {
                refreshMutex.withLock {
                    refreshToken()
                }
            }

            if (newToken != null) {
                val newRequest = request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
                return chain.proceed(newRequest)
            }
        }

        return response
    }

    private fun isAuthEndpoint(path: String): Boolean {
        return path.contains("/auth/login") ||
                path.contains("/auth/register") ||
                path.contains("/auth/refresh")
    }

    private suspend fun refreshToken(): String? {
        return try {
            val currentToken = dataStore.data.first()[TOKEN_KEY] ?: return null

            val client = OkHttpClient.Builder().build()

            val request = Request.Builder()
                .url("$domain/api/${Constants.ApiRoutes.REFRESH}")
                .post("".toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", "Bearer $currentToken")
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val body = response.body.string()
                val refreshResponse = gson.fromJson(body, RefreshResponse::class.java)

                dataStore.edit { prefs ->
                    prefs[TOKEN_KEY] = refreshResponse.accessToken
                }

                refreshResponse.accessToken
            } else {
                dataStore.edit { prefs ->
                    prefs.remove(TOKEN_KEY)
                }
                null
            }
        } catch (e: Exception) {
            dataStore.edit { prefs ->
                prefs.remove(TOKEN_KEY)
            }
            null
        }
    }
}
