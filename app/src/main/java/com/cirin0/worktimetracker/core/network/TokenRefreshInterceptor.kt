package com.cirin0.worktimetracker.core.network

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
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
    @param:Named("active_domain") private val domain: String,
    @param:Named("no_auth_client") private val noAuthClient: OkHttpClient
) : Interceptor {
    private class RefreshRetriedTag

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }

    private val refreshMutex = Mutex()
    private val gson = Gson()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (!shouldAttemptRefresh(response, request.url.encodedPath)) {
            return response
        }

        if (request.tag(RefreshRetriedTag::class.java) != null) {
            return response
        }

        val newToken = runBlocking {
            resolveTokenToUseOrRefresh(extractBearerToken(request))
        }

        if (newToken != null) {
            response.close()
            return chain.proceed(rebuildRequestWithToken(request, newToken))
        }

        return response
    }

    private suspend fun resolveTokenToUseOrRefresh(requestToken: String?): String? {
        return refreshMutex.withLock {
            val latestToken = getStoredToken()

            if (!latestToken.isNullOrBlank() && latestToken != requestToken) {
                latestToken
            } else {
                refreshToken(latestToken ?: requestToken)
            }
        }
    }

    private fun rebuildRequestWithToken(request: Request, token: String): Request {
        return request.newBuilder()
            .header(AUTHORIZATION_HEADER, "$BEARER_PREFIX$token")
            .tag(RefreshRetriedTag::class.java, RefreshRetriedTag())
            .build()
    }

    private fun extractBearerToken(request: Request): String? {
        val authorization = request.header(AUTHORIZATION_HEADER) ?: return null
        if (!authorization.startsWith(BEARER_PREFIX, ignoreCase = true)) return null

        val token = authorization.substringAfter(BEARER_PREFIX).trim()
        return token.ifBlank { null }
    }

    private fun shouldAttemptRefresh(response: Response, path: String): Boolean {
        if (isAuthEndpoint(path)) return false
        return response.code == 401
    }

    private fun isAuthEndpoint(path: String): Boolean {
        return path.contains("/auth/login") ||
                path.contains("/auth/register") ||
                path.contains("/auth/refresh")
    }

    private suspend fun refreshToken(currentToken: String?): String? {
        return try {
            val requestBuilder = Request.Builder()
                .url("$domain/api/${Constants.ApiRoutes.REFRESH}")
                .post("".toRequestBody(JSON_MEDIA_TYPE))
                .header("Accept", "application/json")
                .header("X-Requested-With", "XMLHttpRequest")

            currentToken?.let { requestBuilder.header(AUTHORIZATION_HEADER, "$BEARER_PREFIX$it") }

            val request = requestBuilder.build()

            val response = noAuthClient.newCall(request).execute()

            response.use { refreshResponseHttp ->
                if (refreshResponseHttp.isSuccessful) {
                    val body = refreshResponseHttp.body.string()
                    val refreshResponse = gson.fromJson(body, RefreshResponse::class.java)

                    val accessToken = refreshResponse?.accessToken
                    if (accessToken.isNullOrBlank()) {
                        return@use null
                    }

                    saveToken(accessToken)

                    accessToken
                } else {
                    if (refreshResponseHttp.code == 401 || refreshResponseHttp.code == 403) {
                        clearToken()
                    }
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun getStoredToken(): String? = dataStore.data.first()[Constants.AUTH_TOKEN_KEY]

    private suspend fun saveToken(token: String) {
        dataStore.edit { prefs ->
            prefs[Constants.AUTH_TOKEN_KEY] = token
        }
    }

    private suspend fun clearToken() {
        dataStore.edit { prefs ->
            prefs.remove(Constants.AUTH_TOKEN_KEY)
        }
    }
}
