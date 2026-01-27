package com.cirin0.worktimetracker.features.timeentries.presentation

import com.cirin0.worktimetracker.features.timeentries.data.model.TimeEntry

data class TimeEntriesState(
    val activeEntry: TimeEntry? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val startComment: String = "",
    val stopComment: String = "",
    val timeEntries: List<TimeEntry> = emptyList(),
    val isLoadingList: Boolean = false,
    val listError: String? = null
)