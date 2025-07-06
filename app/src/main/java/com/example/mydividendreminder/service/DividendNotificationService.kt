package com.example.mydividendreminder.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mydividendreminder.MainActivity
import com.example.mydividendreminder.R
import com.example.mydividendreminder.data.database.AppDatabase
import com.example.mydividendreminder.data.repository.DividendRepository
import com.example.mydividendreminder.data.entity.Dividend
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class DividendNotificationService(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "dividend_reminder_channel"
        const val CHANNEL_NAME = "Dividend Reminders"
        const val CHANNEL_DESCRIPTION = "Notifications for upcoming dividend payments"
        const val NOTIFICATION_ID = 1
    }

    override suspend fun doWork(): Result {
        try {
            val database = AppDatabase.getDatabase(context)
            val repository = DividendRepository(database.dividendDao())
            val startDate = LocalDate.now()
            val endDate = startDate.plusDays(7) // Check for next 7 days
            val upcomingDividends = repository.getUpcomingDividends(startDate, endDate)

            if (upcomingDividends.isNotEmpty()) {
                createNotificationChannel()
                sendNotification(upcomingDividends)
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(upcomingDividends: List<Dividend>) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (upcomingDividends.size == 1) {
            "Dividend Reminder"
        } else {
            "${upcomingDividends.size} Dividend Reminders"
        }

        val content = buildNotificationContent(upcomingDividends)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotificationContent(dividends: List<Dividend>): String {
        return if (dividends.size == 1) {
            val dividend = dividends.first()
            val daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), dividend.dividendDate)
            "Dividend of $${dividend.dividendAmount} in $daysUntil days"
        } else {
            val totalAmount = dividends.sumOf { it.dividendAmount }
            "${dividends.size} dividends totaling $${String.format("%.2f", totalAmount)} coming soon"
        }
    }
} 