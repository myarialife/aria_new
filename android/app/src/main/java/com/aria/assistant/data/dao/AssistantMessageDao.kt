package com.aria.assistant.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aria.assistant.data.entities.AssistantMessage

/**
 * Assistant Message DAO
 */
@Dao
interface AssistantMessageDao {
    
    /**
     * Insert message
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: AssistantMessage): Long
    
    /**
     * Insert multiple messages
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<AssistantMessage>): List<Long>
    
    /**
     * Get all messages for a conversation
     */
    @Query("SELECT * FROM assistant_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: Long): LiveData<List<AssistantMessage>>
    
    /**
     * Get message by ID
     */
    @Query("SELECT * FROM assistant_messages WHERE id = :id")
    suspend fun getMessageById(id: Long): AssistantMessage?
    
    /**
     * Get last message for a conversation
     */
    @Query("SELECT * FROM assistant_messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessageForConversation(conversationId: Long): AssistantMessage?
    
    /**
     * Delete all messages for a conversation
     */
    @Query("DELETE FROM assistant_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: Long)
    
    /**
     * Delete all messages
     */
    @Query("DELETE FROM assistant_messages")
    suspend fun deleteAllMessages()
    
    /**
     * Search messages in a conversation
     */
    @Query("SELECT * FROM assistant_messages WHERE conversationId = :conversationId AND content LIKE '%' || :query || '%' ORDER BY timestamp ASC")
    fun searchMessagesInConversation(conversationId: Long, query: String): LiveData<List<AssistantMessage>>
    
    /**
     * Search all messages
     */
    @Query("SELECT * FROM assistant_messages WHERE content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchAllMessages(query: String): LiveData<List<AssistantMessage>>
    
    /**
     * Get message count for a conversation
     */
    @Query("SELECT COUNT(*) FROM assistant_messages WHERE conversationId = :conversationId")
    suspend fun getMessageCountForConversation(conversationId: Long): Int
} 