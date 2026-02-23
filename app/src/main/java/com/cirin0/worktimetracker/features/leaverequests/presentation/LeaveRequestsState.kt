package com.cirin0.worktimetracker.features.leaverequests.presentation

import com.cirin0.worktimetracker.features.leaverequests.data.model.LeaveRequest
import com.cirin0.worktimetracker.features.leaverequests.data.model.LeaveRequestType

data class LeaveRequestsState(
    val isLoading: Boolean = false,
    val requests: List<LeaveRequest> = emptyList(),
    val error: String? = null,

    val isCreating: Boolean = false,
    val showCreateDialog: Boolean = false,
    val selectedType: LeaveRequestType = LeaveRequestType.VACATION,
    val startDate: String = "",
    val endDate: String = "",
    val reason: String = "",
    val createError: String? = null,
    val createSuccess: Boolean = false
)

