package com.cirin0.worktimetracker.features.timesheet.data.repository

import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.core.network.apiCall
import com.cirin0.worktimetracker.features.timeentries.data.api.TimeEntriesApi
import com.cirin0.worktimetracker.features.timesheet.data.model.TimeSummary
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeSheetRepository @Inject constructor(
    private val api: TimeEntriesApi
) {
    suspend fun getTimeSummary(): ApiResponse<TimeSummary> {
        return apiCall {
            api.getTimeSummary().data
        }
    }
}

