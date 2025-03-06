package com.aria.assistant.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.aria.assistant.R
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Error Handler Utility - Provides unified error handling mechanism
 */
object ErrorHandler {
    
    private const val TAG = "ErrorHandler"
    
    /**
     * Handle exception and show friendly error message
     */
    fun handleError(context: Context, throwable: Throwable, tag: String = TAG) {
        Log.e(tag, "Error occurred", throwable)
        
        // If it's a cancellation exception, no need to show error message
        if (throwable is CancellationException) {
            return
        }
        
        val errorMessage = when (throwable) {
            // Network errors
            is UnknownHostException, is IOException -> context.getString(R.string.error_network_connection)
            is SocketTimeoutException -> context.getString(R.string.error_timeout)
            
            // API errors
            is HttpException -> {
                when (throwable.code()) {
                    401 -> context.getString(R.string.error_unauthorized)
                    403 -> context.getString(R.string.error_forbidden)
                    404 -> context.getString(R.string.error_not_found)
                    500 -> context.getString(R.string.error_server)
                    else -> context.getString(R.string.error_unknown, throwable.code())
                }
            }
            
            // Other errors
            else -> throwable.message ?: context.getString(R.string.error_unknown_general)
        }
        
        // Show error message
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }
    
    /**
     * Handle API response error
     */
    fun handleApiError(context: Context, message: String?, tag: String = TAG) {
        Log.e(tag, "API Error: $message")
        
        val errorMessage = message ?: context.getString(R.string.error_api_general)
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }
    
    /**
     * Safely execute code, handling potential exceptions
     */
    inline fun <T> tryCatch(context: Context, tag: String = TAG, action: () -> T): T? {
        return try {
            action()
        } catch (e: Exception) {
            handleError(context, e, tag)
            null
        }
    }
    
    /**
     * Safely execute suspending function, handling potential exceptions
     */
    suspend inline fun <T> tryCatchSuspend(context: Context, tag: String = TAG, action: () -> T): T? {
        return try {
            action()
        } catch (e: Exception) {
            handleError(context, e, tag)
            null
        }
    }
} 