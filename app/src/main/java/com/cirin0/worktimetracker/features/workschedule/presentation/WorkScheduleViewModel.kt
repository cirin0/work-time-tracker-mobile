package com.cirin0.worktimetracker.features.workschedule.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cirin0.worktimetracker.R
import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.core.notifications.WorkNotificationScheduler
import com.cirin0.worktimetracker.data.UserPreferencesRepository
import com.cirin0.worktimetracker.features.workschedule.data.repository.WorkScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkScheduleViewModel @Inject constructor(
    private val repository: WorkScheduleRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    @param:ApplicationContext private val context: Context
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
                    val schedule = response.data
                    _state.value = _state.value.copy(
                        isLoading = false,
                        schedule = schedule,
                        error = if (schedule == null) {
                            context.getString(R.string.work_schedule_no_schedule)
                        } else {
                            null
                        }
                    )

                    // Trigger notification scheduling
                    viewModelScope.launch {
                        val enabled = userPreferencesRepository.isPreWorkNotificationEnabled.first()
                        if (enabled) {
                            val minutes =
                                userPreferencesRepository.preWorkNotificationMinutes.first()
                            schedule?.let {
                                WorkNotificationScheduler.scheduleNotification(context, it, minutes)
                            }
                        }
                    }
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


