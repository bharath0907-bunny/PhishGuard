package com.phishguard.mobile.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phishguard.mobile.network.InterceptRecord
import com.phishguard.mobile.network.RetrofitClient
import com.phishguard.mobile.storage.LocalThreatStorage
import com.phishguard.mobile.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LiveFeedScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var records by remember { mutableStateOf<List<InterceptRecord>>(LocalThreatStorage.getRecords(context)) }
    var isLoading by remember { mutableStateOf(false) }
    var isCloudConnected by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    fun refresh() {
        val localRecords = LocalThreatStorage.getRecords(context)
        records = localRecords
        isLoading = true

        scope.launch {
            try {
                val res = RetrofitClient.apiService.getRecentIntercepts(limit = 40)
                if (res.isSuccessful && res.body() != null) {
                    isCloudConnected = true
                    val cloudList = res.body()!!
                    val merged = (localRecords + cloudList).distinctBy { "${it.sender}_${it.raw_text}" }
                    records = merged
                } else {
                    isCloudConnected = false
                }
            } catch (e: Exception) {
                isCloudConnected = false
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    val filteredRecords = records.filter { item ->
        val matchesSearch = item.sender.contains(searchQuery, ignoreCase = true) ||
                item.raw_text.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "THREATS" -> item.risk_score >= 40.0
            "SAFE" -> item.risk_score < 40.0
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Intercept Telemetry",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Google Messages & SMS Protection Stream",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (records.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            LocalThreatStorage.clearRecords(context)
                            records = emptyList()
                            Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberCard)
                            .border(1.dp, BoundaryDefault, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }
                IconButton(
                    onClick = { refresh() },
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberCard)
                        .border(1.dp, BoundaryDefault, RoundedCornerShape(8.dp))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AccentCyan, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = AccentCyan, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Active Mode Status Container (1px Boundary)
        Surface(
            color = CyberCard,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, BoundaryDefault),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isCloudConnected) CyberGreenLight else AccentCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCloudConnected) "CLOUD SYNCED & ON-DEVICE" else "AUTONOMOUS ON-DEVICE SENTINEL",
                        color = if (isCloudConnected) CyberGreenLight else AccentCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "${records.size} LOGGED",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Search Bar (1px Boundary)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter sender or message keywords...", fontSize = 12.sp, color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CyberCard,
                unfocusedContainerColor = CyberCard,
                focusedBorderColor = AccentCyan,
                unfocusedBorderColor = BoundaryDefault,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        // Filter Chips (1px Boundary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "THREATS", "SAFE").forEach { filter ->
                val isSelected = selectedFilter == filter
                val chipColor = when (filter) {
                    "THREATS" -> AlertRed
                    "SAFE" -> CyberGreenLight
                    else -> AccentCyan
                }

                Surface(
                    color = if (isSelected) chipColor.copy(alpha = 0.15f) else CyberCard,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isSelected) chipColor else BoundaryDefault),
                    modifier = Modifier
                        .clickable { selectedFilter = filter }
                ) {
                    Text(
                        text = filter,
                        color = if (isSelected) chipColor else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Telemetry Feed List
        if (filteredRecords.isEmpty()) {
            Surface(
                color = CyberCard,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, BoundaryDefault),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(AccentCyan.copy(alpha = 0.12f))
                            .border(1.dp, BoundaryAccent.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Sensors, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Awaiting Incoming Messages",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Zero-click interception will capture and record SMS here in real-time as notifications arrive.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = {
                            LocalThreatStorage.saveRecord(
                                context = context,
                                sender = "[CHASE-SECURITY]",
                                text = "ALERT: Unauthorized transfer of $940.00 from your account. Cancel immediately at http://chase-security-auth.xyz/verify",
                                riskScore = 94.0,
                                riskLevel = "CRITICAL",
                                prediction = "SMISHING",
                                categories = listOf("Financial Fraud", "Brand Impersonation"),
                                reasons = listOf(
                                    "Brand spoofing detected for 'CHASE'",
                                    "High-pressure psychological urgency detected",
                                    "Link uses suspicious unverified domain (.xyz)"
                                )
                            )
                            records = LocalThreatStorage.getRecords(context)
                            Toast.makeText(context, "Sample intercept logged to Telemetry!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Inject Sample Threat For Demo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredRecords, key = { it.id }) { item ->
                    TelemetryItemCard(
                        item = item,
                        onCopyIoc = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipText = "THREAT IOC:\nSender: ${item.sender}\nScore: ${item.risk_score}%\nText: ${item.raw_text}\nReasons: ${item.reasons.joinToString("; ")}"
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("IOC", clipText))
                            Toast.makeText(context, "Threat IOC copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TelemetryItemCard(
    item: InterceptRecord,
    onCopyIoc: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val isThreat = item.risk_score >= 50.0
    val isWarning = item.risk_score in 30.0..49.9
    val badgeColor = if (isThreat) AlertRed else if (isWarning) WarningAmber else CyberGreenLight
    val borderColor = if (isThreat) BoundaryDanger.copy(alpha = 0.5f) else if (isWarning) BoundaryWarning.copy(alpha = 0.5f) else BoundarySuccess.copy(alpha = 0.4f)

    Surface(
        color = CyberCard,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Sender & Risk Score Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isThreat) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.sender,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Google Messages • ${item.prediction}",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                // Risk Score Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${item.risk_score.toInt()}% RISK",
                        color = badgeColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Message Quote Container (Pitch dark background with 1px border)
            Surface(
                color = CyberBackground,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, BoundaryDefault),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.raw_text,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }

            // Expandable Explainable AI Attribution
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    HorizontalDivider(color = BoundaryDefault, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "EXPLAINABLE AI SIGNALS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    item.reasons.forEach { reason ->
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
                            Text(text = reason, color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.created_at,
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        OutlinedButton(
                            onClick = onCopyIoc,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy IOC", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
