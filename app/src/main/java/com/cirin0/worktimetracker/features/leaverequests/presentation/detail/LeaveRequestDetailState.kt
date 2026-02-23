package com.cirin0.worktimetracker.features.leaverequests.presentation.detail

import com.cirin0.worktimetracker.features.leaverequests.data.model.LeaveRequest

data class LeaveRequestDetailState(
    val isLoading: Boolean = false,
    val request: LeaveRequest? = null,
    val error: String? = null
)

