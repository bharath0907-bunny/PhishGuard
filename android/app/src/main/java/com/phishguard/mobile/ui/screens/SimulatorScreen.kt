package com.phishguard.mobile.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phishguard.mobile.network.GoogleMessagePayload
import com.phishguard.mobile.network.RetrofitClient
import com.phishguard.mobile.notification.ThreatNotificationHelper
import com.phishguard.mobile.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SimulatorScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sender by remember { mutableStateOf("+1 (800) 555-0199") }
    var text by remember {
        mutableStateOf("[CHASE-SECURITY] We detected an unauthorized transaction of $940.00 on your account. Verify immediately: http://chase-security-auth.xyz/verify")
    }
    var isSubmitting by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf<String?>(null) }
    var resultColor by remember { mutableStateOf(AlertRed) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Google Messages Smishing Sandbox", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("Simulate an incoming SMS from Google Messages to test real-time interception and instant heads-up alarm notification.", color = TextSecondary, fontSize = 13.sp)

        // Presets
        Text("Quick Test Presets:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    sender = "+1 (800) 555-0199"
                    text = "[CHASE] Unauthorized $940 charge detected on debit card. If this wasn't you, verify identity immediately: http://chase-security-auth.xyz/verify"
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                modifier = Modifier.weight(1f)
            ) {
                Text("Chase Scam", fontSize = 11.sp, color = AccentCyan)
            }
            Button(
                onClick = {
                    sender = "USPS-TRACK"
                    text = "USPS: Package #94820 cannot be delivered due to invalid zip code. Update redelivery: http://192.168.1.105/usps/track"
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                modifier = Modifier.weight(1f)
            ) {
                Text("USPS Lure", fontSize = 11.sp, color = WarningAmber)
            }
            Button(
                onClick = {
                    sender = "Google"
                    text = "G-884920 is your Google verification code. Do not share this code with anyone."
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                modifier = Modifier.weight(1f)
            ) {
                Text("Safe 2FA", fontSize = 11.sp, color = CyberGreen)
            }
        }

        // Form fields
        OutlinedTextField(
            value = sender,
            onValueChange = { sender = it },
            label = { Text("Sender (Phone / Name)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentCyan,
                unfocusedBorderColor = TextSecondary,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("SMS Message Text") },
            minLines = 4,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentCyan,
                unfocusedBorderColor = TextSecondary,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Button(
            onClick = {
                isSubmitting = true
                scope.launch {
                    try {
                        val payload = GoogleMessagePayload(
                            sender = sender,
                            text = text,
                            device_id = android.os.Build.MODEL ?: "android-simulator",
                            package_name = "com.google.android.apps.messaging",
                            timestamp = System.currentTimeMillis()
                        )
                        val res = RetrofitClient.apiService.analyzeGoogleMessage(payload)
                        if (res.isSuccessful && res.body() != null) {
                            val body = res.body()!!
                            resultText = "Risk: ${body.risk_score}/100 (${body.risk_level})\nPrediction: ${body.prediction}\nReasons: ${body.reasons.joinToString("\n• ")}"
                            resultColor = if (body.risk_score >= 60) AlertRed else if (body.risk_score >= 35) WarningAmber else CyberGreen

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
                                Toast.makeText(context, "✅ Message Verified Safe / Benign", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            resultText = "Error communicating with server: ${res.code()}"
                            resultColor = AlertRed
                        }
                    } catch (e: Exception) {
                        resultText = "Failed to connect to backend: ${e.message}\n(Make sure backend is running and URL is set in Settings)"
                        resultColor = AlertRed
                    } finally {
                        isSubmitting = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
            enabled = !isSubmitting
        ) {
            Text(if (isSubmitting) "Analyzing Threat..." else "Simulate Google Message Intercept", color = Color.White, fontWeight = FontWeight.Bold)
        }

        // Result Box
        if (resultText != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Interception Analysis Result:", fontWeight = FontWeight.Bold, color = resultColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(resultText!!, color = TextPrimary, fontSize = 13.sp)
                }
            }
        }
    }
}
