package com.aria.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aria.app.R
import com.aria.app.utils.LocaleHelper

/**
 * Settings screen that contains application settings and preferences
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToAI: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Language settings
                ListItem(
                    headlineContent = { 
                        Text(stringResource(R.string.preferred_language)) 
                    },
                    supportingContent = {
                        val currentLang = LocaleHelper.getSelectedLanguage(context)
                        Text(LocaleHelper.SUPPORTED_LANGUAGES[currentLang] ?: "English")
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToLanguage)
                )
                
                Divider()
                
                // AI model settings
                ListItem(
                    headlineContent = { 
                        Text(stringResource(R.string.ai_settings)) 
                    },
                    supportingContent = {
                        Text(stringResource(R.string.select_model))
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToAI)
                )
                
                Divider()
                
                // Privacy settings
                ListItem(
                    headlineContent = { 
                        Text(stringResource(R.string.privacy_settings)) 
                    },
                    supportingContent = {
                        Text(stringResource(R.string.data_collection))
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToPrivacy)
                )
                
                Divider()
                
                // Data isolation toggle
                ListItem(
                    headlineContent = { 
                        Text(stringResource(R.string.data_isolation_enabled)) 
                    },
                    supportingContent = {
                        Text("Isolate and encrypt all user data")
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = settings.dataIsolationEnabled,
                            onCheckedChange = { viewModel.toggleDataIsolation() }
                        )
                    }
                )
                
                Divider()
                
                // Notifications toggle
                ListItem(
                    headlineContent = { 
                        Text("Notifications") 
                    },
                    supportingContent = {
                        Text("Enable push notifications")
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = settings.notificationsEnabled,
                            onCheckedChange = { viewModel.toggleNotifications() }
                        )
                    }
                )
                
                Divider()
                
                // App info section
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "ARIA",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Version 1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "Build: December 15, 2024",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
} 