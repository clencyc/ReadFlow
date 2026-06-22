package com.example.readflow.network

import android.content.Context
import android.content.SharedPreferences
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val PREFS_NAME = "readflow_prefs"
    const val KEY_SERVER_URL = "server_url"

    // Default: use your machine's local IP for physical device
    // For emulator use: http://10.0.2.2:8000/
    const val DEFAULT_URL = "https://readflow-w31r.onrender.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private fun buildClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /** Call this to get a fresh Retrofit instance using the saved URL from prefs. */
    fun getInstance(context: Context): ReadFlowApiService {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val baseUrl = prefs.getString(KEY_SERVER_URL, DEFAULT_URL)
            ?.trimEnd('/') + "/"

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(buildClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ReadFlowApiService::class.java)
    }

    /** Persist a new server URL to shared preferences. */
    fun saveServerUrl(context: Context, url: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SERVER_URL, url.trimEnd('/'))
            .apply()
    }

    /** Read back the currently saved URL (or default). */
    fun getServerUrl(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SERVER_URL, DEFAULT_URL) ?: DEFAULT_URL
    }
}
