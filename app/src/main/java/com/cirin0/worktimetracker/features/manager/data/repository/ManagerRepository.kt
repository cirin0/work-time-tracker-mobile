package com.cirin0.worktimetracker.features.manager.data.repository

import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.features.manager.data.api.ManagerApi
import com.cirin0.worktimetracker.features.manager.data.model.ActiveEmployee
import com.cirin0.worktimetracker.features.manager.data.model.LeaveRequestActionRequest
import com.cirin0.worktimetracker.features.manager.data.model.PendingLeaveRequest
import javax.inject.Inject

class ManagerRepository @Inject constructor(
    private val api: ManagerApi
) {
    suspend fun getPendingLeaveRequests(): ApiResponse<List<PendingLeaveRequest>> {
        return try {
            val response = api.getPendingLeaveRequests()
            if (response.isSuccessful && response.body() != null) {
                ApiResponse.Success(response.body()!!.data)
            } else {
                ApiResponse.Error(response.message() ?: "Unknown error")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Network error")
        }
    }

    suspend fun getActiveEmployees(): ApiResponse<List<ActiveEmployee>> {
        return try {
            val response = api.getActiveEmployees()
            if (response.isSuccessful && response.body() != null) {
                ApiResponse.Success(response.body()!!.data)
            } else {
                ApiResponse.Error(response.message() ?: "Unknown error")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Network error")
        }
    }

    suspend fun approveLeaveRequest(
        requestId: Int,
        reason: String? = null
    ): ApiResponse<PendingLeaveRequest> {
        return try {
            val response = api.approveLeaveRequest(
                requestId,
                LeaveRequestActionRequest(managerComment = reason)
            )
            if (response.isSuccessful && response.body() != null) {
                ApiResponse.Success(response.body()!!.data)
            } else {
                ApiResponse.Error(response.message() ?: "Unknown error")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Network error")
        }
    }

    suspend fun rejectLeaveRequest(
        requestId: Int,
        reason: String? = null
    ): ApiResponse<PendingLeaveRequest> {
        return try {
            val response = api.rejectLeaveRequest(
                requestId,
                LeaveRequestActionRequest(managerComment = reason)
            )
            if (response.isSuccessful && response.body() != null) {
                ApiResponse.Success(response.body()!!.data)
            } else {
                ApiResponse.Error(response.message() ?: "Unknown error")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Network error")
        }
    }
}
