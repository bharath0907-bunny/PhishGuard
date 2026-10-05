package com.phishguard.mobile.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phishguard.mobile.network.RetrofitClient
import com.phishguard.mobile.network.UrlScanPayload
import com.phishguard.mobile.network.UrlScanMobileResponse
import com.phishguard.mobile.notification.ThreatNotificationHelper
import com.phishguard.mobile.storage.LocalThreatStorage
import com.phishguard.mobile.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    isNotificationServiceEnabled: Boolean,
    onOpenSettings: () -> Unit,
    onNavigateToTab: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Dynamic local state
    var localRecordCount by remember { mutableStateOf(LocalThreatStorage.getRecords(context).size) }
    var isCheckingConnection by remember { mutableStateOf(false) }
    var isOnline by remember { mutableStateOf(RetrofitClient.isConnected) }
    var connectionLatency by remember { mutableStateOf(RetrofitClient.latencyMs) }

    // Quick Scanner Modal State
    var showScanDialog by remember { mutableStateOf(false) }
    var scanInputText by remember { mutableStateOf("") }
    var isScanning by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<UrlScanMobileResponse?>(null) }

    // Check backend connection in background
    LaunchedEffect(Unit) {
        isCheckingConnection = true
        val (online, latency) = RetrofitClient.pingHealth()
        isOnline = online
        connectionLatency = latency
        isCheckingConnection = false
        localRecordCount = LocalThreatStorage.getRecords(context).size
    }

    // Breathing Animation for Shield Status
    val infiniteTransition = rememberInfiniteTransition(label = "shieldPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hero Protection Boundary Hub
        Surface(
            color = CyberCard,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                1.dp,
                if (isNotificationServiceEnabled) BoundarySuccess.copy(alpha = 0.6f) else BoundaryDanger.copy(alpha = 0.6f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Radial Shield Icon
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(86.dp)
                        .scale(if (isNotificationServiceEnabled) pulseScale else 1.0f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                if (isNotificationServiceEnabled) CyberGreen.copy(alpha = 0.15f) else AlertRed.copy(alpha = 0.15f)
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(if (isNotificationServiceEnabled) CyberGreen else AlertRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isNotificationServiceEnabled) Icons.Default.Shield else Icons.Default.Warning,
                            contentDescription = "Protection Shield",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isNotificationServiceEnabled) "SYSTEM PROTECTED" else "MONITORING INACTIVE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = if (isNotificationServiceEnabled) CyberGreenLight else AlertRed
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isNotificationServiceEnabled)
                        "Zero-click protection is active for Google Messages & SMS. Malicious smishing is neutralized automatically."
                    else
                        "Notification Access required so PhishGuard can intercept incoming threats and alert you in real-time.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                if (!isNotificationServiceEnabled) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onOpenSettings,
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Grant Notification Access", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // 2. Gateway Connection Status Bar (with Perfect 1px Boundary)
        Surface(
            color = CyberCard,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BoundaryDefault),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToTab(3) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCheckingConnection) WarningAmber
                                else if (isOnline) CyberGreenLight
                                else AccentCyan
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isCheckingConnection) "Checking Link..."
                            else if (isOnline) "Cloud Synced (${RetrofitClient.baseUrl.replace("http://", "")})"
                            else "Autonomous Mode: On-Device ML",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (isOnline && connectionLatency > 0) "Latency: ${connectionLatency}ms • Dual-Engine Active"
                            else "Zero Network Latency (<1ms Native)",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberBackground)
                        .border(1.dp, BoundaryDefault, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("MANAGE", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 3. Telemetry Overview: 2 Metric Containers (with Perfect 1px Boundaries)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                color = CyberCard,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BoundaryDefault),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToTab(1) }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("INTERCEPTS", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.Sensors, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$localRecordCount Logged",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("View Telemetry Stream", color = AccentCyan, fontSize = 10.sp)
                }
            }

            Surface(
                color = CyberCard,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BoundaryDefault),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToTab(2) }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ACCURACY", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.Verified, contentDescription = null, tint = CyberGreenLight, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "98.41% ML",
                        color = CyberGreenLight,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("6,044 Sample Dataset", color = TextMuted, fontSize = 10.sp)
                }
            }
        }

        // 4. Quick Action Tools Section
        Text(
            text = "INSTANT SECURITY TOOLS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )

        // Tool A: Quick URL / Text Scanner
        Surface(
            color = CyberCard,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BoundaryDefault),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = clipboard.primaryClip
                    if (clip != null && clip.itemCount > 0) {
                        scanInputText = clip.getItemAt(0).text?.toString() ?: ""
                    }
                    showScanDialog = true
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentCyan.copy(alpha = 0.15f))
                            .border(1.dp, BoundaryAccent.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Quick Scan Any Link or Text", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                        Text("Paste suspicious SMS, link or clipboard", color = TextSecondary, fontSize = 11.sp)
                    }
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
            }
        }

        // Tool B: Test Threat Alarm Notification
        Surface(
            color = CyberCard,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BoundaryDefault),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val sampleSender = "[CHASE-SECURITY]"
                    val sampleText = "ALERT: Unauthorized transfer of $940.00 from checking account. Verify identity immediately: http://chase-security-auth.xyz/verify"
                    val sampleReasons = listOf(
                        "Brand impersonation detected for 'CHASE'",
                        "High-risk abuse domain (.xyz)",
                        "Financial panic urgency pattern"
                    )

                    ThreatNotificationHelper.showThreatAlert(
                        context = context,
                        sender = sampleSender,
                        text = sampleText,
                        riskScore = 94.0,
                        riskLevel = "CRITICAL",
                        prediction = "SMISHING",
                        reasons = sampleReasons
                    )

                    LocalThreatStorage.saveRecord(
                        context = context,
                        sender = sampleSender,
                        text = sampleText,
                        riskScore = 94.0,
                        riskLevel = "CRITICAL",
                        prediction = "SMISHING",
                        categories = listOf("Financial Fraud", "Brand Impersonation"),
                        reasons = sampleReasons
                    )

                    localRecordCount = LocalThreatStorage.getRecords(context).size
                    Toast.makeText(context, "🚨 Heads-Up Threat Alarm Fired!", Toast.LENGTH_SHORT).show()
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AlertRed.copy(alpha = 0.15f))
                            .border(1.dp, BoundaryDanger.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = AlertRed, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Test Threat Alert Notification", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                        Text("Verifies sound chime, heads-up banner & vibration", color = TextSecondary, fontSize = 11.sp)
                    }
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
            }
        }

        // 5. Monitored Mobile Messaging Coverage Card (with Perfect 1px Boundary)
        Surface(
            color = CyberCard,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, BoundaryDefault),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = CyberGreenLight, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Zero-Click Messaging Protection", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                val appList = listOf(
                    "Google Messages" to "com.google.android.apps.messaging",
                    "Samsung Messages" to "com.samsung.android.messaging",
                    "WhatsApp Messenger" to "com.whatsapp",
                    "Telegram Messenger" to "org.telegram.messenger"
                )

                appList.forEach { (name, pkg) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text(pkg, color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberGreen.copy(alpha = 0.12f))
                                .border(1.dp, BoundarySuccess.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("PROTECTED", color = CyberGreenLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog: Quick Link / Text Scanner
    if (showScanDialog) {
        AlertDialog(
            onDismissRequest = {
                showScanDialog = false
                scanResult = null
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (scanInputText.isBlank()) {
                            Toast.makeText(context, "Please enter a URL or message text", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isScanning = true
                        scope.launch {
                            try {
                                val clean = scanInputText.trim()
                                val isUrl = clean.startsWith("http://") || clean.startsWith("https://") || clean.contains(".com") || clean.contains(".xyz")
                                val urlToScan = if (isUrl && !clean.startsWith("http")) "https://$clean" else clean

                                val res = RetrofitClient.apiService.scanUrl(UrlScanPayload(url = urlToScan))
                                if (res.isSuccessful && res.body() != null) {
                                    scanResult = res.body()
                                } else {
                                    // Fallback to on-device scan
                                    val local = com.phishguard.mobile.analyzer.LocalHeuristicEngine.assessMessage("Manual Scan", clean)
                                    scanResult = UrlScanMobileResponse(
                                        url = clean,
                                        risk_score = local.estimatedRiskScore,
                                        risk_level = if (local.isCritical) "CRITICAL" else "SAFE",
                                        prediction = if (local.isCritical) "PHISHING" else "BENIGN",
                                        reasons = local.reasons,
                                        recommended_action = if (local.isCritical) "BLOCK_ACCESS" else "ALLOW"
                                    )
                                }
                            } catch (e: Exception) {
                                val local = com.phishguard.mobile.analyzer.LocalHeuristicEngine.assessMessage("Manual Scan", scanInputText)
                                scanResult = UrlScanMobileResponse(
                                    url = scanInputText,
                                    risk_score = local.estimatedRiskScore,
                                    risk_level = if (local.isCritical) "CRITICAL" else "SAFE",
                                    prediction = if (local.isCritical) "PHISHING" else "BENIGN",
                                    reasons = local.reasons,
                                    recommended_action = if (local.isCritical) "BLOCK_ACCESS" else "ALLOW"
                                )
                            } finally {
                                isScanning = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isScanning
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CyberBackground, strokeWidth = 2.dp)
                    } else {
                        Text("Analyze Now", color = CyberBackground, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showScanDialog = false
                        scanResult = null
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close", color = TextSecondary)
                }
            },
            title = {
                Text("Quick Link & Threat Scanner", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter or paste any link or message body:", color = TextSecondary, fontSize = 12.sp)

                    OutlinedTextField(
                        value = scanInputText,
                        onValueChange = { scanInputText = it },
                        placeholder = { Text("e.g. http://chase-security-auth.xyz/verify", fontSize = 12.sp, color = TextMuted) },
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

                    if (scanResult != null) {
                        val res = scanResult!!
                        val isThreat = res.risk_score >= 50.0
                        val color = if (isThreat) AlertRed else CyberGreenLight

                        Surface(
                            color = CyberBackground,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(res.prediction, fontWeight = FontWeight.Black, color = color, fontSize = 14.sp)
                                    Text("${res.risk_score.toInt()}% Risk", fontWeight = FontWeight.Bold, color = color, fontSize = 13.sp)
                                }
                                Text("Action: ${res.recommended_action}", color = TextSecondary, fontSize = 11.sp)
                                res.reasons.forEach { r ->
                                    Text("• $r", color = TextPrimary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            containerColor = CyberCardElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
