package com.cirin0.worktimetracker.features.timesheet.data.repository

import android.content.Context
import com.cirin0.worktimetracker.R
import com.cirin0.worktimetracker.core.database.dao.TimesheetDao
import com.cirin0.worktimetracker.core.database.entity.toCachedEntity
import com.cirin0.worktimetracker.core.database.entity.toTimeSummary
import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.core.network.apiCall
import com.cirin0.worktimetracker.core.utils.ConnectivityObserver
import com.cirin0.worktimetracker.features.timeentries.data.api.TimeEntriesApi
import com.cirin0.worktimetracker.features.timesheet.data.model.TimeSummary
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeSheetRepository @Inject constructor(
    private val api: TimeEntriesApi,
    private val timesheetDao: TimesheetDao,
    private val connectivityObserver: ConnectivityObserver,
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private const val CACHE_RETENTION_DAYS = 120L
    }

    suspend fun getTimeSummary(): ApiResponse<TimeSummary> {
        val now = LocalDate.now()
        val year = now.year
        val month = now.monthValue

        if (!connectivityObserver.isConnected()) {
            val cached = timesheetDao.getByMonth(year, month)
            return if (cached != null) {
                ApiResponse.Success(cached.toTimeSummary(), fromCache = true)
            } else {
                ApiResponse.Error(context.getString(R.string.general_no_internet))
            }
        }

        val result = apiCall {
            val summary = api.getTimeSummary().data
            timesheetDao.insertAll(listOf(summary.toCachedEntity(year, month)))
            val minCachedAt =
                System.currentTimeMillis() - (CACHE_RETENTION_DAYS * 24 * 60 * 60 * 1000)
            timesheetDao.clearOld(minCachedAt)
            summary
        }

        return if (result is ApiResponse.Error) {
            val cached = timesheetDao.getByMonth(year, month)
            if (cached != null) {
                ApiResponse.Success(cached.toTimeSummary(), fromCache = true)
            } else {
                result
            }
        } else {
            result
        }
    }
}

