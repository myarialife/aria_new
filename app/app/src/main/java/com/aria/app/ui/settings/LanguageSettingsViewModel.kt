package com.aria.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aria.app.utils.LocaleHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for language settings screen
 */
@HiltViewModel
class LanguageSettingsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    
    private val _selectedLanguage = MutableStateFlow("")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()
    
    init {
        viewModelScope.launch {
            val currentLanguage = LocaleHelper.getSelectedLanguage(getApplication())
            _selectedLanguage.value = currentLanguage
        }
    }
    
    /**
     * Sets the application language
     * @param languageCode ISO language code (e.g., "en", "fr", "zh")
     */
    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            _selectedLanguage.value = languageCode
            LocaleHelper.setLocale(getApplication(), languageCode)
        }
    }
} 