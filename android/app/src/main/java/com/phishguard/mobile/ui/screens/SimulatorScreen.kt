package com.phishguard.mobile.ui.screens

import android.widget.Toast
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
import com.phishguard.mobile.ui.theme.*
import kotlinx.coroutines.launch

data class AttackPreset(
    val name: String,
    val sender: String,
    val text: String,
    val category: String,
    val color: Color
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
    var localResultScore by remember { mutableStateOf<Double?>(null) }
    var localResultReasons by remember { mutableStateOf<List<String>>(emptyList()) }
    var cloudResultScore by remember { mutableStateOf<Double?>(null) }
    var cloudResultLevel by remember { mutableStateOf<String?>(null) }
    var cloudResultReasons by remember { mutableStateOf<List<String>>(emptyList()) }

    // 6 Realistic Real-World Attack Presets
    val presets = listOf(
        AttackPreset(
            name = "Chase Bank Wire Scam",
            sender = "+1 (800) 555-0199",
            text = "[CHASE-SECURITY] We detected an unauthorized transaction of $940.00 on debit card. Cancel immediately: http://chase-security-auth.xyz/verify",
            category = "Financial Fraud",
            color = AlertRed
        ),
        AttackPreset(
            name = "USPS Delivery Lure",
            sender = "USPS-TRACK",
            text = "USPS: Parcel #948201 is on hold due to missing house number. Update redelivery within 12h: http://192.168.1.105/usps/track",
            category = "Delivery Lure",
            color = WarningAmber
        ),
        AttackPreset(
            name = "Netflix Suspension",
            sender = "NETFLIX-ALERT",
            text = "Your Netflix membership is suspended due to billing decline. Update payment method: http://netflix-billing-update.top/account",
            category = "Subscription",
            color = WarningAmber
        ),
        AttackPreset(
            name = "Apple ID Lockdown",
            sender = "AppleSupport",
            text = "Apple ID Alert: Your account was accessed from unknown device in Moscow. Reactivate iCloud: http://appleid-unlock-apple.com",
            category = "Account Takeover",
            color = AlertRed
        ),
        AttackPreset(
            name = "IRS Tax Refund",
            sender = "IRS-GOV",
            text = "IRS Notice: You have an unclaimed tax stimulus refund of $1,420.50. Claim direct deposit: http://irs-tax-refund-gov.xyz/form",
            category = "Government Impersonation",
            color = AlertRed
        ),
        AttackPreset(
            name = "Legitimate 2FA Code",
            sender = "Google",
            text = "G-492810 is your Google verification code. Do not share this code with anyone.",
            category = "Safe Benign",
            color = CyberGreenLight
        )
    )

    fun applyHomoglyphMutation() {
        text = text
            .replace("a", "а") // Cyrillic small letter a
            .replace("e", "е") // Cyrillic small letter ie
            .replace("o", "о") // Cyrillic small letter o
            .replace("p", "р") // Cyrillic small letter er
        Toast.makeText(context, "Applied Cyrillic Homoglyph Mutation!", Toast.LENGTH_SHORT).show()
    }

    fun applyZeroWidthMutation() {
        text = text.replace("http://", "http://\u200B").replace("chase", "ch\u200Base")
        Toast.makeText(context, "Applied Zero-Width Space (\\u200B) Injection!", Toast.LENGTH_SHORT).show()
    }

    fun applyTyposquatMutation() {
        text = text.replace("chase", "chaase").replace(".xyz", ".top").replace("netflix", "netfl1x")
        Toast.makeText(context, "Applied Brand Typosquatting Mutation!", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column {
            Text(
                text = "Smishing & Evasion Lab",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Simulate incoming Google Messages SMS payloads & benchmark dual-engine interception.",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        // Realistic Google Messages Bubble Mockup Preview
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberCardBorder, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AccentBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Google Messages Preview", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp)
                            Text(sender, color = AccentCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Text("Now", color = TextMuted, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Chat Bubble
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CyberBackground)
                        .padding(12.dp)
                ) {
                    Text(
                        text = text,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // 1-Click Real-World Attack Presets
        Text("1-CLICK REAL-WORLD LURES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            presets.chunked(2).forEach { rowPresets ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowPresets.forEach { p ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberCardElevated),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    sender = p.sender
                                    text = p.text
                                }
                                .border(0.5.dp, p.color.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(p.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1)
                                Text(p.category, fontSize = 10.sp, color = p.color)
                            }
                        }
                    }
                }
            }
        }

        // Adversarial Mutation Tools
        Text("ADVERSARIAL EVASION TOOLS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { applyHomoglyphMutation() },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPurple)
            ) {
                Text("Homoglyph", fontSize = 11.sp)
            }
            OutlinedButton(
                onClick = { applyZeroWidthMutation() },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan)
            ) {
                Text("Zero-Width", fontSize = 11.sp)
            }
            OutlinedButton(
                onClick = { applyTyposquatMutation() },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningAmber)
            ) {
                Text("Typosquat", fontSize = 11.sp)
            }
        }

        // Input Fields
        OutlinedTextField(
            value = sender,
            onValueChange = { sender = it },
            label = { Text("Sender Title / Phone") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CyberCard,
                unfocusedContainerColor = CyberCard,
                focusedBorderColor = AccentCyan,
                unfocusedBorderColor = CyberCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("SMS Body Payload") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CyberCard,
                unfocusedContainerColor = CyberCard,
                focusedBorderColor = AccentCyan,
                unfocusedBorderColor = CyberCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        // Dispatch Simulation Button
        Button(
            onClick = {
                isSubmitting = true

                // 1. Run On-Device Local Heuristic Check (<1ms)
                val localAssessment = LocalHeuristicEngine.assessMessage(sender, text)
                localResultScore = localAssessment.estimatedRiskScore
                localResultReasons = localAssessment.reasons

                // 2. Call Cloud API
                scope.launch {
                    try {
                        val payload = GoogleMessagePayload(
                            sender = sender,
                            text = text,
                            device_id = android.os.Build.MODEL ?: "pixel-device",
                            package_name = "com.google.android.apps.messaging",
                            timestamp = System.currentTimeMillis()
                        )
                        val res = RetrofitClient.apiService.analyzeGoogleMessage(payload)
                        if (res.isSuccessful && res.body() != null) {
                            val body = res.body()!!
                            cloudResultScore = body.risk_score
                            cloudResultLevel = body.risk_level
                            cloudResultReasons = body.reasons

                            if (body.should_alert) {
                                ThreatNotificationHelper.showThreatAlert(
                                    context = context,
                                    sender = sender,
                                    text = text,
                                    riskScore = body.risk_score,
                                    riskLevel = body.risk_level,
                                    prediction = body.prediction,
                                    reasons = body.reasons
                                )
                                Toast.makeText(context, "🚨 High Risk Smishing Alarm Triggered!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "✅ Message Verified Safe / Allowed", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            cloudResultScore = localAssessment.estimatedRiskScore
                            cloudResultLevel = if (localAssessment.isCritical) "CRITICAL" else "EVALUATED"
                            cloudResultReasons = localAssessment.reasons
                            Toast.makeText(context, "Cloud API returned ${res.code()}. Using local assessment.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        cloudResultScore = localAssessment.estimatedRiskScore
                        cloudResultLevel = if (localAssessment.isCritical) "CRITICAL" else "OFFLINE"
                        cloudResultReasons = localAssessment.reasons
                        Toast.makeText(context, "Running Offline: Evaluated via On-Device ML", Toast.LENGTH_SHORT).show()
                    } finally {
                        isSubmitting = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
            enabled = !isSubmitting
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CyberBackground)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (isSubmitting) "Evaluating Multi-Vector Engine..." else "Simulate Google Message Arrival",
                color = CyberBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        // Dual Engine Evaluation Output Card
        if (cloudResultScore != null || localResultScore != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberGreenLight.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Dual Interception Evaluation Results:", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Local Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberBackground)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("ON-DEVICE ML", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "${localResultScore?.toInt() ?: 0}% Risk",
                                    color = if ((localResultScore ?: 0.0) >= 60) AlertRed else CyberGreenLight,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                                Text("Latency: <1ms", color = TextMuted, fontSize = 10.sp)
                            }
                        }

                        // Cloud Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberBackground)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("CLOUD AI RISK", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "${cloudResultScore?.toInt() ?: 0}% Risk",
                                    color = if ((cloudResultScore ?: 0.0) >= 60) AlertRed else CyberGreenLight,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                                Text(cloudResultLevel ?: "Verified", color = TextMuted, fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Attribution Signals:", fontWeight = FontWeight.Bold, color = AccentCyan, fontSize = 11.sp)
                    val displayReasons = if (cloudResultReasons.isNotEmpty()) cloudResultReasons else localResultReasons
                    displayReasons.forEach { r ->
                        Text("• $r", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
        }
    }
}
