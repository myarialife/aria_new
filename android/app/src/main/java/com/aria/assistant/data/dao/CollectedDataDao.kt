package com.aria.assistant.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aria.assistant.data.entities.CollectedData

/**
 * Collected Data DAO
 */
@Dao
interface CollectedDataDao {
    
    /**
     * Insert collected data
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: CollectedData): Long
    
    /**
     * Insert multiple data items
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dataItems: List<CollectedData>): List<Long>
    
    /**
     * Get all collected data
     */
    @Query("SELECT * FROM collected_data ORDER BY timestamp DESC")
    fun getAllCollectedData(): LiveData<List<CollectedData>>
    
    /**
     * Get unsynced data
     */
    @Query("SELECT * FROM collected_data WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedData(): List<CollectedData>
    
    /**
     * Get collected data count
     */
    @Query("SELECT COUNT(*) FROM collected_data")
    suspend fun getCollectedDataCount(): Int
    
    /**
     * Get total reward amount
     */
    @Query("SELECT SUM(rewardAmount) FROM collected_data WHERE rewardAmount IS NOT NULL")
    suspend fun getTotalRewards(): Double?
    
    /**
     * Update sync status
     */
    @Query("UPDATE collected_data SET isSynced = :isSynced, syncTimestamp = :syncTimestamp, rewardAmount = :rewardAmount WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, isSynced: Boolean, syncTimestamp: Long, rewardAmount: Double?)
    
    /**
     * Get data by type
     */
    @Query("SELECT * FROM collected_data WHERE type = :type ORDER BY timestamp DESC")
    fun getDataByType(type: String): LiveData<List<CollectedData>>
    
    /**
     * Delete data older than specified time
     */
    @Query("DELETE FROM collected_data WHERE timestamp < :timestamp")
    suspend fun deleteDataOlderThan(timestamp: Long)
    
    /**
     * Delete all data
     */
    @Query("DELETE FROM collected_data")
    suspend fun deleteAllData()
} 