package com.example.navyalert

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class TelegramLauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            val telegramPackages = listOf(
                "tw.nekomimi.nekogram",
                "org.telegram.messenger",
                "org.telegram.plus",
                "org.thunderdog.challegram"
            )

            // Find all installed Telegram apps from our list
            val telegramIntents = telegramPackages.mapNotNull { pkg ->
                packageManager.getLaunchIntentForPackage(pkg)
            }

            when {
                // If no Telegram client exists, launch MainActivity
                telegramIntents.isEmpty() -> {
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
                // If exactly one exists, launch it directly
                telegramIntents.size == 1 -> {
                    startActivity(telegramIntents[0])
                }
                // If multiple exist, show chooser containing ONLY these Telegram apps
                else -> {
                    val chooserIntent = Intent.createChooser(telegramIntents[0], "📂 Open in Telegram")
                    val extraIntents = telegramIntents.drop(1).toTypedArray()
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents)
                    startActivity(chooserIntent)
                }
            }
        } catch (e: Exception) {
            // Fallback for any safety issues
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        } finally {
            finish()
        }
    }
}
