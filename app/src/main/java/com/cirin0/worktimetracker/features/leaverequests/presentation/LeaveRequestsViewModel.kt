package com.cirin0.worktimetracker.features.leaverequests.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.features.leaverequests.data.model.CreateLeaveRequestRequest
import com.cirin0.worktimetracker.features.leaverequests.data.model.LeaveRequestType
import com.cirin0.worktimetracker.features.leaverequests.data.repository.LeaveRequestsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaveRequestsViewModel @Inject constructor(
    private val repository: LeaveRequestsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LeaveRequestsState())
    val state: StateFlow<LeaveRequestsState> = _state.asStateFlow()

    init {
        loadLeaveRequests()
    }

    fun loadLeaveRequests() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val response = repository.getLeaveRequests()) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        requests = response.data,
                        error = null
                    )
                }

                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = response.message
                    )
                }

                is ApiResponse.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
            }
        }
    }

    fun showCreateDialog() {
        _state.value = _state.value.copy(
            showCreateDialog = true,
            selectedType = LeaveRequestType.VACATION,
            startDate = "",
            endDate = "",
            reason = "",
            createError = null,
            createSuccess = false
        )
    }

    fun hideCreateDialog() {
        _state.value = _state.value.copy(showCreateDialog = false)
    }

    fun updateSelectedType(type: LeaveRequestType) {
        _state.value = _state.value.copy(selectedType = type)
    }

    fun updateStartDate(date: String) {
        _state.value = _state.value.copy(startDate = date)
    }

    fun updateEndDate(date: String) {
        _state.value = _state.value.copy(endDate = date)
    }

    fun updateReason(reason: String) {
        _state.value = _state.value.copy(reason = reason)
    }

    fun createLeaveRequest() {
        val currentState = _state.value

        if (currentState.startDate.isEmpty() || currentState.endDate.isEmpty() || currentState.reason.isEmpty()) {
            _state.value = _state.value.copy(createError = "Будь ласка, заповніть всі поля")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isCreating = true, createError = null)

            val request = CreateLeaveRequestRequest(
                type = currentState.selectedType.value,
                startDate = currentState.startDate,
                endDate = currentState.endDate,
                reason = currentState.reason
            )

            when (val response = repository.createLeaveRequest(request)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        isCreating = false,
                        createSuccess = true,
                        showCreateDialog = false
                    )
                    loadLeaveRequests()
                }

                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isCreating = false,
                        createError = response.message
                    )
                }

                is ApiResponse.Loading -> {
                    _state.value = _state.value.copy(isCreating = true)
                }
            }
        }
    }

    fun clearCreateSuccess() {
        _state.value = _state.value.copy(createSuccess = false)
    }
}

