package com.aria.assistant.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.aria.assistant.R
import com.aria.assistant.data.entities.CollectedData
import com.aria.assistant.data.repositories.DataRepository
import com.aria.assistant.network.AriaApiService
import com.aria.assistant.network.RetrofitHelper
import com.aria.assistant.utils.LocationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Data Collection Service - Responsible for collecting user data in the background and syncing to server
 */
class DataCollectionService : Service() {
    
    companion object {
        private const val TAG = "DataCollectionService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "aria_data_collection_channel"
        
        // Data collection interval (milliseconds)
        private const val COLLECTION_INTERVAL = TimeUnit.MINUTES.toMillis(15)
        
        // Data sync interval (milliseconds)
        private const val SYNC_INTERVAL = TimeUnit.HOURS.toMillis(1)
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private lateinit var dataRepository: DataRepository
    private lateinit var apiService: AriaApiService
    
    private val handler = Handler(Looper.getMainLooper())
    private val dataCollectionRunnable = Runnable { collectData() }
    private val dataSyncRunnable = Runnable { syncData() }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Data collection service created")
        
        apiService = RetrofitHelper.apiService
        dataRepository = DataRepository(applicationContext, apiService)
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Data collection service started")
        
        // Start data collection and sync immediately
        scheduleDataCollection()
        scheduleDataSync()
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        Log.d(TAG, "Data collection service destroyed")
        
        // Stop all scheduled tasks
        handler.removeCallbacks(dataCollectionRunnable)
        handler.removeCallbacks(dataSyncRunnable)
        
        // Cancel all coroutines
        serviceScope.cancel()
        
        super.onDestroy()
    }
    
    /**
     * Schedule data collection task
     */
    private fun scheduleDataCollection() {
        handler.post(dataCollectionRunnable)
    }
    
    /**
     * Schedule data sync task
     */
    private fun scheduleDataSync() {
        handler.post(dataSyncRunnable)
    }
    
    /**
     * Collect data
     */
    private fun collectData() {
        serviceScope.launch {
            try {
                Log.d(TAG, "Starting data collection")
                
                // Check if data collection is enabled
                if (!dataRepository.dataCollectionEnabled.value!!) {
                    Log.d(TAG, "Data collection is disabled, skipping this collection")
                    return@launch
                }
                
                // Collect location data
                if (dataRepository.getDataPermission(DataRepository.PERMISSION_LOCATION)) {
                    collectLocationData()
                }
                
                // Collect contacts data - actual implementation should collect real data based on permissions
                if (dataRepository.getDataPermission(DataRepository.PERMISSION_CONTACTS)) {
                    // Mock data
                    val contactsData = JSONObject().apply {
                        put("count", 5)
                        put("anonymized", true)
                        put("timestamp", System.currentTimeMillis())
                    }.toString()
                    
                    dataRepository.saveCollectedData("contacts", contactsData)
                }
                
                // Collect calendar data - actual implementation should collect real data based on permissions
                if (dataRepository.getDataPermission(DataRepository.PERMISSION_CALENDAR)) {
                    // Mock data
                    val calendarData = JSONObject().apply {
                        put("events", 3)
                        put("anonymized", true)
                        put("timestamp", System.currentTimeMillis())
                    }.toString()
                    
                    dataRepository.saveCollectedData("calendar", calendarData)
                }
                
                Log.d(TAG, "Data collection completed")
                
            } catch (e: Exception) {
                Log.e(TAG, "Data collection failed", e)
            } finally {
                // Schedule next data collection
                handler.postDelayed(dataCollectionRunnable, COLLECTION_INTERVAL)
            }
        }
    }
    
    /**
     * Collect location data
     */
    private suspend fun collectLocationData() {
        try {
            if (!LocationUtils.hasLocationPermission(this)) {
                Log.d(TAG, "Missing location permission, skipping location data collection")
                return
            }
            
            val location = LocationUtils.getLastKnownLocation(this)
            if (location != null) {
                val locationData = JSONObject().apply {
                    put("latitude", location.latitude)
                    put("longitude", location.longitude)
                    put("accuracy", location.accuracy)
                    put("timestamp", location.time)
                }.toString()
                
                dataRepository.saveCollectedData("location", locationData)
                Log.d(TAG, "Location data collected")
            } else {
                Log.d(TAG, "Unable to get location information")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to collect location data", e)
        }
    }
    
    /**
     * Sync data to server
     */
    private fun syncData() {
        serviceScope.launch {
            try {
                Log.d(TAG, "Starting data sync")
                
                val success = dataRepository.syncDataToServer()
                if (success) {
                    Log.d(TAG, "Data sync successful")
                    // Update notification
                    updateNotification("Data sync successful")
                } else {
                    Log.d(TAG, "Data sync failed")
                    // Update notification
                    updateNotification("Data sync failed")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Data sync failed", e)
                // Update notification
                updateNotification("Data sync error: ${e.message}")
            } finally {
                // Schedule next data sync
                handler.postDelayed(dataSyncRunnable, SYNC_INTERVAL)
            }
        }
    }
    
    /**
     * Create notification channel
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "ARIA Data Collection"
            val descriptionText = "ARIA Assistant data collection service"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Create foreground service notification
     */
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("ARIA Assistant is running")
            .setContentText("Collecting data to improve services...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
    
    /**
     * Update notification
     */
    private fun updateNotification(text: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("ARIA Assistant is running")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
            
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}