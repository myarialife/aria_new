package com.aria.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Data class representing application settings
 */
data class AppSettings(
    val dataIsolationEnabled: Boolean = true,
    val notificationsEnabled: Boolean = false,
    val processingOnDevice: Boolean = true,
    val selectedModelCode: String = "automatic",
    val responseLengthPreference: String = "balanced"
)

/**
 * ViewModel for application settings
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()
    
    init {
        viewModelScope.launch {
            // Load settings from preferences
            loadSettings()
        }
    }
    
    /**
     * Load settings from shared preferences
     */
    private fun loadSettings() {
        // In a real app, these would be loaded from DataStore or SharedPreferences
        // For now, we'll use default values
        _settings.value = AppSettings(
            dataIsolationEnabled = true,
            notificationsEnabled = false,
            processingOnDevice = true,
            selectedModelCode = "automatic",
            responseLengthPreference = "balanced"
        )
    }
    
    /**
     * Toggle data isolation setting
     */
    fun toggleDataIsolation() {
        _settings.update { currentSettings ->
            currentSettings.copy(
                dataIsolationEnabled = !currentSettings.dataIsolationEnabled
            )
        }
        saveSettings()
    }
    
    /**
     * Toggle notifications setting
     */
    fun toggleNotifications() {
        _settings.update { currentSettings ->
            currentSettings.copy(
                notificationsEnabled = !currentSettings.notificationsEnabled
            )
        }
        saveSettings()
    }
    
    /**
     * Set processing location preference
     * @param onDevice True to process on device, false for cloud processing
     */
    fun setProcessingLocation(onDevice: Boolean) {
        _settings.update { currentSettings ->
            currentSettings.copy(
                processingOnDevice = onDevice
            )
        }
        saveSettings()
    }
    
    /**
     * Select AI model
     * @param modelCode Code for the selected model
     */
    fun selectModel(modelCode: String) {
        _settings.update { currentSettings ->
            currentSettings.copy(
                selectedModelCode = modelCode
            )
        }
        saveSettings()
    }
    
    /**
     * Set response length preference
     * @param preference Preference code (concise, balanced, detailed)
     */
    fun setResponseLengthPreference(preference: String) {
        _settings.update { currentSettings ->
            currentSettings.copy(
                responseLengthPreference = preference
            )
        }
        saveSettings()
    }
    
    /**
     * Save settings to persistent storage
     */
    private fun saveSettings() {
        // In a real app, these would be saved to DataStore or SharedPreferences
        // This would be implemented using a Repository
    }
} 