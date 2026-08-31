package com.phishguard.mobile.service

import android.app.Notification
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.phishguard.mobile.analyzer.LocalHeuristicEngine
import com.phishguard.mobile.analyzer.MessageParser
import com.phishguard.mobile.network.GoogleMessagePayload
import com.phishguard.mobile.network.RetrofitClient
import com.phishguard.mobile.notification.ThreatNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Real-Time Interception Service for Google Messages and all SMS/Messenger notifications.
 * Inspects incoming message payloads for smishing, brand impersonation, and malicious links.
 */
class GoogleMessagesListenerService : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val TAG = "PhishGuard-Service"

    companion object {
        val TARGET_PACKAGES = setOf(
            "com.google.android.apps.messaging", // Google Messages
            "com.samsung.android.messaging",     // Samsung Messages
            "com.android.mms",                   // Default AOSP SMS
            "com.miui.smsextra",                 // Xiaomi / MIUI SMS
            "com.oneplus.mms",                   // OnePlus SMS
            "com.coloros.mms",                   // Oppo / Realme SMS
            "com.oppo.mms",                      // Oppo SMS
            "com.vivo.mms",                      // Vivo SMS
            "com.truecaller",                    // Truecaller SMS
            "com.whatsapp",                      // WhatsApp
            "com.whatsapp.w4b",                  // WhatsApp Business
            "org.telegram.messenger",           // Telegram
            "org.thunderdog.challegram",         // Telegram X
            "com.facebook.orca",                 // Messenger
            "com.instagram.android"              // Instagram Direct
        )
    }

    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(applicationContext)
        ThreatNotificationHelper.createNotificationChannel(applicationContext)
        Log.i(TAG, "GoogleMessagesListenerService initialized with server: ${RetrofitClient.baseUrl}")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return
        val notification = sbn.notification ?: return
        val category = notification.category

        // Check if package is a known messaging app or categorized as a message/SMS
        val isTarget = TARGET_PACKAGES.contains(packageName) ||
                category == Notification.CATEGORY_MESSAGE ||
                category == Notification.CATEGORY_EMAIL ||
                (category != null && category.equals("msg", ignoreCase = true))

        if (!isTarget) {
            return
        }

        val extras = notification.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""

        val rawBody = when {
            bigText.isNotBlank() -> bigText
            text.isNotBlank() -> text
            subText.isNotBlank() -> subText
            else -> ""
        }

        if (rawBody.isBlank()) return

        val sender = if (title.isNotBlank()) title else "Incoming Message ($packageName)"

        Log.i(TAG, "Intercepted message from [$packageName] | Sender: [$sender] | Text: [$rawBody]")

        // 1. Instant On-Device Local Heuristic Check (<5ms)
        val localAssessment = LocalHeuristicEngine.assessMessage(sender, rawBody)
        if (localAssessment.isCritical) {
            ThreatNotificationHelper.showThreatAlert(
                context = applicationContext,
                sender = sender,
                text = rawBody,
                riskScore = localAssessment.estimatedRiskScore,
                riskLevel = "CRITICAL",
                prediction = "SMISHING",
                reasons = localAssessment.reasons
            )
        }

        // 2. Real-Time Async AI Cloud Analysis with FastAPI Backend
        scope.launch {
            try {
                val payload = GoogleMessagePayload(
                    sender = sender,
                    text = rawBody,
                    device_id = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
                    package_name = packageName,
                    timestamp = System.currentTimeMillis()
                )

                val response = RetrofitClient.apiService.analyzeGoogleMessage(payload)

                if (response.isSuccessful && response.body() != null) {
                    val analysis = response.body()!!
                    Log.i(TAG, "AI Cloud Analysis: Risk=${analysis.risk_score} (${analysis.risk_level})")

                    // If API flagged as threat and local critical alert wasn't already triggered
                    if (analysis.should_alert && !localAssessment.isCritical) {
                        ThreatNotificationHelper.showThreatAlert(
                            context = applicationContext,
                            sender = analysis.sender,
                            text = rawBody,
                            riskScore = analysis.risk_score,
                            riskLevel = analysis.risk_level,
                            prediction = analysis.prediction,
                            reasons = analysis.reasons
                        )
                    }
                } else {
                    Log.w(TAG, "API Response code: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect to PhishGuard backend (${RetrofitClient.baseUrl}): ${e.message}")
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }
}
