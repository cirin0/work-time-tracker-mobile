package com.cirin0.worktimetracker.features.manager.data.api

import com.cirin0.worktimetracker.features.manager.data.model.ActiveEmployeesResponse
import com.cirin0.worktimetracker.features.manager.data.model.LeaveRequestActionRequest
import com.cirin0.worktimetracker.features.manager.data.model.LeaveRequestActionResponse
import com.cirin0.worktimetracker.features.manager.data.model.PendingLeaveRequestsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ManagerApi {
    @GET("managers/leave-requests/pending")
    suspend fun getPendingLeaveRequests(): Response<PendingLeaveRequestsResponse>

    @GET("managers/time-entries/active")
    suspend fun getActiveEmployees(): Response<ActiveEmployeesResponse>

    @POST("managers/leave-requests/{id}/approve")
    suspend fun approveLeaveRequest(
        @Path("id") requestId: Int,
        @Body request: LeaveRequestActionRequest
    ): Response<LeaveRequestActionResponse>

    @POST("managers/leave-requests/{id}/reject")
    suspend fun rejectLeaveRequest(
        @Path("id") requestId: Int,
        @Body request: LeaveRequestActionRequest
    ): Response<LeaveRequestActionResponse>
}
