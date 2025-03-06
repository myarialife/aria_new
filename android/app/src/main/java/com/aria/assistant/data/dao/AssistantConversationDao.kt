package com.aria.assistant.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.aria.assistant.data.entities.AssistantConversation
import com.aria.assistant.data.entities.ConversationWithMessages

/**
 * Assistant Conversation DAO
 */
@Dao
interface AssistantConversationDao {
    
    /**
     * Insert conversation
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversation: AssistantConversation): Long
    
    /**
     * Get all conversations
     */
    @Query("SELECT * FROM assistant_conversations ORDER BY updatedAt DESC")
    fun getAllConversations(): LiveData<List<AssistantConversation>>
    
    /**
     * Get conversation by ID
     */
    @Query("SELECT * FROM assistant_conversations WHERE id = :id")
    suspend fun getConversationById(id: Long): AssistantConversation?
    
    /**
     * Get conversation with messages
     */
    @Transaction
    @Query("SELECT * FROM assistant_conversations WHERE id = :id")
    fun getConversationWithMessages(id: Long): LiveData<ConversationWithMessages>
    
    /**
     * Update conversation title
     */
    @Query("UPDATE assistant_conversations SET title = :title, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateConversationTitle(id: Long, title: String, timestamp: Long)
    
    /**
     * Update conversation timestamp
     */
    @Query("UPDATE assistant_conversations SET updatedAt = :timestamp WHERE id = :id")
    suspend fun updateConversationTimestamp(id: Long, timestamp: Long)
    
    /**
     * Delete conversation
     */
    @Query("DELETE FROM assistant_conversations WHERE id = :id")
    suspend fun deleteConversation(id: Long)
    
    /**
     * Delete all conversations
     */
    @Query("DELETE FROM assistant_conversations")
    suspend fun deleteAllConversations()
    
    /**
     * Search conversations
     */
    @Query("SELECT * FROM assistant_conversations WHERE title LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchConversations(query: String): LiveData<List<AssistantConversation>>
    
    /**
     * Get conversation count
     */
    @Query("SELECT COUNT(*) FROM assistant_conversations")
    suspend fun getConversationCount(): Int
} 