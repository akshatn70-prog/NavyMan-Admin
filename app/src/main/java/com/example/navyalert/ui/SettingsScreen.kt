package com.example.navyalert.ui

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.navyalert.util.NotificationHelper
import com.example.navyalert.util.QuietHoursManager
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    
    var isQuietHoursEnabled by remember { mutableStateOf(QuietHoursManager.isEnabled(context)) }
    var startTime by remember { mutableStateOf(QuietHoursManager.getStartTime(context)) }
    var endTime by remember { mutableStateOf(QuietHoursManager.getEndTime(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            NotificationHelper.showTestNotification(context)
        } else {
            Toast.makeText(context, "Notification permission is required to receive alerts.", Toast.LENGTH_LONG).show()
        }
    }

    // Helper to format 24h string (HH:mm) to 12h display (h:mm a)
    fun formatTo12Hour(time24: String): String {
        return try {
            val localTime = LocalTime.parse(time24, DateTimeFormatter.ofPattern("HH:mm"))
            localTime.format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))
        } catch (e: Exception) {
            time24
        }
    }

    fun showTimePicker(initialTime: String, onTimeSelected: (String) -> Unit) {
        val parts = initialTime.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        
        // is24HourView set to false for AM/PM picker
        TimePickerDialog(context, { _, h, m ->
            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", h, m)
            onTimeSelected(formattedTime)
        }, hour, minute, false).show()
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
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    } else {
                        NotificationHelper.showTestNotification(context)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send Test Alert")
            }

            HorizontalDivider()

            Text("Quiet Hours (Sleep Mode)", style = MaterialTheme.typography.titleLarge)
            
            ListItem(
                headlineContent = { Text("Enable Quiet Hours") },
                supportingContent = { Text("Suppress alerts during scheduled time") },
                trailingContent = { 
                    Switch(
                        checked = isQuietHoursEnabled, 
                        onCheckedChange = { 
                            isQuietHoursEnabled = it
                            QuietHoursManager.setEnabled(context, it)
                        }
                    ) 
                }
            )

            if (isQuietHoursEnabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Start Time", style = MaterialTheme.typography.labelLarge)
                        TextButton(onClick = { 
                            showTimePicker(startTime) {
                                startTime = it
                                QuietHoursManager.setStartTime(context, it)
                            }
                        }) {
                            Text(formatTo12Hour(startTime), style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                    Column {
                        Text("End Time", style = MaterialTheme.typography.labelLarge)
                        TextButton(onClick = { 
                            showTimePicker(endTime) {
                                endTime = it
                                QuietHoursManager.setEndTime(context, it)
                            }
                        }) {
                            Text(formatTo12Hour(endTime), style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
            }

            HorizontalDivider()

            Text("About", style = MaterialTheme.typography.titleLarge)
            Text("NavyMan Admin v1.0.0")
            Text("Developed for high-security asset management.")
        }
    }
}
