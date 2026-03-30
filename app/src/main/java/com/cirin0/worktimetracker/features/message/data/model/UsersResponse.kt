package com.cirin0.worktimetracker.features.message.data.model

import com.google.gson.annotations.SerializedName

data class UsersResponse(
    val data: List<User>,
    val meta: UsersMeta? = null
)

data class UsersMeta(
    @SerializedName("current_page")
    val currentPage: Int,
    @SerializedName("last_page")
    val lastPage: Int
)

data class PaginatedUsers(
    val users: List<User>,
    val meta: UsersMeta?
)

