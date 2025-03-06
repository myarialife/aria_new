package com.aria.assistant.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aria.assistant.data.repositories.DataRepository
import com.aria.assistant.data.repositories.WalletRepository
import com.aria.assistant.network.AriaApiService
import com.aria.assistant.network.RetrofitHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Dashboard ViewModel
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    application: Application,
    private val walletRepository: WalletRepository,
    private val dataRepository: DataRepository
) : AndroidViewModel(application) {
    
    private val apiService: AriaApiService = RetrofitHelper.apiService
    
    // Wallet status
    private val _walletStatus = MutableLiveData<String>()
    val walletStatus: LiveData<String> = _walletStatus
    
    // Token balance
    private val _tokenBalance = MutableLiveData<Double>()
    val tokenBalance: LiveData<Double> = _tokenBalance
    
    // Data collection status
    private val _dataCollectionEnabled = MutableLiveData<Boolean>()
    val dataCollectionEnabled: LiveData<Boolean> = _dataCollectionEnabled
    
    // Collected data count
    private val _dataPointsCollected = MutableLiveData<Int>()
    val dataPointsCollected: LiveData<Int> = _dataPointsCollected
    
    // Conversation count
    private val _assistantInteractions = MutableLiveData<Int>()
    val assistantInteractions: LiveData<Int> = _assistantInteractions
    
    // Total rewards
    private val _totalRewards = MutableLiveData<Double>()
    val totalRewards: LiveData<Double> = _totalRewards
    
    // User level
    private val _userLevel = MutableLiveData<Int>()
    val userLevel: LiveData<Int> = _userLevel
    
    init {
        loadInitialData()
    }
    
    /**
     * Load initial data
     */
    private fun loadInitialData() {
        viewModelScope.launch(Dispatchers.IO) {
            // Load wallet information
            _walletStatus.postValue(if (walletRepository.isWalletConnected()) "Connected" else "Not Connected")
            
            // Monitor wallet balance
            walletRepository.getTokenBalance().collect { balance ->
                _tokenBalance.postValue(balance)
            }
            
            // Monitor data collection status
            dataRepository.isDataCollectionEnabled().collect { isEnabled ->
                _dataCollectionEnabled.postValue(isEnabled)
            }
            
            // Monitor collected data count
            dataRepository.getDataPointCount().collect { count ->
                _dataPointsCollected.postValue(count)
            }
            
            // Monitor total rewards
            walletRepository.getTotalRewards().collect { rewards ->
                _totalRewards.postValue(rewards)
            }
            
            // Get user statistics
            getUserStats()
        }
    }
    
    /**
     * Get user statistics
     */
    private fun getUserStats() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Get user profile information
                val userProfile = apiService.getUserProfile()
                _userLevel.postValue(userProfile.level)
                
                // Get assistant interaction count
                val interactions = apiService.getInteractionCount()
                _assistantInteractions.postValue(interactions)
            } catch (e: Exception) {
                // Handle errors
                _userLevel.postValue(1)
                _assistantInteractions.postValue(0)
            }
        }
    }
    
    /**
     * Refresh all dashboard data
     */
    fun refreshDashboardData() {
        viewModelScope.launch {
            try {
                _walletStatus.value = if (walletRepository.isWalletConnected()) "Connected" else "Not Connected"
                _tokenBalance.value = walletRepository.getTokenBalanceSync()
                _dataCollectionEnabled.value = dataRepository.isDataCollectionEnabledSync()
                _dataPointsCollected.value = dataRepository.getDataPointCountSync()
                _totalRewards.value = walletRepository.getTotalRewardsSync()
                getUserStats()
            } catch (e: Exception) {
                // Handle refresh errors
            }
        }
    }
} 