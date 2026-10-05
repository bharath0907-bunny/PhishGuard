package com.phishguard.mobile.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phishguard.mobile.network.RetrofitClient
import com.phishguard.mobile.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var serverUrl by remember { mutableStateOf(RetrofitClient.baseUrl) }
    var autoBlockEnabled by remember { mutableStateOf(true) }
    var soundAlertsEnabled by remember { mutableStateOf(true) }
    var aggressiveBrandCheck by remember { mutableStateOf(true) }
    var clipboardScanEnabled by remember { mutableStateOf(true) }

    // Live Ping State
    var isPinging by remember { mutableStateOf(false) }
    var pingResult by remember { mutableStateOf<String?>(null) }
    var pingSuccess by remember { mutableStateOf(true) }

    fun testConnection() {
        isPinging = true
        pingResult = null
        val startTime = System.currentTimeMillis()
        scope.launch {
            try {
                val res = RetrofitClient.apiService.checkHealth()
                val latency = System.currentTimeMillis() - startTime
                if (res.isSuccessful) {
                    pingSuccess = true
                    pingResult = "ONLINE • ${latency}ms Latency"
                } else {
                    pingSuccess = false
                    pingResult = "HTTP ${res.code()}"
                }
            } catch (e: Exception) {
                pingSuccess = false
                pingResult = "Unreachable"
            } finally {
                isPinging = false
            }
        }
    }

    LaunchedEffect(Unit) {
        testConnection()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("Engine & Gateway Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Configure backend gateway, connectivity tests & threat interception policies.", fontSize = 12.sp, color = TextSecondary)
        }

        // Live Connectivity HUD Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(if (pingSuccess && pingResult != null && !isPinging) CyberGreenLight else if (isPinging) WarningAmber else AlertRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Backend Link Status", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (pingSuccess && pingResult != null) CyberGreen.copy(alpha = 0.2f) else AlertRed.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isPinging) "TESTING..." else (pingResult ?: "STANDBY"),
                            color = if (pingSuccess && pingResult != null) CyberGreenLight else AlertRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("Gateway Endpoint URL") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CyberBackground,
                        unfocusedContainerColor = CyberBackground,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            serverUrl = "http://10.0.2.2:8000"
                            RetrofitClient.saveBaseUrl(context, serverUrl)
                            testConnection()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan)
                    ) {
                        Text("Emulator", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            serverUrl = "http://127.0.0.1:8000"
                            RetrofitClient.saveBaseUrl(context, serverUrl)
                            testConnection()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentBlue)
                    ) {
                        Text("Localhost", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            RetrofitClient.saveBaseUrl(context, serverUrl)
                            Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
                            testConnection()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                    ) {
                        Text("Save & Test", fontSize = 11.sp, color = CyberBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Interception Policies Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("REAL-WORLD DEFENSE POLICIES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentCyan, letterSpacing = 1.sp)

                PolicyRow(
                    title = "Heads-Up High Priority Alarm",
                    description = "Fires instant overlay banner, chime & vibration when critical smishing is detected in Google Messages.",
                    checked = soundAlertsEnabled,
                    onCheckedChange = { soundAlertsEnabled = it }
                )

                HorizontalDivider(color = CyberCardBorder, thickness = 0.5.dp)

                PolicyRow(
                    title = "Offline On-Device ML Sentinel",
                    description = "Sub-1ms local heuristic assessment guarantees continuous protection even without Wi-Fi or cellular data.",
                    checked = autoBlockEnabled,
                    onCheckedChange = { autoBlockEnabled = it }
                )

                HorizontalDivider(color = CyberCardBorder, thickness = 0.5.dp)

                PolicyRow(
                    title = "Aggressive Brand Spoofing Check",
                    description = "Levenshtein distance matching against top 50 banks, parcel couriers, and payment services.",
                    checked = aggressiveBrandCheck,
                    onCheckedChange = { aggressiveBrandCheck = it }
                )

                HorizontalDivider(color = CyberCardBorder, thickness = 0.5.dp)

                PolicyRow(
                    title = "Clipboard URL Protection Sentinel",
                    description = "Prompts instant scan whenever a deceptive web link is copied into the mobile clipboard.",
                    checked = clipboardScanEnabled,
                    onCheckedChange = { clipboardScanEnabled = it }
                )
            }
        }

        // System Diagnostics Info Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("System Diagnostics & Engine Specs", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                DiagRow("Engine Version", "PhishGuard Mobile v2.4.0")
                DiagRow("Local ML Vector Classifier", "Kotlin Native Sigmoid Engine (<1ms)")
                DiagRow("Primary OS Hook", "com.google.android.apps.messaging")
                DiagRow("Threat Bank Sync", "Active (50+ Target Brands)")
                DiagRow("Android Target SDK", "Android 14 (API Level 34)")
            }
        }
    }
}

@Composable
fun PolicyRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(description, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CyberBackground,
                checkedTrackColor = AccentCyan,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = CyberCardElevated
            )
        )
    }
}

@Composable
fun DiagRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 11.sp)
        Text(value, color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
    }
}
