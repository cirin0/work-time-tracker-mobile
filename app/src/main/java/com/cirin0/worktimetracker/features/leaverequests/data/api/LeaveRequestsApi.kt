package com.cirin0.worktimetracker.features.leaverequests.data.api

import com.cirin0.worktimetracker.core.utils.Constants
import com.cirin0.worktimetracker.features.leaverequests.data.model.CreateLeaveRequestRequest
import com.cirin0.worktimetracker.features.leaverequests.data.model.CreateLeaveRequestResponse
import com.cirin0.worktimetracker.features.leaverequests.data.model.LeaveRequest
import com.cirin0.worktimetracker.features.leaverequests.data.model.LeaveRequestsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface LeaveRequestsApi {
    @GET(Constants.ApiRoutes.LEAVE_REQUESTS)
    suspend fun getLeaveRequests(): LeaveRequestsResponse

    @GET("${Constants.ApiRoutes.LEAVE_REQUESTS}/{id}")
    suspend fun getLeaveRequest(@Path("id") id: Int): LeaveRequest

    @POST(Constants.ApiRoutes.LEAVE_REQUESTS)
    suspend fun createLeaveRequest(@Body request: CreateLeaveRequestRequest): CreateLeaveRequestResponse
}

