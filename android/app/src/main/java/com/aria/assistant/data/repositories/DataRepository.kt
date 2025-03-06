package com.aria.assistant.data.repositories

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aria.assistant.data.AppDatabase
import com.aria.assistant.data.entities.CollectedData
import com.aria.assistant.network.AriaApiService
import com.aria.assistant.network.models.DataPermissionRequest
import com.aria.assistant.network.models.SubmitDataRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Data Collection Repository
 */
class DataRepository(
    private val context: Context,
    private val apiService: AriaApiService
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("aria_prefs", Context.MODE_PRIVATE)
    private val database = AppDatabase.getInstance(context)
    
    companion object {
        const val KEY_DATA_COLLECTION_ENABLED = "data_collection_enabled"
        const val KEY_PERMISSION_PREFIX = "permission_"
        
        // Permission types
        const val PERMISSION_LOCATION = "location"
        const val PERMISSION_CONTACTS = "contacts"
        const val PERMISSION_CALENDAR = "calendar"
        const val PERMISSION_SMS = "sms"
    }
    
    // Data collection status
    private val _dataCollectionEnabled = MutableLiveData<Boolean>()
    val dataCollectionEnabled: LiveData<Boolean> = _dataCollectionEnabled
    
    // Total reward amount
    private val _totalRewards = MutableLiveData<Double>()
    val totalRewards: LiveData<Double> = _totalRewards
    
    // Collected data count
    private val _collectedDataCount = MutableLiveData<Int>()
    val collectedDataCount: LiveData<Int> = _collectedDataCount
    
    init {
        loadDataCollectionStatus()
        refreshDataCount()
    }
    
    /**
     * Load data collection status
     */
    private fun loadDataCollectionStatus() {
        val isEnabled = prefs.getBoolean(KEY_DATA_COLLECTION_ENABLED, false)
        _dataCollectionEnabled.postValue(isEnabled)
    }
    
    /**
     * Set data collection status
     */
    fun setDataCollectionEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DATA_COLLECTION_ENABLED, enabled).apply()
        _dataCollectionEnabled.postValue(enabled)
    }
    
    /**
     * Refresh data count
     */
    suspend fun refreshDataCount() {
        withContext(Dispatchers.IO) {
            val count = database.collectedDataDao().getCollectedDataCount()
            _collectedDataCount.postValue(count)
        }
    }
    
    /**
     * Get unsynced data
     */
    suspend fun getUnsyncedData(): List<CollectedData> {
        return withContext(Dispatchers.IO) {
            database.collectedDataDao().getUnsyncedData()
        }
    }
    
    /**
     * Save collected data
     */
    suspend fun saveCollectedData(type: String, content: String) {
        withContext(Dispatchers.IO) {
            val data = CollectedData(
                type = type,
                content = content,
                timestamp = System.currentTimeMillis(),
                isSynced = false
            )
            database.collectedDataDao().insert(data)
            refreshDataCount()
        }
    }
    
    /**
     * Sync data to server
     */
    suspend fun syncDataToServer(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val unsyncedData = getUnsyncedData()
                if (unsyncedData.isEmpty()) {
                    return@withContext true
                }
                
                val dataItems = unsyncedData.map { data ->
                    val contentJson = try {
                        JSONObject(data.content)
                    } catch (e: Exception) {
                        JSONObject().put("raw", data.content)
                    }
                    
                    SubmitDataRequest.DataItem(
                        id = data.id,
                        type = data.type,
                        content = contentJson.toString(),
                        timestamp = data.timestamp
                    )
                }
                
                val request = SubmitDataRequest(dataItems)
                val response = apiService.submitCollectedData(request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    // Update synced data
                    response.body()?.data?.syncedData?.forEach { syncedItem ->
                        val localData = unsyncedData.find { it.id == syncedItem.id }
                        localData?.let {
                            it.apply {
                                database.collectedDataDao().updateSyncStatus(
                                    id = syncedItem.id,
                                    isSynced = true,
                                    syncTimestamp = System.currentTimeMillis(),
                                    rewardAmount = syncedItem.reward
                                )
                            }
                        }
                    }
                    
                    // Update total rewards
                    loadTotalRewards()
                    return@withContext true
                }
                false
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    
    /**
     * Load total rewards
     */
    suspend fun loadTotalRewards() {
        withContext(Dispatchers.IO) {
            try {
                // Try to get latest reward information from API
                val statsResponse = apiService.getUserStats()
                if (statsResponse.isSuccessful && statsResponse.body()?.success == true) {
                    _totalRewards.postValue(statsResponse.body()?.data?.totalRewards ?: 0.0)
                } else {
                    // If API call fails, fall back to local calculation
                    val localTotal = database.collectedDataDao().getTotalRewards() ?: 0.0
                    _totalRewards.postValue(localTotal)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // If API call throws exception, fall back to local calculation
                val localTotal = database.collectedDataDao().getTotalRewards() ?: 0.0
                _totalRewards.postValue(localTotal)
            }
        }
    }
    
    /**
     * Set data permission
     */
    suspend fun setDataPermission(permissionType: String, enabled: Boolean): Boolean {
        // Save local permission setting
        val prefKey = KEY_PERMISSION_PREFIX + permissionType
        prefs.edit().putBoolean(prefKey, enabled).apply()
        
        // Sync to server
        return withContext(Dispatchers.IO) {
            try {
                val request = DataPermissionRequest(permissionType, enabled)
                val response = apiService.updateDataPermissions(request)
                response.isSuccessful && response.body()?.success == true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    
    /**
     * Get data permission
     */
    fun getDataPermission(permissionType: String): Boolean {
        val prefKey = KEY_PERMISSION_PREFIX + permissionType
        return prefs.getBoolean(prefKey, false)
    }
} 