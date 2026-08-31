package com.phishguard.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phishguard.mobile.ui.theme.*

@Composable
fun HomeScreen(
    isNotificationServiceEnabled: Boolean,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Card
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isNotificationServiceEnabled) DarkCard else Color(0xFF3B181A)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isNotificationServiceEnabled) CyberGreen else AlertRed)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isNotificationServiceEnabled) "SHIELD ACTIVE" else "INTERCEPTION DISABLED",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isNotificationServiceEnabled) CyberGreen else AlertRed
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isNotificationServiceEnabled)
                        "PhishGuard is actively intercepting incoming Google Messages notifications and analyzing smishing threats in real-time."
                    else
                        "Notification Access is required so PhishGuard can monitor Google Messages (`com.google.android.apps.messaging`) for phishing URLs & scams.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                if (!isNotificationServiceEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onOpenSettings,
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                    ) {
                        Text("Grant Google Messages Access", color = Color.White)
                    }
                }
            }
        }

        // Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricBox(
                title = "Target App",
                value = "Google Msg",
                subtitle = "Active Hook",
                accent = AccentCyan,
                modifier = Modifier.weight(1f)
            )
            MetricBox(
                title = "Threat Engine",
                value = "AI SmishX",
                subtitle = "<40ms Latency",
                accent = CyberGreen,
                modifier = Modifier.weight(1f)
            )
        }

        // Instructions Card
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("How Real-Time Google Messages Defense Works:", fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("1. When an SMS arrives in Google Messages, NotificationListenerService captures sender & text instantly.", color = TextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("2. On-device heuristics run in <5ms to detect raw IP links or urgency traps.", color = TextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("3. AI cloud engine extracts 30+ lexical features & brand spoofing triggers.", color = TextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("4. If malicious, an immediate Heads-Up Warning Banner & Alarm Notification appears!", color = TextSecondary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun MetricBox(
    title: String,
    value: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = TextSecondary, fontSize = 11.sp)
        }
    }
}
