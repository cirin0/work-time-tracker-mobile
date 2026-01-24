package com.cirin0.worktimetracker.features.profile.data.repository

import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.core.network.apiCall
import com.cirin0.worktimetracker.core.utils.Constants
import com.cirin0.worktimetracker.features.profile.data.api.ProfileApi
import com.cirin0.worktimetracker.features.profile.data.model.User
import jakarta.inject.Inject
import jakarta.inject.Named

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
}