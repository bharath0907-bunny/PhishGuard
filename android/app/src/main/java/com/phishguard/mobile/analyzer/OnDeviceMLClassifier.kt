package com.phishguard.mobile.analyzer

import java.util.regex.Pattern
import kotlin.math.exp

/**
 * High-Speed On-Device Statistical NLP Vector Classifier for Android.
 * Evaluates Smishing probability in <1ms without network calls.
 */
object OnDeviceMLClassifier {

    private const val INTERCEPT = -0.85
    private val TOKEN_PATTERN = Pattern.compile("(?i)\\b[a-zA-Z0-9]+\\b|https?://[^\\s]+")

    // Calibrated token weights exported from PhishGuard ML engine
    private val MODEL_WEIGHTS: Map<String, Double> = mapOf(
        "chase" to 2.85,
        "unauthorized" to 3.10,
        "wire" to 2.90,
        "verify" to 2.45,
        "login" to 2.65,
        "suspended" to 3.40,
        "locked" to 3.25,
        "restricted" to 2.80,
        "usps" to 2.95,
        "redelivery" to 3.50,
        "fedex" to 2.60,
        "customs" to 2.40,
        "fee" to 2.10,
        "parcel" to 2.70,
        "shipment" to 2.30,
        "apple" to 2.10,
        "appleid" to 3.60,
        "icloud" to 2.50,
        "netflix" to 2.30,
        "billing" to 2.20,
        "declined" to 2.75,
        "irs" to 3.20,
        "tax" to 2.60,
        "refund" to 2.90,
        "stimulus" to 3.10,
        "summons" to 2.90,
        "penalty" to 2.70,
        "winner" to 3.10,
        "won" to 2.80,
        "congratulations" to 2.50,
        "bitcoin" to 2.70,
        "btc" to 2.60,
        "airdrop" to 3.30,
        "crypto" to 2.40,
        "urgent" to 3.20,
        "immediately" to 3.05,
        "action" to 2.10,
        "required" to 2.25,
        "http" to 1.95,
        "https" to 1.40,
        "xyz" to 3.80,
        "top" to 3.60,
        "online" to 2.50,
        "site" to 2.40,
        "link" to 2.30,
        "cc" to 2.80,
        "sbs" to 3.50,
        "bitly" to 3.10,
        "tinyurl" to 3.00,
        "owly" to 2.90,
        "isgd" to 3.00,
        "hours" to 1.90,
        "24h" to 2.70,
        "within" to 1.80,
        "cancel" to 2.10,
        "fraud" to 2.95,
        "security" to 1.70,
        "alert" to 1.60,
        "account" to 1.50,
        "code" to -1.80,
        "otp" to -2.20,
        "verification" to -0.80,
        "meeting" to -3.80,
        "lunch" to -3.50,
        "dinner" to -3.20,
        "coffee" to -3.10,
        "tomorrow" to -2.90,
        "tonight" to -2.70,
        "birthday" to -3.90,
        "happy" to -2.80,
        "thanks" to -3.40,
        "home" to -2.10,
        "call" to -1.90,
        "airport" to -2.80,
        "match" to -2.50,
        "table" to -2.40,
        "dr" to -2.70,
        "appointment" to -2.80,
        "confirmed" to -2.50,
        "prescription" to -2.60,
        "pharmacy" to -2.50,
        "delivered" to -1.60,
        "mailbox" to -2.10
    )

    private fun sigmoid(z: Double): Double {
        if (z < -20.0) return 0.0
        if (z > 20.0) return 1.0
        return 1.0 / (1.0 + exp(-z))
    }

    /**
     * Computes the probability that the message is smishing in [0.0, 1.0].
     */
    fun predictProbability(text: String): Double {
        var z = INTERCEPT
        val matcher = TOKEN_PATTERN.matcher(text.lowercase())
        while (matcher.find()) {
            val token = matcher.group()
            MODEL_WEIGHTS[token]?.let { weight ->
                z += weight
            }
        }
        return sigmoid(z)
    }
}
