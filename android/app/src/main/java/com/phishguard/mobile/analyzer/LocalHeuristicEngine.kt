package com.phishguard.mobile.analyzer

import java.util.regex.Pattern

data class LocalAssessment(
    val isCritical: Boolean,
    val estimatedRiskScore: Double,
    val reasons: List<String>
)

object LocalHeuristicEngine {

    private val URGENCY_PATTERN = Pattern.compile(
        "(?i)\\b(immediate(ly)?|account (suspended|locked|restricted)|action required|unauthorized (access|transaction)|security alert|card (declined|deactivated))\\b"
    )

    private val SUSPICIOUS_TLDS = setOf(
        ".tk", ".ml", ".ga", ".cf", ".gq", ".top", ".xyz", ".club", ".work",
        ".buzz", ".icu", ".cam", ".rest", ".online", ".site", ".app", ".sbs"
    )

    private val BRAND_PATTERNS = Pattern.compile(
        "(?i)\\b(chase|wellsfargo|bankofamerica|citi|paypal|usps|fedex|ups|dhl|amazon|netflix|apple id|irs|venmo)\\b"
    )

    fun assessMessage(sender: String, text: String): LocalAssessment {
        val reasons = mutableListOf<String>()
        var score = 0.0

        val urls = MessageParser.extractUrls(text)
        if (urls.isNotEmpty()) {
            reasons.add("Contains embedded web link(s)")
            score += 20.0

            for (url in urls) {
                // IP Address check
                if (url.matches(Regex(".*//\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*"))) {
                    score += 60.0
                    reasons.add("Link uses direct numerical IP address: $url")
                }
                // Suspicious TLD
                for (tld in SUSPICIOUS_TLDS) {
                    if (url.contains(tld, ignoreCase = true)) {
                        score += 45.0
                        reasons.add("Link uses high-risk TLD ($tld)")
                        break
                    }
                }
            }
        }

        if (URGENCY_PATTERN.matcher(text).find()) {
            score += 35.0
            reasons.add("High-pressure psychological urgency detected")
        }

        if (BRAND_PATTERNS.matcher(text).find() && urls.isNotEmpty()) {
            score += 30.0
            reasons.add("Impersonates reputable institution with unauthorized link")
        }

        val isCritical = score >= 75.0
        return LocalAssessment(
            isCritical = isCritical,
            estimatedRiskScore = score.coerceIn(0.0, 100.0),
            reasons = if (reasons.isEmpty()) listOf("No immediate local flags.") else reasons
        )
    }
}
