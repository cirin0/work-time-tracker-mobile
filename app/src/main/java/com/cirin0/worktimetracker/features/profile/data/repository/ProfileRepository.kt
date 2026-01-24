package com.cirin0.worktimetracker.features.profile.data.repository

import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.core.network.apiCall
import com.cirin0.worktimetracker.core.utils.Constants
import com.cirin0.worktimetracker.features.profile.data.api.ProfileApi
import com.cirin0.worktimetracker.features.profile.data.model.UpdateProfileRequest
import com.cirin0.worktimetracker.features.profile.data.model.User
import jakarta.inject.Inject
import jakarta.inject.Named
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ProfileRepository @Inject constructor(
    private val profileApi: ProfileApi,
    @param:Named(Constants.NAMED_IMAGE_URL) private val imageBaseUrl: String
) {
    suspend fun getCurrentUser(): ApiResponse<User> {
        return apiCall {
            val user = profileApi.getCurrentUser()
            user.copy(
                avatar = user.avatar?.let { path ->
                    if (path.startsWith("http")) path
                    else "$imageBaseUrl$path"
                }
            )
        }
    }

    suspend fun updateProfile(name: String, email: String): ApiResponse<User> {
        return apiCall {
            val response = profileApi.updateProfile(UpdateProfileRequest(name, email))
            val user = response.user
            user.copy(
                avatar = user.avatar?.let { path ->
                    if (path.startsWith("http")) path
                    else "$imageBaseUrl$path"
                }
            )
        }
    }

    suspend fun updateAvatar(imageFile: File): ApiResponse<User> {
        return apiCall {
            val requestBody = imageFile.asRequestBody("image/*".toMediaType())
            val part = MultipartBody.Part.createFormData("avatar", imageFile.name, requestBody)
            val user = profileApi.updateAvatar(part)
            user.copy(
                avatar = user.avatar?.let { path ->
                    if (path.startsWith("http")) path
                    else "$imageBaseUrl$path"
                }
            )
        }
    }
}