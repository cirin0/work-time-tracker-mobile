package com.cirin0.worktimetracker.features.workschedule.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.features.workschedule.data.repository.WorkScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkScheduleViewModel @Inject constructor(
    private val repository: WorkScheduleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WorkScheduleState())
    val state: StateFlow<WorkScheduleState> = _state.asStateFlow()

    init {
        loadSchedules()
    }

    fun loadSchedules() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val response = repository.getMyWorkSchedule()) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        schedule = response.data,
                        error = if (response.data == null) "Графік роботи не призначено" else null
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


