package com.cirin0.worktimetracker.features.timeentries.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.features.timeentries.data.repository.TimeEntriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class TimeEntriesViewModel @Inject constructor(
    private val repository: TimeEntriesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TimeEntriesState())
    val state: StateFlow<TimeEntriesState> = _state.asStateFlow()

    init {
        loadActiveEntry()
        loadTimeEntries()
    }

    fun loadActiveEntry() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = repository.getActiveTimeEntry()) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        activeEntry = result.data,
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

    fun startTimeEntry() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val comment = _state.value.startComment.takeIf { it.isNotBlank() }
            when (val result = repository.startTimeEntry(comment)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        activeEntry = result.data,
                        isLoading = false,
                        startComment = ""
                    )
                    loadTimeEntries()
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

    fun stopTimeEntry() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val comment = _state.value.stopComment.takeIf { it.isNotBlank() }
            when (val result = repository.stopTimeEntry(comment)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        activeEntry = null,
                        isLoading = false,
                        stopComment = ""
                    )
                    loadTimeEntries()
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

    fun updateStartComment(comment: String) {
        _state.value = _state.value.copy(startComment = comment)
    }

    fun updateStopComment(comment: String) {
        _state.value = _state.value.copy(stopComment = comment)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun loadTimeEntries() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingList = true, listError = null)
            when (val result = repository.getTimeEntries()) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        timeEntries = result.data,
                        isLoadingList = false
                    )
                }

                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoadingList = false,
                        listError = result.message
                    )
                }

                is ApiResponse.Loading -> {}
            }
        }
    }
}