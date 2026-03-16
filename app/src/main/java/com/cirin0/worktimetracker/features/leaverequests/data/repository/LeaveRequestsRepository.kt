package com.cirin0.worktimetracker.features.leaverequests.data.repository

import com.cirin0.worktimetracker.core.database.dao.LeaveRequestDao
import com.cirin0.worktimetracker.core.database.entity.toCachedEntity
import com.cirin0.worktimetracker.core.database.entity.toLeaveRequest
import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.core.network.apiCall
import com.cirin0.worktimetracker.core.utils.ConnectivityObserver
import com.cirin0.worktimetracker.features.leaverequests.data.api.LeaveRequestsApi
import com.cirin0.worktimetracker.features.leaverequests.data.model.CreateLeaveRequestRequest
import com.cirin0.worktimetracker.features.leaverequests.data.model.LeaveRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeaveRequestsRepository @Inject constructor(
    private val api: LeaveRequestsApi,
    private val leaveRequestDao: LeaveRequestDao,
    private val connectivityObserver: ConnectivityObserver
) {
    suspend fun getLeaveRequests(): ApiResponse<List<LeaveRequest>> {
        if (!connectivityObserver.isConnected()) {
            val cached = leaveRequestDao.getAll().map { it.toLeaveRequest() }
            return if (cached.isNotEmpty()) {
                ApiResponse.Success(cached, fromCache = true)
            } else {
                ApiResponse.Error("Немає підключення до інтернету")
            }
        }

        val result = apiCall {
            val items = api.getLeaveRequests().data
            leaveRequestDao.insertAll(items.map { it.toCachedEntity() })
            items
        }

        return if (result is ApiResponse.Error) {
            val cached = leaveRequestDao.getAll().map { it.toLeaveRequest() }
            if (cached.isNotEmpty()) {
                ApiResponse.Success(cached, fromCache = true)
            } else {
                result
            }
        } else {
            result
        }
    }

    suspend fun getLeaveRequest(id: Int): ApiResponse<LeaveRequest> {
        if (!connectivityObserver.isConnected()) {
            val cached = leaveRequestDao.getById(id)
            return if (cached != null) {
                ApiResponse.Success(cached.toLeaveRequest(), fromCache = true)
            } else {
                ApiResponse.Error("Немає підключення до інтернету")
            }
        }

        val result = apiCall {
            val item = api.getLeaveRequest(id)
            leaveRequestDao.insert(item.toCachedEntity())
            item
        }

        return if (result is ApiResponse.Error) {
            val cached = leaveRequestDao.getById(id)
            if (cached != null) {
                ApiResponse.Success(cached.toLeaveRequest(), fromCache = true)
            } else {
                result
            }
        } else {
            result
        }
    }

    suspend fun createLeaveRequest(request: CreateLeaveRequestRequest): ApiResponse<LeaveRequest> {
        if (!connectivityObserver.isConnected()) {
            return ApiResponse.Error("Немає підключення до інтернету. Неможливо створити запит.")
        }

        return apiCall {
            val created = api.createLeaveRequest(request).data
            leaveRequestDao.insert(created.toCachedEntity())
            created
        }
    }
}

