package com.phishguard.mobile.network

import android.content.Context
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

data class GoogleMessagePayload(
    val sender: String,
    val text: String,
    val device_id: String,
    val package_name: String,
    val timestamp: Long
)

data class MobileAnalysisResponse(
    val id: String,
    val sender: String,
    val risk_score: Double,
    val risk_level: String,
    val prediction: String,
    val confidence: Double,
    val threat_categories: List<String>,
    val reasons: List<String>,
    val recommended_action: String,
    val should_alert: Boolean
)

data class InterceptRecord(
    val id: String,
    val sender: String,
    val raw_text: String,
    val risk_score: Double,
    val risk_level: String,
    val prediction: String,
    val threat_categories: List<String>,
    val reasons: List<String>,
    val created_at: String
)

interface PhishGuardApiService {
    @POST("/api/v1/mobile/analyze-notification")
    suspend fun analyzeGoogleMessage(@Body payload: GoogleMessagePayload): Response<MobileAnalysisResponse>

    @GET("/api/v1/mobile/recent-intercepts")
    suspend fun getRecentIntercepts(@Query("limit") limit: Int = 20): Response<List<InterceptRecord>>
}

object RetrofitClient {
    private const val PREFS_NAME = "phishguard_prefs"
    private const val KEY_BASE_URL = "backend_base_url"

    var baseUrl: String = "http://10.0.2.2:8000"
        set(value) {
            field = value
            _apiService = null
        }

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedUrl = prefs.getString(KEY_BASE_URL, null)
        if (!savedUrl.isNullOrBlank()) {
            baseUrl = savedUrl
        }
    }

    fun saveBaseUrl(context: Context, url: String) {
        baseUrl = url.trim()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BASE_URL, baseUrl).apply()
    }

    private var _apiService: PhishGuardApiService? = null

    val apiService: PhishGuardApiService
        get() {
            if (_apiService == null) {
                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(8, TimeUnit.SECONDS)
                    .build()

                val cleanUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
                val retrofit = Retrofit.Builder()
                    .baseUrl(cleanUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                _apiService = retrofit.create(PhishGuardApiService::class.java)
            }
            return _apiService!!
        }
}
