package com.aria.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.aria.app.utils.LocaleHelper
import dagger.hilt.android.HiltAndroidApp

/**
 * Main application class for ARIA assistant
 */
@HiltAndroidApp
class ARIAApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize app-wide settings
        initializeAppSettings()
    }
    
    /**
     * Initialize application-wide settings
     */
    private fun initializeAppSettings() {
        // Apply preferred language settings
        LocaleHelper.applyPreferredLanguage(this)
        
        // Enable vector drawables compatibility
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }
} 