package com.cirin0.worktimetracker.core.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.cirin0.worktimetracker.core.utils.DateUtils
import com.cirin0.worktimetracker.features.workschedule.data.model.WorkSchedule
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object WorkNotificationScheduler {
    private const val WORK_NAME = "pre_work_notification"

    fun scheduleNotification(context: Context, workSchedule: WorkSchedule, leadMinutes: Int) {
        val dailySchedules = workSchedule.dailySchedules ?: return
        if (dailySchedules.isEmpty()) return

        val now = LocalDateTime.now()
        var targetDateTime: LocalDateTime? = null
        var targetStartTime: String? = null

        // Look for the next work day in the next 7 days
        for (i in 0..7) {
            val date = LocalDate.now().plusDays(i.toLong())
            val dayOfWeek = DateUtils.normalizeDayOfWeek(date.dayOfWeek.name)

            val schedule = dailySchedules.find {
                DateUtils.normalizeDayOfWeek(it.dayOfWeek) == dayOfWeek && it.isWorkingDay
            }

            if (schedule != null) {
                val startTime = DateUtils.parseWorkTime(schedule.startTime)

                if (startTime != null) {
                    val notificationTime =
                        LocalDateTime.of(date, startTime).minusMinutes(leadMinutes.toLong())

                    if (notificationTime.isAfter(now)) {
                        targetDateTime = notificationTime
                        targetStartTime = schedule.startTime
                        break
                    }
                }
            }
        }

        if (targetDateTime != null && targetStartTime != null) {
            val delay = Duration.between(now, targetDateTime).toMillis()

            val data = Data.Builder()
                .putString(PreWorkNotificationWorker.KEY_START_TIME, targetStartTime)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<PreWorkNotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        } else {
            cancelNotification(context)
        }
    }

    fun cancelNotification(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
