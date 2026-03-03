package com.cirin0.worktimetracker.features.timeentries.data.repository

import com.cirin0.worktimetracker.core.database.dao.TimeEntryDao
import com.cirin0.worktimetracker.core.database.entity.toCachedEntity
import com.cirin0.worktimetracker.core.database.entity.toTimeEntry
import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.core.network.apiCall
import com.cirin0.worktimetracker.features.timeentries.data.api.TimeEntriesApi
import com.cirin0.worktimetracker.features.timeentries.data.model.PaginatedTimeEntries
import com.cirin0.worktimetracker.features.timeentries.data.model.StopTimeEntryRequest
import com.cirin0.worktimetracker.features.timeentries.data.model.TimeEntry
import com.cirin0.worktimetracker.features.timeentries.data.model.TimeEntryRequest
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class TimeEntriesRepository @Inject constructor(
    private val api: TimeEntriesApi,
    private val timeEntryDao: TimeEntryDao
) {
    suspend fun startTimeEntry(
        startComment: String?,
        latitude: Double?,
        longitude: Double?,
        qrCode: String?
    ): ApiResponse<TimeEntry> {
        return apiCall {
            val response = api.startTimeEntry(
                TimeEntryRequest(
                    startComment = startComment,
                    latitude = latitude,
                    longitude = longitude,
                    qrCode = qrCode
                )
            )
            val entry = response.data ?: throw Exception("Failed to start time entry")
            timeEntryDao.cacheTimeEntry(entry.toCachedEntity())
            entry
        }
    }

    suspend fun getActiveTimeEntry(): ApiResponse<TimeEntry?> {
        return try {
            apiCall {
                val response = api.getActiveTimeEntry()
                response.data?.let { entry ->
                    timeEntryDao.cacheTimeEntry(entry.toCachedEntity())
                    entry
                }
            }
        } catch (e: Exception) {
            val cachedEntry = timeEntryDao.getActiveTimeEntry()
            if (cachedEntry != null) {
                ApiResponse.Success(cachedEntry.toTimeEntry())
            } else {
                ApiResponse.Error(e.message ?: "Unknown error")
            }
        }
    }

    suspend fun stopTimeEntry(stopComment: String?, pinCode: String): ApiResponse<TimeEntry> {
        return apiCall {
            val response = api.stopTimeEntry(StopTimeEntryRequest(stopComment, pinCode))
            val entry = response.data ?: throw Exception("Failed to stop time entry")
            timeEntryDao.cacheTimeEntry(entry.toCachedEntity())
            entry
        }
    }

    suspend fun getTimeEntries(page: Int = 1, perPage: Int = 5): ApiResponse<PaginatedTimeEntries> {
        return try {
            apiCall {
                val response = api.getTimeEntries(page, perPage)
                val entries = response.data
                if (page == 1) {
                    timeEntryDao.cacheTimeEntries(entries.map { it.toCachedEntity() })
                } else {
                    entries.forEach { timeEntryDao.cacheTimeEntry(it.toCachedEntity()) }
                }
                PaginatedTimeEntries(entries, response.meta)
            }
        } catch (e: Exception) {
            if (page == 1) {
                val cachedEntries = timeEntryDao.getAllCachedTimeEntries()
                if (cachedEntries.isNotEmpty()) {
                    ApiResponse.Success(
                        PaginatedTimeEntries(
                            cachedEntries.map { it.toTimeEntry() },
                            null
                        )
                    )
                } else {
                    ApiResponse.Error(e.message ?: "Unknown error")
                }
            } else {
                ApiResponse.Error(e.message ?: "Unknown error")
            }
        }
    }

    suspend fun getTimeEntryById(id: Int): ApiResponse<TimeEntry> {
        return try {
            apiCall {
                val response = api.getTimeEntryById(id)
                val entry = response.data ?: throw Exception("Time entry not found")
                timeEntryDao.cacheTimeEntry(entry.toCachedEntity())
                entry
            }
        } catch (e: Exception) {
            val cachedEntry = timeEntryDao.getCachedTimeEntryById(id)
            if (cachedEntry != null) {
                ApiResponse.Success(cachedEntry.toTimeEntry())
            } else {
                ApiResponse.Error(e.message ?: "Unknown error")
            }
        }
    }

    suspend fun getCachedTimeEntries(): List<TimeEntry> {
        return timeEntryDao.getAllCachedTimeEntries().map { it.toTimeEntry() }
    }
}