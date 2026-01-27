package com.cirin0.worktimetracker.features.timeentries.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.features.timeentries.data.model.TimeEntry
import com.cirin0.worktimetracker.features.timeentries.data.repository.TimeEntriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TimeEntryDetailState(
    val timeEntry: TimeEntry? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TimeEntryDetailViewModel @Inject constructor(
    private val repository: TimeEntriesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TimeEntryDetailState())
    val state: StateFlow<TimeEntryDetailState> = _state.asStateFlow()

    fun loadTimeEntry(id: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = repository.getTimeEntryById(id)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        timeEntry = result.data,
                        isLoading = false
                    )
                }

                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }

                is ApiResponse.Loading -> {}
            }
        }
    }
}
