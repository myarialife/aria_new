package com.aria.assistant.network

import android.content.Context
import android.content.SharedPreferences
import com.aria.assistant.AriaApplication
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Authentication Interceptor - Adds authentication token to all requests
 */
class AuthInterceptor(private val context: Context? = null) : Interceptor {
    
    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREF_NAME = "aria_auth"
        private const val KEY_TOKEN = "auth_token"
        
        /**
         * Save token
         */
        fun saveToken(context: Context, token: String) {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_TOKEN, token)
                .apply()
        }
        
        /**
         * Clear token
         */
        fun clearToken(context: Context) {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_TOKEN)
                .apply()
        }
        
        /**
         * Get token
         */
        fun getToken(context: Context): String? {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_TOKEN, null)
        }
        
        /**
         * Check if authenticated
         */
        fun isAuthenticated(context: Context): Boolean {
            return getToken(context) != null
        }
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestUrl = request.url.toString()
        
        // Check if request URL contains login or registration paths (these don't need a token)
        if (requestUrl.contains("/auth/login") || requestUrl.contains("/auth/register")) {
            return chain.proceed(request)
        }
        
        // Get authentication token
        val authToken = prefs?.getString(KEY_TOKEN, null)
        
        // If token exists, add it to request header
        return if (!authToken.isNullOrEmpty()) {
            val authenticatedRequest = request.newBuilder()
                .header("Authorization", "Bearer $authToken")
                .build()
            chain.proceed(authenticatedRequest)
        } else {
            // If no token, proceed with original request
            chain.proceed(request)
        }
    }
} 