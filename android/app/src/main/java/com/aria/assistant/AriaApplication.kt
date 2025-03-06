package com.aria.assistant

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.room.Room
import com.aria.assistant.data.AppDatabase
import com.aria.assistant.services.DataCollectionService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * ARIA Application Class
 */
@HiltAndroidApp
class AriaApplication : Application() {
    
    companion object {
        private const val TAG = "AriaApplication"
        private lateinit var instance: AriaApplication
        
        fun getInstance(): AriaApplication {
            return instance
        }
    }
    
    // Application level coroutine scope
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Database singleton
    val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "aria_database"
        ).build()
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        Log.d(TAG, "ARIA application initializing...")
        
        // Initialize app
        initializeApp()
    }
    
    /**
     * Initialize the application
     */
    private fun initializeApp() {
        // Here we can initialize the application, such as:
        // - Initialize crash reporting tools
        // - Register activity lifecycle callbacks
        // - Initialize analytics tools
        // - Etc...
        
        // Check if we need to auto-start data collection services
        checkAndStartServices()
    }
    
    /**
     * Check and start services
     */
    private fun checkAndStartServices() {
        // Check if data collection is enabled
        val prefs = getSharedPreferences("aria_prefs", MODE_PRIVATE)
        val isDataCollectionEnabled = prefs.getBoolean("data_collection_enabled", false)
        
        Log.d(TAG, "Data collection enabled status: $isDataCollectionEnabled")
        
        // If data collection is enabled, start the data collection service
        if (isDataCollectionEnabled) {
            val serviceIntent = Intent(this, DataCollectionService::class.java)
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                    Log.d(TAG, "Started data collection foreground service")
                } else {
                    startService(serviceIntent)
                    Log.d(TAG, "Started data collection service")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start data collection service", e)
            }
        }
    }
    
    /**
     * Clean up resources when the application exits
     */
    fun cleanUp() {
        Log.d(TAG, "Cleaning up application resources...")
        
        // Stop data collection service
        try {
            val serviceIntent = Intent(this, DataCollectionService::class.java)
            stopService(serviceIntent)
            Log.d(TAG, "Stopped data collection service")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop data collection service", e)
        }
    }
} 