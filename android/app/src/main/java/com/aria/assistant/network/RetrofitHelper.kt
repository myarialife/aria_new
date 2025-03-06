package com.aria.assistant.network

import com.aria.assistant.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit Helper - Creates API service instance
 */
object RetrofitHelper {
    
    private const val BASE_URL = "https://api.aria.ai/v1/"
    private const val TIMEOUT = 30L
    
    // Create OkHttpClient
    private val okHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            
        // Add logging interceptor (only in DEBUG mode)
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }
        
        // Add auth interceptor
        builder.addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
            
            // For authenticated requests, add token
            // This could be managed by a TokenManager class
            // requestBuilder.header("Authorization", "Bearer $token")
            
            val request = requestBuilder.build()
            chain.proceed(request)
        }
        
        builder.build()
    }
    
    // Create Retrofit instance
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    // Create API service
    val apiService: AriaApiService by lazy {
        retrofit.create(AriaApiService::class.java)
    }
} 