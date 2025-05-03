package com.daniru.financetracker.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.daniru.financetracker.R

class NotificationHelper(private val context: Context) {

    private val channelId = "budget_channel_id"
    private val channelName = "Budget Notifications"

    init {
        createNotificationChannel()
    }

    // Create a notification channel (required for Android 8.0+)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Channel for budget alerts"
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Send the notification
    fun sendBudgetExceededNotification() {
        val notification: Notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Budget Exceeded")
            .setContentText("Your spending has exceeded the budget!")
            .setSmallIcon(R.drawable.alert) // Replace with your icon
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)  // Use a unique ID for each notification
    }
}
