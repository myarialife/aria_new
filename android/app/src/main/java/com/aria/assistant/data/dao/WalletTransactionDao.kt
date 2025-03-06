package com.aria.assistant.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aria.assistant.data.entities.WalletTransaction

/**
 * Wallet Transaction DAO
 */
@Dao
interface WalletTransactionDao {
    
    /**
     * Insert wallet transaction
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: WalletTransaction)
    
    /**
     * Insert multiple wallet transactions
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<WalletTransaction>)
    
    /**
     * Get all transactions
     */
    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): LiveData<List<WalletTransaction>>
    
    /**
     * Get transactions by type
     */
    @Query("SELECT * FROM wallet_transactions WHERE type = :type ORDER BY timestamp DESC")
    fun getTransactionsByType(type: String): LiveData<List<WalletTransaction>>
    
    /**
     * Get transaction by ID
     */
    @Query("SELECT * FROM wallet_transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): WalletTransaction?
    
    /**
     * Get total amount by transaction type
     */
    @Query("SELECT SUM(amount) FROM wallet_transactions WHERE type = :type")
    suspend fun getTotalAmountByType(type: String): Double?
    
    /**
     * Delete all transactions
     */
    @Query("DELETE FROM wallet_transactions")
    suspend fun deleteAllTransactions()
    
    /**
     * Delete transaction by ID
     */
    @Query("DELETE FROM wallet_transactions WHERE id = :id")
    suspend fun deleteTransaction(id: String)
    
    /**
     * Get transaction count
     */
    @Query("SELECT COUNT(*) FROM wallet_transactions")
    suspend fun getTransactionCount(): Int
} 