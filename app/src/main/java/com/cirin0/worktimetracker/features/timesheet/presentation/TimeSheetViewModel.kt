package com.cirin0.worktimetracker.features.timesheet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.features.timesheet.data.repository.TimeSheetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimeSheetViewModel @Inject constructor(
    private val repository: TimeSheetRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TimeSheetState())
    val state: StateFlow<TimeSheetState> = _state.asStateFlow()

    init {
        loadTimeSummary()
    }

    fun loadTimeSummary() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val response = repository.getTimeSummary()) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        summary = response.data,
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

