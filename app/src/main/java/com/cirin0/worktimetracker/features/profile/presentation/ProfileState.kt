package com.cirin0.worktimetracker.features.profile.presentation

import com.cirin0.worktimetracker.features.profile.data.model.User

data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
