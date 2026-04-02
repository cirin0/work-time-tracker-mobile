package com.cirin0.worktimetracker.features.manager.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.features.manager.data.repository.ManagerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManagerViewModel @Inject constructor(
    private val repository: ManagerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ManagerState())
    val state: StateFlow<ManagerState> = _state.asStateFlow()

    init {
        loadLeaveRequests()
        loadActiveEmployees()
    }

    fun loadLeaveRequests() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingLeaveRequests = true, leaveRequestsError = null) }
            when (val result = repository.getPendingLeaveRequests()) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            leaveRequests = result.data,
                            isLoadingLeaveRequests = false
                        )
                    }
                }

                is ApiResponse.Error -> {
                    _state.update {
                        it.copy(
                            leaveRequestsError = result.message,
                            isLoadingLeaveRequests = false
                        )
                    }
                }

                is ApiResponse.Loading -> {
                    // Already handled by isLoadingLeaveRequests
                }
            }
        }
    }

    fun loadActiveEmployees() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingActiveEmployees = true, activeEmployeesError = null) }
            when (val result = repository.getActiveEmployees()) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            activeEmployees = result.data,
                            isLoadingActiveEmployees = false
                        )
                    }
                }

                is ApiResponse.Error -> {
                    _state.update {
                        it.copy(
                            activeEmployeesError = result.message,
                            isLoadingActiveEmployees = false
                        )
                    }
                }

                is ApiResponse.Loading -> {
                    // Already handled by isLoadingActiveEmployees
                }
            }
        }
    }

    fun approveLeaveRequest(requestId: Int, reason: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(processingRequestId = requestId) }
            when (val result = repository.approveLeaveRequest(requestId, reason)) {
                is ApiResponse.Success -> {
                    _state.update { it.copy(processingRequestId = null, actionSuccess = true) }
                    loadLeaveRequests()
                }

                is ApiResponse.Error -> {
                    _state.update { it.copy(processingRequestId = null) }
                }

                is ApiResponse.Loading -> {
                    // Already handled by processingRequestId
                }
            }
        }
    }

    fun rejectLeaveRequest(requestId: Int, reason: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(processingRequestId = requestId) }
            when (val result = repository.rejectLeaveRequest(requestId, reason)) {
                is ApiResponse.Success -> {
                    _state.update { it.copy(processingRequestId = null, actionSuccess = true) }
                    loadLeaveRequests()
                }

                is ApiResponse.Error -> {
                    _state.update { it.copy(processingRequestId = null) }
                }

                is ApiResponse.Loading -> {
                    // Already handled by processingRequestId
                }
            }
        }
    }

    fun clearActionSuccess() {
        _state.update { it.copy(actionSuccess = false) }
    }

    fun showApproveDialog(requestId: Int) {
        _state.update {
            it.copy(
                showCommentDialog = true,
                commentDialogType = CommentDialogType.APPROVE,
                selectedRequestId = requestId,
                comment = "",
                commentError = null
            )
        }
    }

    fun showRejectDialog(requestId: Int) {
        _state.update {
            it.copy(
                showCommentDialog = true,
                commentDialogType = CommentDialogType.REJECT,
                selectedRequestId = requestId,
                comment = "",
                commentError = null
            )
        }
    }

    fun closeCommentDialog() {
        _state.update {
            it.copy(
                showCommentDialog = false,
                commentDialogType = null,
                selectedRequestId = null,
                comment = "",
                commentError = null
            )
        }
    }

    fun onCommentChange(comment: String) {
        _state.update { it.copy(comment = comment, commentError = null) }
    }

    fun submitComment() {
        val currentState = _state.value

        if (currentState.commentDialogType == CommentDialogType.REJECT && currentState.comment.isBlank()) {
            _state.update { it.copy(commentError = "Comment is required for rejection") }
            return
        }

        val requestId = currentState.selectedRequestId ?: return
        val comment = currentState.comment.ifBlank { null }

        closeCommentDialog()

        when (currentState.commentDialogType) {
            CommentDialogType.APPROVE -> approveLeaveRequest(requestId, comment)
            CommentDialogType.REJECT -> rejectLeaveRequest(requestId, comment)
            null -> {}
        }
    }
}
