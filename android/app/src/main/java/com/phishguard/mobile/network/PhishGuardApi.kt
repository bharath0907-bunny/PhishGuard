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

data class UrlScanPayload(
    val url: String,
    val source: String = "ANDROID_APP_CLIPBOARD"
)

data class UrlScanMobileResponse(
    val url: String,
    val risk_score: Double,
    val risk_level: String,
    val prediction: String,
    val reasons: List<String>,
    val recommended_action: String
)

interface PhishGuardApiService {
    @POST("/api/v1/mobile/analyze-notification")
    suspend fun analyzeGoogleMessage(@Body payload: GoogleMessagePayload): Response<MobileAnalysisResponse>

    @GET("/api/v1/mobile/recent-intercepts")
    suspend fun getRecentIntercepts(@Query("limit") limit: Int = 20): Response<List<InterceptRecord>>

    @GET("/health")
    suspend fun checkHealth(): Response<Map<String, Any>>

    @POST("/api/v1/analyze/url")
    suspend fun scanUrl(@Body payload: UrlScanPayload): Response<UrlScanMobileResponse>
}

object RetrofitClient {
    private const val PREFS_NAME = "phishguard_prefs"
    private const val KEY_BASE_URL = "backend_base_url"

    // Default intelligently to the local Wi-Fi PC host
    const val DEFAULT_WIFI_PC_URL = "http://192.168.1.16:8000"
    const val DEFAULT_EMULATOR_URL = "http://10.0.2.2:8000"
    const val DEFAULT_LOCALHOST_URL = "http://127.0.0.1:8000"

    var baseUrl: String = DEFAULT_WIFI_PC_URL
        set(value) {
            field = value.trim()
            _apiService = null
        }

    // Live connection tracking
    var isConnected: Boolean = false
        private set
    var latencyMs: Long = -1L
        private set
    var statusText: String = "Standby"
        private set

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedUrl = prefs.getString(KEY_BASE_URL, null)
        if (!savedUrl.isNullOrBlank()) {
            baseUrl = savedUrl
        } else {
            baseUrl = DEFAULT_WIFI_PC_URL
        }
    }

    fun saveBaseUrl(context: Context, url: String) {
        baseUrl = url.trim()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BASE_URL, baseUrl).apply()
    }

    suspend fun pingHealth(): Pair<Boolean, Long> {
        val start = System.currentTimeMillis()
        return try {
            val response = apiService.checkHealth()
            val duration = System.currentTimeMillis() - start
            if (response.isSuccessful) {
                isConnected = true
                latencyMs = duration
                statusText = "Online • ${duration}ms"
                Pair(true, duration)
            } else {
                isConnected = false
                latencyMs = -1L
                statusText = "HTTP ${response.code()}"
                Pair(false, -1L)
            }
        } catch (e: Exception) {
            isConnected = false
            latencyMs = -1L
            statusText = "Autonomous ML"
            Pair(false, -1L)
        }
    }

    private var _apiService: PhishGuardApiService? = null

    val apiService: PhishGuardApiService
        get() {
            if (_apiService == null) {
                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(3500, TimeUnit.MILLISECONDS)
                    .readTimeout(5000, TimeUnit.MILLISECONDS)
                    .retryOnConnectionFailure(true)
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

