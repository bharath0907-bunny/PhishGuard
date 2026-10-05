package com.phishguard.mobile.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
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
                    // Merge local and cloud records without duplicates
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
            "CRITICAL" -> item.risk_score >= 60.0
            "SUSPICIOUS" -> item.risk_score >= 35.0 && item.risk_score < 60.0
            "SAFE" -> item.risk_score < 35.0
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
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Live Threat Telemetry",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Real-Time Google Messages & SMS Stream",
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
                            Toast.makeText(context, "Telemetry history cleared", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberCardElevated)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Clear", tint = TextMuted)
                    }
                }
                IconButton(
                    onClick = { refresh() },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberCardElevated)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = AccentCyan)
                }
            }
        }

        // Active Engine Status Banner (Autonomous On-Device vs Cloud)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (isCloudConnected) CyberGreen.copy(alpha = 0.12f) else AccentCyan.copy(alpha = 0.10f))
                .border(
                    width = 1.dp,
                    color = if (isCloudConnected) CyberGreen.copy(alpha = 0.35f) else AccentCyan.copy(alpha = 0.30f),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isCloudConnected) CyberGreenLight else AccentCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCloudConnected) "DUAL-ENGINE CLOUD SYNCED" else "ON-DEVICE ML SENTINEL (<1ms)",
                        color = if (isCloudConnected) CyberGreenLight else AccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "${records.size} INTERCEPTS",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search sender or message content...", fontSize = 13.sp, color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CyberCard,
                unfocusedContainerColor = CyberCard,
                focusedBorderColor = AccentCyan,
                unfocusedBorderColor = CyberCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        // Severity Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "CRITICAL", "SUSPICIOUS", "SAFE").forEach { filter ->
                val isSelected = selectedFilter == filter
                val chipColor = when (filter) {
                    "CRITICAL" -> AlertRed
                    "SUSPICIOUS" -> WarningAmber
                    "SAFE" -> CyberGreenLight
                    else -> AccentCyan
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) chipColor.copy(alpha = 0.2f) else CyberCard)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) chipColor else CyberCardBorder,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = filter,
                        color = if (isSelected) chipColor else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Content Feed
        if (filteredRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(AccentCyan.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Sensors,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        "Awaiting Incoming Messages",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Zero-click interception is active for Google Messages & SMS. Incoming notifications will be analyzed in <1ms and recorded here in real-time.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = {
                            // Inject a real sample test intercept into local storage
                            val testRecord = LocalThreatStorage.saveRecord(
                                context = context,
                                sender = "[CHASE-SECURITY]",
                                text = "ALERT: Unauthorized transfer of $940.00 from your account. Cancel immediately at http://chase-security-auth.xyz/verify",
                                riskScore = 94.0,
                                riskLevel = "CRITICAL",
                                prediction = "SMISHING",
                                categories = listOf("Financial Fraud", "Brand Impersonation"),
                                reasons = listOf(
                                    "On-Device ML: High-confidence smishing vector (94%)",
                                    "Impersonates reputable institution (CHASE)",
                                    "High-pressure psychological urgency detected",
                                    "Link uses high-risk TLD (.xyz)"
                                )
                            )
                            records = LocalThreatStorage.getRecords(context)
                            Toast.makeText(context, "Simulated intercept added to Telemetry!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Record Sample Threat Test", fontSize = 12.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredRecords, key = { it.id }) { item ->
                    ExpandableInterceptCard(
                        item = item,
                        onCopyIoc = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipText = "THREAT IOC:\nSender: ${item.sender}\nScore: ${item.risk_score}\nText: ${item.raw_text}\nReasons: ${item.reasons.joinToString("; ")}"
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("IOC", clipText))
                            Toast.makeText(context, "Threat IOC copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ExpandableInterceptCard(
    item: InterceptRecord,
    onCopyIoc: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val isCritical = item.risk_score >= 60.0
    val isSuspicious = item.risk_score >= 35.0 && item.risk_score < 60.0
    val badgeColor = if (isCritical) AlertRed else if (isSuspicious) WarningAmber else CyberGreenLight

    Card(
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = badgeColor.copy(alpha = if (isCritical) 0.5f else 0.25f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCritical) Icons.Default.Warning else if (isSuspicious) Icons.Default.Warning else Icons.Default.Security,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.sender,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 14.sp
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
                        .clip(RoundedCornerShape(20.dp))
                        .background(badgeColor.copy(alpha = 0.18f))
                        .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
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

            // Message Bubble Text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyberBackground)
                    .padding(12.dp)
            ) {
                Text(
                    text = item.raw_text,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }

            // Expandable Technical Inspection Section
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    HorizontalDivider(color = CyberCardBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "EXPLAINABLE AI (XAI) ATTRIBUTION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

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
                            Text(
                                text = reason,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Timestamp: ${item.created_at}",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        OutlinedButton(
                            onClick = onCopyIoc,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy IOC", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
