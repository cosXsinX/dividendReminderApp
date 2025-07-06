package com.example.mydividendreminder.service

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class DividendNotificationScheduler(private val context: Context) {

    companion object {
        private const val WORK_NAME = "dividend_notification_work"
        private const val WORK_TAG = "dividend_reminder"
    }

    fun scheduleDailyNotificationCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()

        val dailyWorkRequest = PeriodicWorkRequestBuilder<DividendNotificationService>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .addTag(WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyWorkRequest
        )
    }

    fun cancelScheduledNotifications() {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
    }

    fun isNotificationScheduled(): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosByTag(WORK_TAG)
            .get()
        
        return workInfos.any { workInfo ->
            workInfo.state == WorkInfo.State.ENQUEUED || 
            workInfo.state == WorkInfo.State.RUNNING
        }
    }
} 