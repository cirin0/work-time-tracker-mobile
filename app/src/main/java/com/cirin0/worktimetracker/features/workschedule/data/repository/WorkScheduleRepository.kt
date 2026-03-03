package com.cirin0.worktimetracker.features.workschedule.data.repository

import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.core.network.apiCall
import com.cirin0.worktimetracker.features.workschedule.data.api.WorkScheduleApi
import com.cirin0.worktimetracker.features.workschedule.data.model.WorkSchedule
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkScheduleRepository @Inject constructor(
    private val api: WorkScheduleApi
) {
    suspend fun getMyWorkSchedule(): ApiResponse<WorkSchedule?> = apiCall {
        api.getMyWorkSchedule().data
    }
}


