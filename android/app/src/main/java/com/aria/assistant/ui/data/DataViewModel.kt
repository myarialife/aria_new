package com.aria.assistant.ui.data

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aria.assistant.data.entities.CollectedData
import com.aria.assistant.data.repositories.DataRepository
import com.aria.assistant.network.AriaApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataViewModel @Inject constructor(
    application: Application,
    private val dataRepository: DataRepository,
    private val apiService: AriaApiService
) : AndroidViewModel(application) {
    
    // Data collection enabled status
    private val _dataCollectionEnabled = MutableLiveData<Boolean>()
    val dataCollectionEnabled: LiveData<Boolean> = _dataCollectionEnabled
    
    // Total rewards earned
    private val _totalRewards = MutableLiveData<Double>(0.0)
    val totalRewards: LiveData<Double> = _totalRewards
    
    // Number of data items collected
    private val _collectedDataCount = MutableLiveData<Int>(0)
    val collectedDataCount: LiveData<Int> = _collectedDataCount
    
    init {
        // Monitor data collection status and rewards
        dataRepository.dataCollectionEnabled.observeForever { enabled ->
            _dataCollectionEnabled.value = enabled
        }
        
        dataRepository.totalRewards.observeForever { rewards ->
            _totalRewards.value = rewards
        }
        
        dataRepository.collectedDataCount.observeForever { count ->
            _collectedDataCount.value = count
        }
        
        // Load total rewards
        loadTotalRewards()
    }
    
    /**
     * Set data collection status
     * @param enabled Whether to enable data collection
     */
    fun setDataCollectionEnabled(enabled: Boolean) {
        dataRepository.setDataCollectionEnabled(enabled)
    }
    
    /**
     * Load total rewards
     */
    private fun loadTotalRewards() {
        viewModelScope.launch {
            try {
                dataRepository.loadTotalRewards()
            } catch (e: Exception) {
                // In a real application, should handle errors
            }
        }
    }
    
    /**
     * Set data permission
     * @param permissionType Permission type
     * @param enabled Whether to enable
     */
    fun setDataPermission(permissionType: String, enabled: Boolean) {
        viewModelScope.launch {
            try {
                dataRepository.setDataPermission(permissionType, enabled)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    /**
     * Get data permission status
     * @param permissionType Permission type
     * @return Whether enabled
     */
    fun getDataPermission(permissionType: String): Boolean {
        return dataRepository.getDataPermission(permissionType)
    }
} 