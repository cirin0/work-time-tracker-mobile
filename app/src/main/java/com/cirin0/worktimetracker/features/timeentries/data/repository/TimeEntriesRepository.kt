package com.cirin0.worktimetracker.features.timeentries.data.repository

import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.core.network.apiCall
import com.cirin0.worktimetracker.features.timeentries.data.api.TimeEntriesApi
import com.cirin0.worktimetracker.features.timeentries.data.model.StopTimeEntryRequest
import com.cirin0.worktimetracker.features.timeentries.data.model.TimeEntry
import com.cirin0.worktimetracker.features.timeentries.data.model.TimeEntryRequest
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class TimeEntriesRepository @Inject constructor(
    private val api: TimeEntriesApi
) {
    suspend fun startTimeEntry(
        startComment: String?,
        latitude: Double?,
        longitude: Double?
    ): ApiResponse<TimeEntry> {
        return apiCall {
            val response = api.startTimeEntry(
                TimeEntryRequest(
                    startComment = startComment,
                    latitude = latitude,
                    longitude = longitude
                )
            )
            response.data ?: throw Exception("Failed to start time entry")
        }
    }

    suspend fun getActiveTimeEntry(): ApiResponse<TimeEntry?> {
        return apiCall {
            val response = api.getActiveTimeEntry()
            response.data
        }
    }

    suspend fun stopTimeEntry(stopComment: String?, pinCode: String): ApiResponse<TimeEntry> {
        return apiCall {
            val response = api.stopTimeEntry(StopTimeEntryRequest(stopComment, pinCode))
            response.data ?: throw Exception("Failed to stop time entry")
        }
    }

    suspend fun getTimeEntries(): ApiResponse<List<TimeEntry>> {
        return apiCall {
            val response = api.getTimeEntries()
            response.data
        }
    }

    suspend fun getTimeEntryById(id: Int): ApiResponse<TimeEntry> {
        return apiCall {
            val response = api.getTimeEntryById(id)
            response.data ?: throw Exception("Time entry not found")
        }
    }
}