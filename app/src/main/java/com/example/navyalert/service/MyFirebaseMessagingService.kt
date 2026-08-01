package com.example.navyalert.service

import android.util.Log
import com.example.navyalert.util.FCMTokenManager
import com.example.navyalert.util.NotificationHelper
import com.example.navyalert.util.QuietHoursManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val title = data["title"]
        val body = data["body"]
        val chatId = data["chat_id"]
        val messageId = data["message_id"]

        Log.d(
            "FCM",
            "Notification Received - Title: $title, Body: $body, ChatId: $chatId, MsgId: $messageId"
        )

        if (title != null && body != null) {
            handleMessage(title, body, chatId, messageId)
        }
    }

    private fun handleMessage(title: String, body: String, chatId: String?, messageId: String?) {
        // Step 13: Filtering logic for specific keywords and mentions
        // Accept both "Escrow Filled" and "New Filled Escrow Form"
        val shouldNotify = title.contains("Escrow Filled", ignoreCase = true) ||
                title.contains("New Filled Escrow Form", ignoreCase = true) ||
                body.contains("Admin Mention", ignoreCase = true) ||
                body.contains("Admins Mention", ignoreCase = true) ||
                body.contains("Navyman 1 Mention", ignoreCase = true)

        if (shouldNotify) {
            if (QuietHoursManager.isCurrentlyInQuietHours(this)) {
                Log.d("QUIET_HOURS", "Notification suppressed during quiet hours")
                QuietHoursManager.saveMissedNotification(this, title, body)
            } else {
                NotificationHelper.showNotification(this, title, body, chatId, messageId)
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Token: $token")

        // Register token with server
        FCMTokenManager.registerTokenWithServer(this, token)
    }
}
