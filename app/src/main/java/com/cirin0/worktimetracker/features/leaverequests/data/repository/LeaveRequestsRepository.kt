package com.cirin0.worktimetracker.features.leaverequests.data.repository

import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.core.network.apiCall
import com.cirin0.worktimetracker.features.leaverequests.data.api.LeaveRequestsApi
import com.cirin0.worktimetracker.features.leaverequests.data.model.CreateLeaveRequestRequest
import com.cirin0.worktimetracker.features.leaverequests.data.model.LeaveRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeaveRequestsRepository @Inject constructor(
    private val api: LeaveRequestsApi
) {
    suspend fun getLeaveRequests(): ApiResponse<List<LeaveRequest>> = apiCall {
        api.getLeaveRequests().data
    }

    suspend fun getLeaveRequest(id: Int): ApiResponse<LeaveRequest> = apiCall {
        api.getLeaveRequest(id)
    }

    suspend fun createLeaveRequest(request: CreateLeaveRequestRequest): ApiResponse<LeaveRequest> =
        apiCall {
            api.createLeaveRequest(request).data
        }
}

