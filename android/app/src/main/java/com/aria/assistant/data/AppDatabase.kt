package com.aria.assistant.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aria.assistant.data.dao.AssistantConversationDao
import com.aria.assistant.data.dao.AssistantMessageDao
import com.aria.assistant.data.dao.CollectedDataDao
import com.aria.assistant.data.dao.WalletTransactionDao
import com.aria.assistant.data.entities.AssistantConversation
import com.aria.assistant.data.entities.AssistantMessage
import com.aria.assistant.data.entities.CollectedData
import com.aria.assistant.data.entities.Converters
import com.aria.assistant.data.entities.WalletTransaction

/**
 * Application Database
 */
@Database(
    entities = [
        AssistantConversation::class,
        AssistantMessage::class,
        CollectedData::class,
        WalletTransaction::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * Get assistant conversation DAO
     */
    abstract fun assistantConversationDao(): AssistantConversationDao
    
    /**
     * Get assistant message DAO
     */
    abstract fun assistantMessageDao(): AssistantMessageDao
    
    /**
     * Get collected data DAO
     */
    abstract fun collectedDataDao(): CollectedDataDao
    
    /**
     * Get wallet transaction DAO
     */
    abstract fun walletTransactionDao(): WalletTransactionDao
    
    companion object {
        private const val DATABASE_NAME = "aria_database"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
} 