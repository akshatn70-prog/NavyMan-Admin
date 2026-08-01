package com.example.navyalert

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.navyalert.ui.AppNavigation
import com.example.navyalert.ui.theme.NavyAlertTheme
import com.example.navyalert.util.FCMTokenManager
import com.example.navyalert.util.NotificationHelper
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    // Handles the response for notification permission request
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("NAVY_ADMIN", "Notification permission granted")
        } else {
            Log.d("NAVY_ADMIN", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure notification channel exists on startup
        NotificationHelper.createNotificationChannel(this)
        
        // Request POST_NOTIFICATIONS permission only once (Android 13+)
        requestNotificationPermission()

        // Fetch current token on launch. 
        // Note: As of Firebase Messaging 24.1.0/BOM 34.17.0, the getToken() API is still the primary way
        // to retrieve the registration token string for backend targeting. 
        // We use the property access which is the recommended Kotlin style.
        @Suppress("DEPRECATION")
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            // Requirement 2: Log current token with tag "FCM"
            Log.d("FCM", "Current Token: $token")
            
            // Register token with server
            FCMTokenManager.registerTokenWithServer(this, token)
        }
        
        enableEdgeToEdge()

        setContent {
            NavyAlertTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
