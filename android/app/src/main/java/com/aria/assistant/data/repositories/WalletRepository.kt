package com.aria.assistant.data.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aria.assistant.blockchain.SolanaWalletManager
import com.aria.assistant.data.AppDatabase
import com.aria.assistant.data.entities.WalletTransaction
import com.aria.assistant.network.AriaApiService
import com.aria.assistant.network.models.TokenRewardRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Wallet Repository - Responsible for wallet-related operations
 */
class WalletRepository(
    private val context: Context,
    private val apiService: AriaApiService
) {
    private val walletManager = SolanaWalletManager(context)
    private val walletDatabase = AppDatabase.getInstance(context)
    
    // Current wallet address
    private val _walletAddress = MutableLiveData<String>()
    val walletAddress: LiveData<String> = _walletAddress
    
    // Current token balance
    private val _tokenBalance = MutableLiveData<Double>()
    val tokenBalance: LiveData<Double> = _tokenBalance
    
    init {
        refreshWalletInfo()
    }
    
    /**
     * Refresh wallet information
     */
    suspend fun refreshWalletInfo() {
        withContext(Dispatchers.IO) {
            if (walletManager.hasWallet()) {
                val address = walletManager.getWalletAddress()
                _walletAddress.postValue(address)
                
                try {
                    val balance = walletManager.getAriaTokenBalance()
                    _tokenBalance.postValue(balance)
                } catch (e: Exception) {
                    // If local wallet retrieval fails, try from API
                    try {
                        val response = apiService.getWalletInfo(address)
                        if (response.isSuccessful && response.body()?.success == true) {
                            _tokenBalance.postValue(response.body()?.data?.balance ?: 0.0)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                _walletAddress.postValue("")
                _tokenBalance.postValue(0.0)
            }
        }
    }
    
    /**
     * Create new wallet
     */
    suspend fun createWallet(): List<String>? {
        return withContext(Dispatchers.IO) {
            try {
                val mnemonic = walletManager.createWallet()
                refreshWalletInfo()
                mnemonic
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    /**
     * Import wallet
     */
    suspend fun importWallet(mnemonic: List<String>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                walletManager.importWalletFromMnemonic(mnemonic)
                refreshWalletInfo()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    
    /**
     * Get transaction history
     */
    fun getTransactionHistory(): LiveData<List<WalletTransaction>> {
        return walletDatabase.walletTransactionDao().getAllTransactions()
    }
    
    /**
     * Request token reward
     */
    suspend fun requestTokenReward(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val address = walletManager.getWalletAddress()
                val request = TokenRewardRequest(address)
                val response = apiService.requestTokenReward(request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    // Update local transaction record
                    response.body()?.data?.transactionInfo?.let { transaction ->
                        val walletTx = WalletTransaction(
                            id = transaction.txId,
                            amount = transaction.amount,
                            timestamp = System.currentTimeMillis(),
                            type = "REWARD",
                            status = "COMPLETED",
                            description = "Data Collection Reward",
                            fromAddress = transaction.fromAddress,
                            toAddress = address
                        )
                        walletDatabase.walletTransactionDao().insert(walletTx)
                    }
                    
                    refreshWalletInfo()
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
     * Check if wallet exists
     */
    fun hasWallet(): Boolean {
        return walletManager.hasWallet()
    }
} 