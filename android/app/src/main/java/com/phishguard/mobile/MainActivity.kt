package com.phishguard.mobile

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.phishguard.mobile.notification.ThreatNotificationHelper
import com.phishguard.mobile.service.GoogleMessagesListenerService
import com.phishguard.mobile.ui.screens.*
import com.phishguard.mobile.ui.theme.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.phishguard.mobile.network.RetrofitClient.init(this)
        ThreatNotificationHelper.createNotificationChannel(this)

        setContent {
            PhishGuardApp(
                isNotificationServiceEnabled = isNotificationServiceEnabled(),
                onOpenNotificationSettings = { openNotificationAccessSettings() }
            )
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat?.contains(packageName) == true
    }

    private fun openNotificationAccessSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        } else {
            Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        }
        startActivity(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhishGuardApp(
    isNotificationServiceEnabled: Boolean,
    onOpenNotificationSettings: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = DarkBackground,
            surface = DarkSurface,
            primary = AccentCyan,
            secondary = CyberGreen,
            error = AlertRed
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("PhishGuard • Google Messages Shield", color = TextPrimary)
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
                )
            },
            bottomBar = {
                NavigationBar(containerColor = DarkSurface) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Shield") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.List, contentDescription = "Live Feed") },
                        label = { Text("Live Feed") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Simulator") },
                        label = { Text("Simulator") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") }
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(DarkBackground)
            ) {
                when (selectedTab) {
                    0 -> HomeScreen(
                        isNotificationServiceEnabled = isNotificationServiceEnabled,
                        onOpenSettings = onOpenNotificationSettings
                    )
                    1 -> LiveFeedScreen()
                    2 -> SimulatorScreen()
                    3 -> SettingsScreen()
                }
            }
        }
    }
}
