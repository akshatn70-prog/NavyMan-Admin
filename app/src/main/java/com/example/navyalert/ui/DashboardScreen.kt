package com.example.navyalert.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onNavigateToSettings: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "NavyMan Admin",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StatusCard(
                    title = "Bot Status",
                    status = "Operational",
                    details = "All systems running smoothly",
                    color = MaterialTheme.colorScheme.primaryContainer
                )
            }

            item {
                InfoCard(
                    title = "Escrow Alerts",
                    value = "0 Pending",
                    subtext = "Last checked: Just now",
                    icon = Icons.Default.Notifications
                )
            }

            item {
                InfoCard(
                    title = "Active Locks",
                    value = "12 Active",
                    subtext = "3 expiring soon",
                    icon = Icons.Default.Notifications // Placeholder icon
                )
            }

            item {
                NotificationHistoryCard()
            }
        }
    }
}

@Composable
fun StatusCard(title: String, status: String, details: String, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                ) {}
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = status, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Text(text = details, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun InfoCard(title: String, value: String, subtext: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(text = subtext, style = MaterialTheme.typography.bodySmall)
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun NotificationHistoryCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Notification History", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("• Bot started successfully", style = MaterialTheme.typography.bodyMedium)
            Text("• Escrow #1234 completed", style = MaterialTheme.typography.bodyMedium)
            Text("• New lock created by Admin", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { /* TODO */ }, modifier = Modifier.align(Alignment.End)) {
                Text("View All")
            }
        }
    }
}
