package com.cirin0.worktimetracker.features.message.data.repository

import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.core.network.apiCall
import com.cirin0.worktimetracker.core.utils.Constants
import com.cirin0.worktimetracker.features.message.data.api.UserApi
import com.cirin0.worktimetracker.features.message.data.model.User
import jakarta.inject.Named
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApi: UserApi,
    @param:Named(Constants.NAMED_IMAGE_URL) private val imageBaseUrl: String
) {
    suspend fun getUsers(): ApiResponse<List<User>> = apiCall {
        val users = userApi.getUsers().data
        users.map { user ->
            user.copy(
                avatar = user.avatar?.let { path ->
                    if (path.startsWith("http")) path
                    else "$imageBaseUrl$path"
                }
            )
        }
    }
}



