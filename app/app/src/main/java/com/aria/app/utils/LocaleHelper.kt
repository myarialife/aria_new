package com.aria.app.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Helper class for handling locale/language changes in the application
 */
class LocaleHelper {
    companion object {
        private const val PREFS_LANGUAGE = "aria_language_pref"
        private const val KEY_LANGUAGE = "selected_language"
        
        /**
         * Available languages in the application
         */
        val SUPPORTED_LANGUAGES = mapOf(
            "en" to "English",
            "es" to "Español",
            "fr" to "Français",
            "ja" to "Japanese",
            "zh" to "Chinese"
        )
        
        /**
         * Set the app's locale
         * @param context Application context
         * @param languageCode ISO language code (e.g., "en", "es", "zh")
         */
        fun setLocale(context: Context, languageCode: String) {
            // Save selected language preference
            val sharedPreferences = context.getSharedPreferences(PREFS_LANGUAGE, Context.MODE_PRIVATE)
            sharedPreferences.edit().putString(KEY_LANGUAGE, languageCode).apply()
            
            // Create locale and update configuration
            val locale = Locale(languageCode)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // For Android 13+ (API 33+), use AppCompatDelegate
                val localeList = LocaleListCompat.create(locale)
                AppCompatDelegate.setApplicationLocales(localeList)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // For Android 7.0+ (API 24-32)
                val localeList = LocaleListCompat.create(locale)
                AppCompatDelegate.setApplicationLocales(localeList)
                
                // Update resources configuration as well for compatibility
                updateResourcesLegacy(context, languageCode)
            } else {
                // For older Android versions
                updateResourcesLegacy(context, languageCode)
            }
        }
        
        /**
         * Legacy method for updating resources with the new locale
         */
        private fun updateResourcesLegacy(context: Context, languageCode: String) {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            
            val resources = context.resources
            val configuration = Configuration(resources.configuration)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                configuration.setLocale(locale)
            } else {
                @Suppress("DEPRECATION")
                configuration.locale = locale
            }
            
            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
        
        /**
         * Get the currently selected language code
         * @param context Application context
         * @return The current language code (e.g., "en", "es")
         */
        fun getSelectedLanguage(context: Context): String {
            val sharedPreferences = context.getSharedPreferences(PREFS_LANGUAGE, Context.MODE_PRIVATE)
            return sharedPreferences.getString(KEY_LANGUAGE, getDeviceLanguage()) ?: "en"
        }
        
        /**
         * Get the current device language
         * @return ISO language code of the device
         */
        fun getDeviceLanguage(): String {
            val deviceLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Locale.getDefault(Locale.Category.DISPLAY)
            } else {
                Locale.getDefault()
            }
            
            val languageCode = deviceLocale.language
            return if (SUPPORTED_LANGUAGES.containsKey(languageCode)) {
                languageCode
            } else {
                "en" // Default to English if device language not supported
            }
        }
        
        /**
         * Apply saved language preference
         * @param context Application context
         */
        fun applyPreferredLanguage(context: Context) {
            val savedLanguage = getSelectedLanguage(context)
            setLocale(context, savedLanguage)
        }
    }
} 