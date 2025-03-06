package com.aria.assistant.di

import android.content.Context
import com.aria.assistant.data.AppDatabase
import com.aria.assistant.data.repositories.DataRepository
import com.aria.assistant.data.repositories.WalletRepository
import com.aria.assistant.network.AriaApiService
import com.aria.assistant.network.RetrofitHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Application Dependency Injection Module - Provides various singletons
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    /**
     * Provide API service
     */
    @Provides
    @Singleton
    fun provideApiService(): AriaApiService {
        return RetrofitHelper.apiService
    }
    
    /**
     * Provide database
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }
    
    /**
     * Provide wallet repository
     */
    @Provides
    @Singleton
    fun provideWalletRepository(
        @ApplicationContext context: Context,
        apiService: AriaApiService
    ): WalletRepository {
        return WalletRepository(context, apiService)
    }
    
    /**
     * Provide data repository
     */
    @Provides
    @Singleton
    fun provideDataRepository(
        @ApplicationContext context: Context,
        apiService: AriaApiService
    ): DataRepository {
        return DataRepository(context, apiService)
    }
} 