package com.phishguard.mobile.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
    var soundAlertsEnabled by remember { mutableStateOf(true) }
    var onDeviceMlEnabled by remember { mutableStateOf(true) }
    var aggressiveBrandCheck by remember { mutableStateOf(true) }

    // Live Ping State
    var isPinging by remember { mutableStateOf(false) }
    var pingStatus by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    fun testConnection() {
        isPinging = true
        pingStatus = null
        scope.launch {
            val (ok, latency) = RetrofitClient.pingHealth()
            isSuccess = ok
            if (ok) {
                pingStatus = "ONLINE • ${latency}ms Latency"
            } else {
                pingStatus = "Autonomous Mode (On-Device ML Active)"
            }
            isPinging = false
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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column {
            Text("Settings & Gateway Link", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Manage backend server connection and threat alert policies.", fontSize = 12.sp, color = TextSecondary)
        }

        // 1. Backend Gateway Connection Container (with Perfect 1px Boundary)
        Surface(
            color = CyberCard,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, BoundaryDefault),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isPinging) WarningAmber
                                    else if (isSuccess) CyberGreenLight
                                    else AccentCyan
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Backend Link Status", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isSuccess) CyberGreen.copy(alpha = 0.15f)
                                else AccentCyan.copy(alpha = 0.12f)
                            )
                            .border(
                                1.dp,
                                if (isSuccess) BoundarySuccess.copy(alpha = 0.4f)
                                else BoundaryAccent.copy(alpha = 0.3f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isPinging) "CONNECTING..." else (pingStatus ?: "STANDBY"),
                            color = if (isSuccess) CyberGreenLight else AccentCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Text(
                    text = "PhishGuard works autonomously on-device for anyone. Connecting to your PC gateway synchronizes cloud models and the research database.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("Backend URL Endpoint") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CyberBackground,
                        unfocusedContainerColor = CyberBackground,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = BoundaryDefault,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // 1-Tap Preset Switchers (with 1px Boundaries)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            serverUrl = RetrofitClient.DEFAULT_WIFI_PC_URL
                            RetrofitClient.saveBaseUrl(context, serverUrl)
                            Toast.makeText(context, "Set to Wi-Fi PC (192.168.1.16)", Toast.LENGTH_SHORT).show()
                            testConnection()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberGreenLight),
                        border = BorderStroke(1.dp, BoundarySuccess.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("Wi-Fi PC", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            serverUrl = RetrofitClient.DEFAULT_EMULATOR_URL
                            RetrofitClient.saveBaseUrl(context, serverUrl)
                            Toast.makeText(context, "Set to Emulator (10.0.2.2)", Toast.LENGTH_SHORT).show()
                            testConnection()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                        border = BorderStroke(1.dp, BoundaryAccent.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("Emulator", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            RetrofitClient.saveBaseUrl(context, serverUrl)
                            Toast.makeText(context, "Connecting...", Toast.LENGTH_SHORT).show()
                            testConnection()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        if (isPinging) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = CyberBackground, strokeWidth = 2.dp)
                        } else {
                            Text("Save & Connect", fontSize = 11.sp, color = CyberBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Defense Policies Container (with Perfect 1px Boundary)
        Surface(
            color = CyberCard,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, BoundaryDefault),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "AUTONOMOUS DEFENSE POLICIES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentCyan,
                    letterSpacing = 0.5.sp
                )

                PolicyItem(
                    title = "Heads-Up Threat Alert Alarm",
                    subtitle = "Fires high-priority sound chime, vibration, and overlay banner when smishing is detected.",
                    checked = soundAlertsEnabled,
                    onCheckedChange = { soundAlertsEnabled = it }
                )

                HorizontalDivider(color = BoundaryDefault, thickness = 0.5.dp)

                PolicyItem(
                    title = "Autonomous On-Device AI (<1ms)",
                    subtitle = "Instant local vector evaluation runs without needing internet or cell connectivity.",
                    checked = onDeviceMlEnabled,
                    onCheckedChange = { onDeviceMlEnabled = it }
                )

                HorizontalDivider(color = BoundaryDefault, thickness = 0.5.dp)

                PolicyItem(
                    title = "Bank & Brand Spoofing Check",
                    subtitle = "Levenshtein distance matching against top 50 financial institutions and parcel delivery services.",
                    checked = aggressiveBrandCheck,
                    onCheckedChange = { aggressiveBrandCheck = it }
                )
            }
        }

        // 3. Engine Diagnostics Container (with Perfect 1px Boundary)
        Surface(
            color = CyberCard,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, BoundaryDefault),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Model Architecture & Diagnostics", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(2.dp))

                DiagRow("Dataset Training Base", "6,044 SMS Real-World Samples")
                DiagRow("Model Accuracy / F1", "98.41% Accuracy (0.9588 F1)")
                DiagRow("Primary OS Target Hook", "com.google.android.apps.messaging")
                DiagRow("Inference Latency", "< 1 ms Native Sigmoid Engine")
                DiagRow("Version", "PhishGuard Mobile v2.5.0")
            }
        }
    }
}

@Composable
fun PolicyItem(
    title: String,
    subtitle: String,
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
            Text(subtitle, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CyberBackground,
                checkedTrackColor = AccentCyan,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = BoundaryDefault
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
