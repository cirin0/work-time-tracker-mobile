package com.cirin0.worktimetracker.features.workschedule.data.repository

import android.content.Context
import com.cirin0.worktimetracker.R
import com.cirin0.worktimetracker.core.database.dao.WorkScheduleDao
import com.cirin0.worktimetracker.core.database.entity.toCachedEntity
import com.cirin0.worktimetracker.core.database.entity.toWorkSchedule
import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.core.network.apiCall
import com.cirin0.worktimetracker.core.utils.ConnectivityObserver
import com.cirin0.worktimetracker.features.workschedule.data.api.WorkScheduleApi
import com.cirin0.worktimetracker.features.workschedule.data.model.WorkSchedule
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkScheduleRepository @Inject constructor(
    private val api: WorkScheduleApi,
    private val workScheduleDao: WorkScheduleDao,
    private val connectivityObserver: ConnectivityObserver,
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private const val CACHE_RETENTION_DAYS = 120L
    }

    suspend fun getMyWorkSchedule(): ApiResponse<WorkSchedule?> {
        val now = LocalDate.now()
        val year = now.year
        val month = now.monthValue

        if (!connectivityObserver.isConnected()) {
            val cached = workScheduleDao.getByMonth(year, month)
            return if (cached != null) {
                ApiResponse.Success(cached.toWorkSchedule(), fromCache = true)
            } else {
                ApiResponse.Error(context.getString(R.string.general_no_internet))
            }
        }

        val result = apiCall {
            val schedule = api.getMyWorkSchedule().data
            if (schedule != null) {
                workScheduleDao.insert(schedule.toCachedEntity(year, month))
                val minCachedAt =
                    System.currentTimeMillis() - (CACHE_RETENTION_DAYS * 24 * 60 * 60 * 1000)
                workScheduleDao.clearOld(minCachedAt)
            }
            schedule
        }

        return if (result is ApiResponse.Error) {
            val cached = workScheduleDao.getByMonth(year, month)
            if (cached != null) {
                ApiResponse.Success(cached.toWorkSchedule(), fromCache = true)
            } else {
                result
            }
        } else {
            result
        }
    }
}


