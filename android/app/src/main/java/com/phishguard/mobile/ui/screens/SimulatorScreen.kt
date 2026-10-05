package com.phishguard.mobile.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.phishguard.mobile.analyzer.LocalHeuristicEngine
import com.phishguard.mobile.network.GoogleMessagePayload
import com.phishguard.mobile.network.RetrofitClient
import com.phishguard.mobile.notification.ThreatNotificationHelper
import com.phishguard.mobile.storage.LocalThreatStorage
import com.phishguard.mobile.ui.theme.*
import kotlinx.coroutines.launch

data class ThreatPreset(
    val title: String,
    val sender: String,
    val text: String,
    val category: String,
    val isThreat: Boolean
)

@Composable
fun SimulatorScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var sender by remember { mutableStateOf("+1 (800) 555-0199") }
    var text by remember {
        mutableStateOf("[CHASE-SECURITY] Unauthorized transaction of $940.00 detected. Verify identity immediately: http://chase-security-auth.xyz/verify")
    }
    var isSubmitting by remember { mutableStateOf(false) }

    // Evaluation outputs
    var evalScore by remember { mutableStateOf<Double?>(null) }
    var evalLevel by remember { mutableStateOf<String?>(null) }
    var evalPrediction by remember { mutableStateOf<String?>(null) }
    var evalReasons by remember { mutableStateOf<List<String>>(emptyList()) }
    var engineUsed by remember { mutableStateOf("Ready") }

    val presets = listOf(
        ThreatPreset(
            title = "Chase Bank Scam",
            sender = "+1 (800) 555-0199",
            text = "[CHASE-SECURITY] We detected an unauthorized transaction of $940.00 on debit card. Cancel immediately: http://chase-security-auth.xyz/verify",
            category = "Financial Fraud",
            isThreat = true
        ),
        ThreatPreset(
            title = "USPS Delivery Hold",
            sender = "USPS-TRACK",
            text = "USPS: Parcel #948201 is on hold due to incorrect address. Confirm redelivery within 12h: http://192.168.1.105/usps/track",
            category = "Delivery Lure",
            isThreat = true
        ),
        ThreatPreset(
            title = "Netflix Suspension",
            sender = "NETFLIX-ALERT",
            text = "Your Netflix membership is suspended due to billing decline. Update payment method: http://netflix-billing-update.top/account",
            category = "Subscription",
            isThreat = true
        ),
        ThreatPreset(
            title = "Legitimate 2FA Code",
            sender = "Google",
            text = "G-492810 is your Google verification code. Do not share this code with anyone.",
            category = "Safe / Benign",
            isThreat = false
        )
    )

    fun runAnalysis() {
        if (text.isBlank()) {
            Toast.makeText(context, "Please enter message text to test", Toast.LENGTH_SHORT).show()
            return
        }

        isSubmitting = true

        // 1. Instant local ML assessment (<1ms)
        val local = LocalHeuristicEngine.assessMessage(sender, text)
        evalScore = local.estimatedRiskScore
        evalLevel = if (local.isCritical) "CRITICAL" else if (local.estimatedRiskScore >= 35.0) "SUSPICIOUS" else "SAFE"
        evalPrediction = if (local.isCritical) "SMISHING" else if (local.estimatedRiskScore >= 35.0) "SUSPICIOUS" else "SAFE"
        evalReasons = local.reasons
        engineUsed = "On-Device ML (<1ms)"

        // 2. Call backend if reachable
        scope.launch {
            try {
                val payload = GoogleMessagePayload(
                    sender = sender,
                    text = text,
                    device_id = android.os.Build.MODEL ?: "android-device",
                    package_name = "com.google.android.apps.messaging",
                    timestamp = System.currentTimeMillis()
                )
                val res = RetrofitClient.apiService.analyzeGoogleMessage(payload)
                if (res.isSuccessful && res.body() != null) {
                    val body = res.body()!!
                    evalScore = body.risk_score
                    evalLevel = body.risk_level
                    evalPrediction = body.prediction
                    evalReasons = body.reasons
                    engineUsed = "Dual-Engine Cloud Synced"
                }
            } catch (e: Exception) {
                // Keep local assessment
                engineUsed = "On-Device Autonomous ML"
            } finally {
                // Log to local storage
                LocalThreatStorage.saveRecord(
                    context = context,
                    sender = sender,
                    text = text,
                    riskScore = evalScore ?: 0.0,
                    riskLevel = evalLevel ?: "SAFE",
                    prediction = evalPrediction ?: "SAFE",
                    categories = listOf("Test Lab"),
                    reasons = evalReasons
                )
                isSubmitting = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Column {
            Text(
                text = "Threat Inspector Lab",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Test any SMS or notification payload against the 98.4% NLP model.",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        // 1-Click Attack Presets (2x2 Grid with Perfect 1px Boundaries)
        Text(
            text = "ONE-TAP REAL-WORLD SAMPLES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            presets.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { p ->
                        Surface(
                            color = CyberCard,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (p.isThreat) BoundaryDanger.copy(alpha = 0.4f) else BoundarySuccess.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    sender = p.sender
                                    text = p.text
                                }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = p.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = p.category,
                                    fontSize = 10.sp,
                                    color = if (p.isThreat) AlertRed else CyberGreenLight
                                )
                            }
                        }
                    }
                }
            }
        }

        // Message Input Container (with Perfect 1px Boundary)
        Surface(
            color = CyberCard,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, BoundaryDefault),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "CUSTOM MESSAGE INSPECTOR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentCyan,
                    letterSpacing = 0.5.sp
                )

                OutlinedTextField(
                    value = sender,
                    onValueChange = { sender = it },
                    label = { Text("Sender Name or Phone Number") },
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

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("SMS Message Text") },
                    minLines = 3,
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

                Button(
                    onClick = { runAnalysis() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CyberBackground, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CyberBackground, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Analyze Message", color = CyberBackground, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Analysis Output Card (with Perfect 1px Boundary)
        if (evalScore != null) {
            val score = evalScore!!
            val isThreat = score >= 50.0
            val isWarning = score in 30.0..49.9
            val badgeColor = if (isThreat) AlertRed else if (isWarning) WarningAmber else CyberGreenLight
            val borderColor = if (isThreat) BoundaryDanger else if (isWarning) BoundaryWarning else BoundarySuccess

            Surface(
                color = CyberCard,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor.copy(alpha = 0.15f))
                                    .border(1.dp, badgeColor.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isThreat) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = badgeColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isThreat) "SMISHING THREAT" else if (isWarning) "SUSPICIOUS" else "VERIFIED BENIGN",
                                    fontWeight = FontWeight.Black,
                                    color = badgeColor,
                                    fontSize = 14.sp
                                )
                                Text(engineUsed, color = TextMuted, fontSize = 10.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeColor.copy(alpha = 0.15f))
                                .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${score.toInt()}% RISK",
                                color = badgeColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    HorizontalDivider(color = BoundaryDefault, thickness = 0.5.dp)

                    Text("Model Attribution Signals:", fontWeight = FontWeight.Bold, color = TextSecondary, fontSize = 11.sp)
                    evalReasons.forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = r, color = TextPrimary, fontSize = 11.sp)
                        }
                    }

                    if (isThreat) {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = {
                                ThreatNotificationHelper.showThreatAlert(
                                    context = context,
                                    sender = sender,
                                    text = text,
                                    riskScore = score,
                                    riskLevel = evalLevel ?: "CRITICAL",
                                    prediction = evalPrediction ?: "SMISHING",
                                    reasons = evalReasons
                                )
                                Toast.makeText(context, "🚨 High Priority Alarm Triggered!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test As Real-World Threat Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
