package com.example.navyalert.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.navyalert.MainActivity
import com.example.navyalert.TelegramLauncherActivity
import com.example.navyalert.service.AlertSoundService

object NotificationHelper {
    private const val CHANNEL_ID = "navy_escrow_alerts"
    private const val CHANNEL_NAME = "Navy Escrow Alerts"
    private const val CHANNEL_DESC = "Notifications for NavyMan Admin escrow and mention alerts"
    
    // Fixed IDs to prevent multiple notification spam and crashes
    private const val ESCROW_NOTIFICATION_ID = 2001
    private const val MENTION_NOTIFICATION_ID = 2002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(
        context: Context, 
        title: String?, 
        body: String?, 
        chatId: String? = null, 
        messageId: String? = null
    ) {
        val isEscrowForm = title?.contains("New Filled Escrow Form", ignoreCase = true) == true
        
        // 1. Normal notification tap behavior: Always open Navy Alert app (MainActivity)
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            appIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Stop Action for the alert sound
        val stopIntent = Intent(context, AlertSoundService::class.java).apply {
            action = AlertSoundService.STOP_ACTION
        }
        val stopPendingIntent = PendingIntent.getService(
            context, 2, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) 
            .setContentTitle(title ?: "Navy Alert")
            .setContentText(body ?: "New alert received")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setDeleteIntent(stopPendingIntent) // Stop sound when notification is swiped away
            .setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)

        // Apply Sound setting (Only if Alert Service is NOT going to play it)
        if (!isEscrowForm) {
            if (NotificationSettingsManager.isSoundEnabled(context)) {
                val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                builder.setSound(defaultSoundUri)
            } else {
                builder.setSilent(true)
            }
        } else {
            // For Escrow Form, we start the foreground service
            val serviceIntent = Intent(context, AlertSoundService::class.java)
            try {
                ContextCompat.startForegroundService(context, serviceIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // Add Stop button to the notification itself
            builder.addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop Alert",
                stopPendingIntent
            )
        }

        // Apply Vibration setting
        if (NotificationSettingsManager.isVibrationEnabled(context)) {
            builder.setVibrate(longArrayOf(0, 500, 200, 500))
        } else {
            builder.setVibrate(longArrayOf(0))
        }

        // 2. Add "📂 Open in Telegram" action button if it's an escrow form
        if (isEscrowForm) {
            val launcherIntent = Intent(context, TelegramLauncherActivity::class.java)
            
            val actionPendingIntent = PendingIntent.getActivity(
                context,
                1, 
                launcherIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            builder.addAction(
                android.R.drawable.ic_menu_send, 
                "📂 Open in Telegram", 
                actionPendingIntent
            )
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Use fixed IDs to prevent unlimited notifications and system crashes
        val notificationId = if (isEscrowForm) ESCROW_NOTIFICATION_ID else MENTION_NOTIFICATION_ID
        notificationManager.notify(notificationId, builder.build())
    }

    fun showTestNotification(context: Context) {
        showNotification(context, "🛡 High Priority Alert", "This is a test notification from NavyMan Admin.")
        Toast.makeText(context, "Test Alert Sent!", Toast.LENGTH_SHORT).show()
    }
}
