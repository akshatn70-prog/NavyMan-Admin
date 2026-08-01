package com.example.navyalert.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.navyalert.util.NotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    
    // Logic to handle the permission request result
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            NotificationHelper.showTestNotification(context)
        } else {
            // Requirement 8: Explain that permission is required
            Toast.makeText(context, "Notification permission is required to receive alerts.", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("App Configuration", style = MaterialTheme.typography.titleLarge)
            
            ListItem(
                headlineContent = { Text("Dark Mode") },
                supportingContent = { Text("Follow system theme") },
                trailingContent = { Switch(checked = true, onCheckedChange = {}) }
            )
            
            ListItem(
                headlineContent = { Text("Notifications") },
                supportingContent = { Text("Enable push alerts") },
                trailingContent = { Switch(checked = true, onCheckedChange = {}) }
            )

            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                NotificationHelper.showTestNotification(context)
                            }
                            else -> {
                                // Request permission if not granted
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    } else {
                        // For Android 12 and below, permission is granted at install time
                        NotificationHelper.showTestNotification(context)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send Test Alert")
            }

            HorizontalDivider()

            Text("About", style = MaterialTheme.typography.titleLarge)
            Text("NavyMan Admin v1.0.0")
            Text("Developed for high-security asset management.")
        }
    }
}
