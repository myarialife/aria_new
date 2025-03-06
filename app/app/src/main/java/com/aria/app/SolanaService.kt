package com.aria.app

import android.util.Log
import com.solana.core.Account
import com.solana.core.PublicKey
import com.solana.core.Transaction
import com.solana.networking.RPCEndpoint
import com.solana.programs.TokenProgram
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.TransactionResult
import com.solana.networking.HttpNetworkingRouter
import com.solana.networking.RPCError
import com.solana.networking.serialization.serializers.solana.SolanaResponseSerializer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Service class that handles interactions with the Solana blockchain
 */
class SolanaService(
    private val endpoint: RPCEndpoint = RPCEndpoint.mainnetBetaSolana,
    private val ariTokenMintAddress: String = "" // Will be configured with actual token address
) {
    private val TAG = "SolanaService"
    private val router = HttpNetworkingRouter(endpoint.url, SolanaResponseSerializer())
    private var walletAdapter: MobileWalletAdapter? = null
    private var connectedPublicKey: PublicKey? = null
    
    /**
     * Initialize the wallet adapter for connection to Solana wallets
     */
    fun initializeWalletAdapter(activityResultSender: ActivityResultSender) {
        walletAdapter = MobileWalletAdapter(
            activityResultSender,
            "ARIA", // App identity name
            "https://ariatoken.io", // App identity URI
            "ARIA AI Assistant" // App identity icon URL
        )
    }
    
    /**
     * Connect to a Solana wallet
     */
    suspend fun connectWallet(): PublicKey = withContext(Dispatchers.IO) {
        suspendCoroutine { continuation ->
            walletAdapter?.connect { result ->
                when(result) {
                    is TransactionResult.Success -> {
                        val publicKey = PublicKey(result.payload.publicKey)
                        connectedPublicKey = publicKey
                        Log.d(TAG, "Connected to wallet: ${publicKey.toBase58()}")
                        continuation.resume(publicKey)
                    }
                    is TransactionResult.Failure -> {
                        Log.e(TAG, "Failed to connect wallet: ${result.error}")
                        continuation.resumeWithException(Exception("Failed to connect wallet: ${result.error}"))
                    }
                    else -> {
                        Log.e(TAG, "Unknown error connecting wallet")
                        continuation.resumeWithException(Exception("Unknown error connecting wallet"))
                    }
                }
            }
        }
    }
    
    /**
     * Disconnect from the currently connected wallet
     */
    fun disconnectWallet() {
        walletAdapter?.disconnect()
        connectedPublicKey = null
    }
    
    /**
     * Get ARI token balance for the connected wallet
     */
    suspend fun getAriTokenBalance(owner: PublicKey? = connectedPublicKey): Double = withContext(Dispatchers.IO) {
        try {
            if (owner == null) {
                return@withContext 0.0
            }
            
            if (ariTokenMintAddress.isEmpty()) {
                // If token address not configured, return dummy value for MVP
                return@withContext 1000.0
            }
            
            val tokenMint = PublicKey(ariTokenMintAddress)
            val tokenAccounts = TokenProgram.getTokenAccountsByOwner(router, owner, tokenMint)
            
            var balance = 0.0
            tokenAccounts.forEach { account ->
                // Parse token amount and add to total balance
                balance += account.account.data.parsed.info.tokenAmount.uiAmountString.toDoubleOrNull() ?: 0.0
            }
            
            balance
        } catch (e: Exception) {
            Log.e(TAG, "Error getting token balance: ${e.message}")
            // Return mock data for MVP
            1000.0
        }
    }
    
    /**
     * Get transaction history for the connected wallet (simplified for MVP)
     */
    suspend fun getTransactionHistory(): List<AriaTransaction> = withContext(Dispatchers.IO) {
        try {
            // In a real implementation, this would fetch actual transactions
            // For MVP, we'll return mock data
            listOf(
                AriaTransaction("Transfer", 100.0, "2023-04-15T10:30:00Z", "Received"),
                AriaTransaction("Transfer", -25.5, "2023-04-13T15:45:00Z", "Sent"),
                AriaTransaction("Staking", 5.25, "2023-04-10T09:15:00Z", "Reward")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting transaction history: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Get SOL balance for the connected wallet
     */
    suspend fun getSolBalance(owner: PublicKey? = connectedPublicKey): Double = withContext(Dispatchers.IO) {
        try {
            if (owner == null) {
                return@withContext 0.0
            }
            
            val balance = router.getBalance(owner)
            balance.toDouble() / 1_000_000_000 // Convert lamports to SOL
        } catch (e: Exception) {
            Log.e(TAG, "Error getting SOL balance: ${e.message}")
            // Return mock data for MVP
            0.5
        }
    }
}

/**
 * Data class representing a transaction in the ARIA app
 */
data class AriaTransaction(
    val type: String,
    val amount: Double,
    val timestamp: String,
    val status: String
) 