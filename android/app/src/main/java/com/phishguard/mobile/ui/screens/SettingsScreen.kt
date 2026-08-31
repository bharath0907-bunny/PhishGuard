package com.phishguard.mobile.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phishguard.mobile.network.RetrofitClient
import com.phishguard.mobile.ui.theme.*

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var serverUrl by remember { mutableStateOf(RetrofitClient.baseUrl) }
    var autoBlockEnabled by remember { mutableStateOf(true) }
    var soundAlertsEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Defense Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Backend Server Address", fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Configure your PhishGuard backend IP (use 10.0.2.2 for emulator or your PC LAN IP for physical device)", color = TextSecondary, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("Server URL") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        RetrofitClient.saveBaseUrl(context, serverUrl.trim())
                        Toast.makeText(context, "Server URL saved: ${RetrofitClient.baseUrl}", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                ) {
                    Text("Save Server URL")
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Interception & Alert Policies", fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Real-Time Alarm Notifications", color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text("Trigger sound/vibrate when critical smishing is detected in Google Messages", color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = soundAlertsEnabled,
                        onCheckedChange = { soundAlertsEnabled = it }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("On-Device Local Heuristics", color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text("Instant local link analysis even if network connection drops", color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = autoBlockEnabled,
                        onCheckedChange = { autoBlockEnabled = it }
                    )
                }
            }
        }
    }
}
