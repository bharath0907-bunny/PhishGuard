package com.phishguard.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phishguard.mobile.network.InterceptRecord
import com.phishguard.mobile.network.RetrofitClient
import com.phishguard.mobile.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LiveFeedScreen() {
    val scope = rememberCoroutineScope()
    var records by remember { mutableStateOf<List<InterceptRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                val res = RetrofitClient.apiService.getRecentIntercepts(limit = 30)
                if (res.isSuccessful && res.body() != null) {
                    records = res.body()!!
                } else {
                    errorMessage = "Server returned ${res.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Could not reach server: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Intercepted SMS Feed", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Button(
                onClick = { refresh() },
                colors = ButtonDefaults.buttonColors(containerColor = DarkCard)
            ) {
                Text("Refresh", color = AccentCyan, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentCyan)
            }
        } else if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Connection Status", fontWeight = FontWeight.Bold, color = WarningAmber)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(errorMessage!!, color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { refresh() }, colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)) {
                        Text("Retry")
                    }
                }
            }
        } else if (records.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No intercepted messages yet.\nIncoming Google Messages will appear here.", color = TextSecondary, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(records) { item ->
                    InterceptCard(item)
                }
            }
        }
    }
}

@Composable
fun InterceptCard(item: InterceptRecord) {
    val badgeColor = if (item.risk_score >= 60.0) AlertRed else if (item.risk_score >= 35.0) WarningAmber else CyberGreen

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.sender, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                Card(
                    colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${item.risk_level} (${item.risk_score.toInt()}%)",
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(item.raw_text, color = TextSecondary, fontSize = 13.sp)

            if (item.reasons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• " + item.reasons.first(),
                    color = badgeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
