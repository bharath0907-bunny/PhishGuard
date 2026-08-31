package com.phishguard.mobile.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.phishguard.mobile.MainActivity

object ThreatNotificationHelper {

    private const val CHANNEL_ID = "phishguard_threat_alerts"
    private const val CHANNEL_NAME = "PhishGuard Threat Alerts"
    private var notificationIdCounter = 1000

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alerts for smishing and phishing threats detected in Google Messages"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showThreatAlert(
        context: Context,
        sender: String,
        text: String,
        riskScore: Double,
        riskLevel: String,
        prediction: String,
        reasons: List<String>
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_THREAT_SENDER", sender)
            putExtra("EXTRA_THREAT_TEXT", text)
            putExtra("EXTRA_THREAT_SCORE", riskScore)
            putExtra("EXTRA_THREAT_REASONS", ArrayList(reasons))
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationIdCounter,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val primaryReason = reasons.firstOrNull() ?: "High-risk indicators identified"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("⚠️ SMISHING DETECTED: $sender")
            .setContentText("Risk Score: ${riskScore.toInt()}/100 ($riskLevel) - $primaryReason")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("🚨 Threat Alert from: $sender\nRisk Level: $riskLevel (${riskScore.toInt()}%)\n\nMessage: \"$text\"\n\nFlags: ${reasons.joinToString("\n• ")}")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setColor(Color.RED)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationIdCounter++, builder.build())
    }
}
