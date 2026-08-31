package com.phishguard.mobile.analyzer

import java.util.regex.Pattern

data class LocalAssessment(
    val isCritical: Boolean,
    val estimatedRiskScore: Double,
    val mlProbability: Double,
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
        var heuristicScore = 0.0

        // 1. Run On-Device ML Vector Classifier (<1ms)
        val mlProb = OnDeviceMLClassifier.predictProbability(text)
        val mlScore = mlProb * 100.0

        if (mlProb >= 0.75) {
            reasons.add("On-Device ML: High-confidence smishing vector pattern (${(mlProb * 100).toInt()}%)")
        }

        // 2. Lexical & URL Checks
        val urls = MessageParser.extractUrls(text)
        if (urls.isNotEmpty()) {
            reasons.add("Contains embedded web link(s)")
            heuristicScore += 20.0

            for (url in urls) {
                // IP Address check
                if (url.matches(Regex(".*//\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*"))) {
                    heuristicScore += 60.0
                    reasons.add("Link uses direct numerical IP address: $url")
                }
                // Suspicious TLD
                for (tld in SUSPICIOUS_TLDS) {
                    if (url.contains(tld, ignoreCase = true)) {
                        heuristicScore += 45.0
                        reasons.add("Link uses high-risk TLD ($tld)")
                        break
                    }
                }
            }
        }

        if (URGENCY_PATTERN.matcher(text).find()) {
            heuristicScore += 35.0
            reasons.add("High-pressure psychological urgency detected")
        }

        if (BRAND_PATTERNS.matcher(text).find() && urls.isNotEmpty()) {
            heuristicScore += 30.0
            reasons.add("Impersonates reputable institution with unverified link")
        }

        // 3. Hybrid Fusion: 55% Heuristics + 45% On-Device ML
        val combinedScore = (0.55 * heuristicScore) + (0.45 * mlScore)
        val finalScore = combinedScore.coerceIn(0.0, 100.0)
        val isCritical = finalScore >= 70.0 || (mlProb >= 0.85 && urls.isNotEmpty())

        return LocalAssessment(
            isCritical = isCritical,
            estimatedRiskScore = finalScore,
            mlProbability = mlProb,
            reasons = if (reasons.isEmpty()) listOf("No immediate local flags.") else reasons
        )
    }
}
