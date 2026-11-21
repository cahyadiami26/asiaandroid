package com.example.data.api

import android.content.Context
import com.example.data.api.service.HrisApiService
import com.example.data.local.SessionPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit

class ApiClient(private val context: Context, private val sessionPreferences: SessionPreferences) {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Volatile
    private var cachedRetrofit: Retrofit? = null
    @Volatile
    private var cachedBaseUrl: String? = null
    @Volatile
    private var cachedApiService: HrisApiService? = null

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        // Send X-Request-Id UUID
        builder.header("X-Request-Id", "req_${UUID.randomUUID().toString().replace("-", "").take(16)}")

        // Append Bearer token if available
        val token = runBlocking { sessionPreferences.accessTokenFlow.first() }
        if (!token.isNullOrBlank() && originalRequest.header("Authorization").isNullOrEmpty()) {
            builder.header("Authorization", "Bearer $token")
        }

        chain.proceed(builder.build())
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
                redactHeader("Authorization")
            }
        )
        .build()

    suspend fun getApiService(): HrisApiService {
        val currentBaseUrl = sessionPreferences.getServerUrl()
        val formattedBaseUrl = if (currentBaseUrl.endsWith("/")) currentBaseUrl else "$currentBaseUrl/"

        if (cachedApiService != null && cachedBaseUrl == formattedBaseUrl) {
            return cachedApiService!!
        }

        synchronized(this) {
            if (cachedApiService != null && cachedBaseUrl == formattedBaseUrl) {
                return cachedApiService!!
            }
            val retrofit = Retrofit.Builder()
                .baseUrl(formattedBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            cachedRetrofit = retrofit
            cachedBaseUrl = formattedBaseUrl
            cachedApiService = retrofit.create(HrisApiService::class.java)
            return cachedApiService!!
        }
    }

    suspend fun resolveServerUrl(relativePath: String?): String {
        if (relativePath.isNullOrBlank()) return ""
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath
        }
        val baseUrl = sessionPreferences.getServerUrl()
        val cleanPath = if (relativePath.startsWith("/")) relativePath else "/$relativePath"
        return "$baseUrl$cleanPath"
    }

    fun getMoshi(): Moshi = moshi

    fun getOkHttpClient(): OkHttpClient = okHttpClient
}
