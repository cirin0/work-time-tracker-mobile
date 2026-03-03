package com.cirin0.worktimetracker.features.workschedule.data.api

import com.cirin0.worktimetracker.core.utils.Constants
import com.cirin0.worktimetracker.features.workschedule.data.model.WorkScheduleResponse
import retrofit2.http.GET

interface WorkScheduleApi {
    @GET(Constants.ApiRoutes.ME_WORK_SCHEDULE)
    suspend fun getMyWorkSchedule(): WorkScheduleResponse
}


