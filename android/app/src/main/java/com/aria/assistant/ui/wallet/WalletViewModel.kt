package com.aria.assistant.ui.wallet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aria.assistant.blockchain.SolanaWalletManager
import com.aria.assistant.data.repositories.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    application: Application,
    private val walletRepository: WalletRepository
) : AndroidViewModel(application) {
    
    private val walletManager = SolanaWalletManager(application)
    
    // Wallet status
    private val _walletStatus = MutableLiveData<Boolean>()
    val walletStatus: LiveData<Boolean> = _walletStatus
    
    // Wallet balance
    private val _walletBalance = MutableLiveData<Double>(0.0)
    val walletBalance: LiveData<Double> = _walletBalance
    
    // Error messages
    private val _errorMessage = MutableLiveData<String>("")
    val errorMessage: LiveData<String> = _errorMessage
    
    /**
     * Check wallet status
     */
    fun checkWalletStatus() {
        viewModelScope.launch {
            val hasWallet = walletManager.hasWallet()
            _walletStatus.value = hasWallet
        }
    }
    
    /**
     * Create a new wallet
     * @return Mnemonic phrase list
     */
    suspend fun createWallet(): List<String> = withContext(Dispatchers.IO) {
        try {
            val mnemonic = walletManager.createWallet()
            _walletStatus.postValue(true)
            refreshBalance()
            return@withContext mnemonic
        } catch (e: Exception) {
            _errorMessage.postValue("Failed to create wallet: ${e.message}")
            throw e
        }
    }
    
    /**
     * Import wallet from mnemonic
     * @param mnemonic Mnemonic phrase list
     * @return Wallet address
     */
    suspend fun importWallet(mnemonic: List<String>): String = withContext(Dispatchers.IO) {
        try {
            val address = walletManager.importWallet(mnemonic)
            _walletStatus.postValue(true)
            refreshBalance()
            return@withContext address
        } catch (e: Exception) {
            _errorMessage.postValue("Failed to import wallet: ${e.message}")
            throw e
        }
    }
    
    /**
     * Get wallet address
     * @return Wallet address
     */
    fun getWalletAddress(): String? {
        return walletManager.getWalletAddress()
    }
    
    /**
     * Check if wallet exists
     * @return Whether wallet exists
     */
    fun hasWallet(): Boolean {
        return walletManager.hasWallet()
    }
    
    /**
     * Refresh wallet balance
     */
    suspend fun refreshBalance() {
        try {
            if (walletManager.hasWallet()) {
                val balance = walletManager.getAriaTokenBalance()
                _walletBalance.postValue(balance)
            }
        } catch (e: Exception) {
            _errorMessage.postValue("Failed to get balance: ${e.message}")
        }
    }
} 