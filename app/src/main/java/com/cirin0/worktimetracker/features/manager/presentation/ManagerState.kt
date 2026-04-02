package com.cirin0.worktimetracker.features.manager.presentation

import com.cirin0.worktimetracker.features.manager.data.model.ActiveEmployee
import com.cirin0.worktimetracker.features.manager.data.model.PendingLeaveRequest

data class ManagerState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val leaveRequests: List<PendingLeaveRequest> = emptyList(),
    val activeEmployees: List<ActiveEmployee> = emptyList(),
    val isLoadingLeaveRequests: Boolean = false,
    val isLoadingActiveEmployees: Boolean = false,
    val leaveRequestsError: String? = null,
    val activeEmployeesError: String? = null,
    val processingRequestId: Int? = null,
    val actionSuccess: Boolean = false,
    val showCommentDialog: Boolean = false,
    val commentDialogType: CommentDialogType? = null,
    val selectedRequestId: Int? = null,
    val comment: String = "",
    val commentError: String? = null
)

enum class CommentDialogType {
    APPROVE, REJECT
}
