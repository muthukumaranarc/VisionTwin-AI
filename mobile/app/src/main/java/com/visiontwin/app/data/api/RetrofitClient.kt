package com.visiontwin.app.data.api

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val DEFAULT_BASE_URL = "http://10.0.2.2:8080/"
    private var currentBaseUrl = DEFAULT_BASE_URL

    var urlVersion = mutableStateOf(0)
        private set

    val BASE_URL: String
        get() = currentBaseUrl

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun buildRetrofit(url: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private var retrofit: Retrofit = buildRetrofit(currentBaseUrl)

    var apiService: ApiService = retrofit.create(ApiService::class.java)
        private set

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences("visiontwin_cache", Context.MODE_PRIVATE)
        val savedIp = prefs.getString("backend_ip", null)
        if (!savedIp.isNullOrBlank()) {
            val formattedUrl = if (savedIp.startsWith("http://") || savedIp.startsWith("https://")) {
                if (savedIp.endsWith("/")) savedIp else "$savedIp/"
            } else {
                "http://${savedIp.trim()}:8080/"
            }
            updateBaseUrl(formattedUrl)
        }
    }

    fun updateBaseUrl(newUrl: String) {
        currentBaseUrl = newUrl
        retrofit = buildRetrofit(newUrl)
        apiService = retrofit.create(ApiService::class.java)
        urlVersion.value = urlVersion.value + 1
    }

    /** Build a full URL to serve uploaded files from the backend */
    fun fileUrl(path: String?): String {
        if (path.isNullOrBlank()) return ""
        // path is like /uploads/abc.jpg or /thumbnails/def.jpg
        val cleanPath = path.removePrefix("/")
        return "${currentBaseUrl}api/machines/files/$cleanPath"
    }
}
