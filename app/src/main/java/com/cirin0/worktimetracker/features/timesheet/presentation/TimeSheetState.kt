package com.cirin0.worktimetracker.features.timesheet.presentation

import com.cirin0.worktimetracker.features.timesheet.data.model.TimeSummary

data class TimeSheetState(
    val isLoading: Boolean = false,
    val summary: TimeSummary? = null,
    val error: String? = null
)

