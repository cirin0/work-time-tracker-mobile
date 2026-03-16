package com.cirin0.worktimetracker.features.profile.data.repository

import android.content.Context
import com.cirin0.worktimetracker.R
import com.cirin0.worktimetracker.core.database.dao.UserDao
import com.cirin0.worktimetracker.core.database.entity.toCachedEntity
import com.cirin0.worktimetracker.core.database.entity.toUser
import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.core.network.apiCall
import com.cirin0.worktimetracker.core.utils.ConnectivityObserver
import com.cirin0.worktimetracker.core.utils.Constants
import com.cirin0.worktimetracker.features.profile.data.api.ProfileApi
import com.cirin0.worktimetracker.features.profile.data.model.MessageResponse
import com.cirin0.worktimetracker.features.profile.data.model.SetupPinCodeRequest
import com.cirin0.worktimetracker.features.profile.data.model.UpdatePinCodeRequest
import com.cirin0.worktimetracker.features.profile.data.model.UpdateProfileRequest
import com.cirin0.worktimetracker.features.profile.data.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Named
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ProfileRepository @Inject constructor(
    private val profileApi: ProfileApi,
    private val userDao: UserDao,
    private val connectivityObserver: ConnectivityObserver,
    @param:Named(Constants.NAMED_IMAGE_URL) private val imageBaseUrl: String,
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private const val CACHE_TTL = 24 * 60 * 60 * 1000L
    }

    suspend fun getCurrentUser(): ApiResponse<User> {
        val cachedUser = userDao.getCachedUser()
        val isCacheValid =
            cachedUser != null && (System.currentTimeMillis() - cachedUser.cachedAt) < CACHE_TTL

        if (!connectivityObserver.isConnected()) {
            return if (cachedUser != null) {
                if (isCacheValid) {
                    ApiResponse.Success(cachedUser.toUser(), fromCache = true)
                } else {
                    ApiResponse.Error(context.getString(R.string.profile_cache_expired_need_internet))
                }
            } else {
                ApiResponse.Error(context.getString(R.string.profile_offline_no_cache))
            }
        }

        val response = apiCall {
            val user = profileApi.getCurrentUser()
            val userWithAvatar = user.copy(
                avatar = user.avatar?.let { path ->
                    if (path.startsWith("http")) path
                    else "$imageBaseUrl$path"
                }
            )
            userDao.cacheUser(userWithAvatar.toCachedEntity())
            userWithAvatar
        }

        return if (response is ApiResponse.Error) {
            if (isCacheValid) {
                ApiResponse.Success(cachedUser.toUser(), fromCache = true)
            } else {
                response
            }
        } else {
            response
        }
    }

    suspend fun updateProfile(name: String, email: String): ApiResponse<User> {
        if (!connectivityObserver.isConnected()) {
            return ApiResponse.Error(context.getString(R.string.profile_offline_update_error))
        }

        return apiCall {
            val response = profileApi.updateProfile(UpdateProfileRequest(name, email))
            val user = response.user
            val userWithAvatar = user.copy(
                avatar = user.avatar?.let { path ->
                    if (path.startsWith("http")) path
                    else "$imageBaseUrl$path"
                }
            )
            userDao.cacheUser(userWithAvatar.toCachedEntity())
            userWithAvatar
        }
    }

    suspend fun updateAvatar(imageFile: File): ApiResponse<User> {
        if (!connectivityObserver.isConnected()) {
            return ApiResponse.Error(context.getString(R.string.profile_offline_avatar_error))
        }

        return apiCall {
            val requestBody = imageFile.asRequestBody("image/*".toMediaType())
            val part = MultipartBody.Part.createFormData("avatar", imageFile.name, requestBody)
            val user = profileApi.updateAvatar(part)
            val userWithAvatar = user.copy(
                avatar = user.avatar?.let { path ->
                    if (path.startsWith("http")) path
                    else "$imageBaseUrl$path"
                }
            )
            userDao.cacheUser(userWithAvatar.toCachedEntity())
            userWithAvatar
        }
    }

    suspend fun clearCache() {
        userDao.clearCache()
    }

    suspend fun setupPinCode(pinCode: String): ApiResponse<MessageResponse> {
        if (!connectivityObserver.isConnected()) {
            return ApiResponse.Error(context.getString(R.string.profile_offline_pin_setup_error))
        }

        return apiCall {
            profileApi.setupPinCode(SetupPinCodeRequest(pinCode))
        }
    }

    suspend fun updatePinCode(currentPin: String, newPin: String): ApiResponse<MessageResponse> {
        if (!connectivityObserver.isConnected()) {
            return ApiResponse.Error(context.getString(R.string.profile_offline_pin_update_error))
        }

        return apiCall {
            profileApi.updatePinCode(
                UpdatePinCodeRequest(
                    currentPin,
                    newPin
                )
            )
        }
    }
}
