package com.cirin0.worktimetracker.features.leaverequests.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.features.leaverequests.data.repository.LeaveRequestsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaveRequestDetailViewModel @Inject constructor(
    private val repository: LeaveRequestsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val requestId: Int = savedStateHandle.get<Int>("requestId") ?: 0

    private val _state = MutableStateFlow(LeaveRequestDetailState())
    val state: StateFlow<LeaveRequestDetailState> = _state.asStateFlow()

    init {
        loadLeaveRequest()
    }

    fun loadLeaveRequest() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val response = repository.getLeaveRequest(requestId)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        request = response.data,
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
}


