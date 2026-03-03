package com.cirin0.worktimetracker.features.workschedule.presentation

import com.cirin0.worktimetracker.features.workschedule.data.model.WorkSchedule

data class WorkScheduleState(
    val isLoading: Boolean = false,
    val schedule: WorkSchedule? = null,
    val error: String? = null
)


