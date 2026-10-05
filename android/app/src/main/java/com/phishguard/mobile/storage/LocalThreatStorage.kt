package com.phishguard.mobile.storage

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.phishguard.mobile.network.InterceptRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * High-performance, offline-first persistent storage for real-world SMS/Google Messages intercepts.
 * Ensures the app works for any user out-of-the-box even without an external backend.
 */
object LocalThreatStorage {
    private const val PREFS_NAME = "phishguard_threat_storage"
    private const val KEY_RECORDS = "intercepted_records"
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    @Synchronized
    fun getRecords(context: Context): List<InterceptRecord> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_RECORDS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<InterceptRecord>>() {}.type
            gson.fromJson<List<InterceptRecord>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Synchronized
    fun saveRecord(
        context: Context,
        sender: String,
        text: String,
        riskScore: Double,
        riskLevel: String,
        prediction: String,
        categories: List<String>,
        reasons: List<String>
    ): InterceptRecord {
        val existing = getRecords(context).toMutableList()
        val newRecord = InterceptRecord(
            id = UUID.randomUUID().toString().substring(0, 8),
            sender = sender,
            raw_text = text,
            risk_score = riskScore,
            risk_level = riskLevel,
            prediction = prediction,
            threat_categories = categories,
            reasons = reasons,
            created_at = dateFormat.format(Date())
        )
        // Prepend newest at the top
        existing.add(0, newRecord)
        // Keep up to 100 recent intercepts
        val trimmed = if (existing.size > 100) existing.take(100) else existing
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_RECORDS, gson.toJson(trimmed)).apply()
        return newRecord
    }

    @Synchronized
    fun clearRecords(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_RECORDS).apply()
    }
}
