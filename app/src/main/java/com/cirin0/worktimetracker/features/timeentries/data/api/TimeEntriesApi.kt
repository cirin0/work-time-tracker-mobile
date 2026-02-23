package com.cirin0.worktimetracker.features.timeentries.data.api

import com.cirin0.worktimetracker.core.utils.Constants
import com.cirin0.worktimetracker.features.timeentries.data.model.StopTimeEntryRequest
import com.cirin0.worktimetracker.features.timeentries.data.model.TimeEntriesListResponse
import com.cirin0.worktimetracker.features.timeentries.data.model.TimeEntryRequest
import com.cirin0.worktimetracker.features.timeentries.data.model.TimeEntryResponse
import com.cirin0.worktimetracker.features.timesheet.data.model.TimeSummaryResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface TimeEntriesApi {
    @POST(Constants.ApiRoutes.TIME_ENTRIES)
    suspend fun startTimeEntry(@Body request: TimeEntryRequest): TimeEntryResponse

    @GET("${Constants.ApiRoutes.TIME_ENTRIES}/active")
    suspend fun getActiveTimeEntry(): TimeEntryResponse

    @PATCH("${Constants.ApiRoutes.TIME_ENTRIES}/active/stop")
    suspend fun stopTimeEntry(@Body request: StopTimeEntryRequest): TimeEntryResponse

    @GET(Constants.ApiRoutes.TIME_ENTRIES)
    suspend fun getTimeEntries(): TimeEntriesListResponse

    @GET("${Constants.ApiRoutes.TIME_ENTRIES}/{id}")
    suspend fun getTimeEntryById(@Path("id") id: Int): TimeEntryResponse

    @GET("${Constants.ApiRoutes.TIME_ENTRIES}/summary/me")
    suspend fun getTimeSummary(): TimeSummaryResponse
}