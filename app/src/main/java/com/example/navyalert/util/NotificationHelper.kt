package com.example.navyalert.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.navyalert.MainActivity

object NotificationHelper {
    private const val CHANNEL_ID = "navy_escrow_alerts"
    private const val CHANNEL_NAME = "Navy Escrow Alerts"
    private const val CHANNEL_DESC = "Notifications for NavyMan Admin escrow and mention alerts"

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
        // Create Intent: Direct to Telegram message if IDs are present, else to App
        val intent = if (!chatId.isNullOrBlank() && !messageId.isNullOrBlank()) {
            val telegramUrl = if (chatId.startsWith("-100")) {
                // Private channel/group link format: https://t.me/c/CHAT_ID_WITHOUT_PREFIX/MSG_ID
                "https://t.me/c/${chatId.substring(4)}/$messageId"
            } else {
                // Public or direct link format: https://t.me/CHAT_ID/MSG_ID
                "https://t.me/$chatId/$messageId"
            }
            Intent(Intent.ACTION_VIEW, Uri.parse(telegramUrl))
        } else {
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 
            System.currentTimeMillis().toInt(), 
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) 
            .setContentTitle(title ?: "Navy Alert")
            .setContentText(body ?: "New alert received")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    fun showTestNotification(context: Context) {
        showNotification(context, "🛡 High Priority Alert", "This is a test notification from NavyMan Admin.")
        Toast.makeText(context, "Test Alert Sent!", Toast.LENGTH_SHORT).show()
    }
}
