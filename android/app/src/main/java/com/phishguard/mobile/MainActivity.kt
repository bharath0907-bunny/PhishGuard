package com.phishguard.mobile

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phishguard.mobile.notification.ThreatNotificationHelper
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
            background = CyberBackground,
            surface = CyberSurface,
            surfaceVariant = CyberCard,
            primary = AccentCyan,
            secondary = CyberGreenLight,
            error = AlertRed
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(end = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AccentCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "PhishGuard Logo",
                                        tint = AccentCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "PhishGuard",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 17.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "Google Messages Sentinel",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Engine Status Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isNotificationServiceEnabled) CyberGreen.copy(alpha = 0.18f) else AlertRed.copy(alpha = 0.18f))
                                    .border(
                                        width = 1.dp,
                                        color = if (isNotificationServiceEnabled) CyberGreen.copy(alpha = 0.4f) else AlertRed.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (isNotificationServiceEnabled) CyberGreenLight else AlertRed)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isNotificationServiceEnabled) "ARMED" else "OFFLINE",
                                        color = if (isNotificationServiceEnabled) CyberGreenLight else AlertRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CyberSurface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = CyberSurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.border(width = 0.5.dp, color = CyberCardBorder)
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = "Shield",
                                tint = if (selectedTab == 0) AccentCyan else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                "Shield",
                                color = if (selectedTab == 0) AccentCyan else TextSecondary,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = AccentCyan.copy(alpha = 0.18f)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Icon(
                                Icons.Default.Sensors,
                                contentDescription = "Live Feed",
                                tint = if (selectedTab == 1) AccentCyan else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                "Telemetry",
                                color = if (selectedTab == 1) AccentCyan else TextSecondary,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = AccentCyan.copy(alpha = 0.18f)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = {
                            Icon(
                                Icons.Default.Science,
                                contentDescription = "Simulator",
                                tint = if (selectedTab == 2) AccentCyan else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                "Attack Lab",
                                color = if (selectedTab == 2) AccentCyan else TextSecondary,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = AccentCyan.copy(alpha = 0.18f)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = "Settings",
                                tint = if (selectedTab == 3) AccentCyan else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                "Policies",
                                color = if (selectedTab == 3) AccentCyan else TextSecondary,
                                fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = AccentCyan.copy(alpha = 0.18f)
                        )
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(CyberBackground)
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
