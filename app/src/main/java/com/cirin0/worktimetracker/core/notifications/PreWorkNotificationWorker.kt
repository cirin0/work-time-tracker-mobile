package com.cirin0.worktimetracker.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cirin0.worktimetracker.R

class PreWorkNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "work_notifications"
        const val CHANNEL_NAME = "Work Notifications"
        const val NOTIFICATION_ID = 1001

        const val KEY_START_TIME = "start_time"
    }

    override suspend fun doWork(): Result {
        val startTime = inputData.getString(KEY_START_TIME)
            ?: return Result.failure()

        showNotification(startTime)
        return Result.success()
    }

    private fun showNotification(startTime: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val intent =
            applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = applicationContext.getString(R.string.settings_notification_before_work)
        val formattedTime = startTime.substring(0, minOf(5, startTime.length))
        val body =
            applicationContext.getString(R.string.settings_notification_before_work_desc) + ": $formattedTime"

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
