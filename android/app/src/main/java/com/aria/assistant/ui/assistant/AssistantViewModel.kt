package com.aria.assistant.ui.assistant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aria.assistant.data.entities.AssistantConversation
import com.aria.assistant.data.entities.AssistantMessage
import com.aria.assistant.data.repository.AssistantRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class AssistantViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: AssistantRepository
    
    // Current conversation ID
    var currentConversationId: Long = 0
    // Is this a new conversation
    var isNewConversation: Boolean = true
    
    // Message list
    private val _messages = MutableLiveData<List<AssistantMessage>>(emptyList())
    val messages: LiveData<List<AssistantMessage>> = _messages
    
    // Loading state
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Error message
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error
    
    init {
        val database = (application as com.aria.assistant.AriaApplication).database
        repository = AssistantRepository(database.assistantConversationDao())
        
        // Create a new conversation
        createNewConversation()
    }
    
    // Create a new conversation
    private fun createNewConversation() {
        viewModelScope.launch(Dispatchers.IO) {
            val conversation = AssistantConversation(
                title = "New conversation - ${System.currentTimeMillis()}",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            currentConversationId = repository.insertConversation(conversation)
            isNewConversation = true
            
            // Add welcome message
            val welcomeMessage = AssistantMessage(
                conversationId = currentConversationId,
                content = "Hello! I am ARIA, your AI personal assistant. How can I help you today?",
                isFromUser = false,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(welcomeMessage.copy(conversationId = currentConversationId))
            
            loadMessages()
        }
    }
    
    // Add user message
    fun sendMessage(content: String) {
        if (_isLoading.value == true) return
        
        // Save user message
        val userMessage = AssistantMessage(
            conversationId = currentConversationId,
            content = content,
            isFromUser = true,
            timestamp = System.currentTimeMillis()
        )
        addMessage(userMessage)
        
        // Call AI assistant API
        _isLoading.value = true
        viewModelScope.launch {
            try {
                delay(1500) // Simulate network delay
                
                // Mock AI response for now
                val assistantMessage = AssistantMessage(
                    conversationId = currentConversationId,
                    content = generateMockResponse(content),
                    isFromUser = false,
                    timestamp = System.currentTimeMillis()
                )
                addMessage(assistantMessage)
                
                // Update conversation timestamp
                repository.updateConversationTimestamp(currentConversationId, System.currentTimeMillis())
                
                // Actual project would call the API here
                // val response = assistantApiService.sendMessage(content)
                // addMessage(AssistantMessage(conversationId = currentConversationId, content = response.message, isFromUser = false))
                
            } catch (e: Exception) {
                _error.value = "Failed to get response: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    fun addMessage(message: AssistantMessage) {
        // Add message to in-memory list
        val currentList = _messages.value.orEmpty().toMutableList()
        currentList.add(message)
        _messages.value = currentList
        
        // Save message to database
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertMessage(message.copy(conversationId = currentConversationId))
        }
    }
    
    fun loadConversation(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val conversation = repository.getConversationWithMessages(id)
                if (conversation != null) {
                    currentConversationId = conversation.conversation.id
                    _messages.postValue(conversation.messages)
                    isNewConversation = false
                } else {
                    createNewConversation()
                }
            } catch (e: Exception) {
                _error.postValue("Failed to load conversation: ${e.message}")
                createNewConversation()
            }
        }
    }
    
    private fun generateMockResponse(input: String): String {
        return when {
            input.contains("hello", ignoreCase = true) || 
            input.contains("hi", ignoreCase = true) -> 
                "Hello! How can I assist you today?"
            
            input.contains("weather", ignoreCase = true) -> 
                "I'm sorry, I don't have access to real-time weather data yet. Would you like me to help you with something else?"
            
            input.contains("aria", ignoreCase = true) -> 
                "ARIA is a decentralized AI personal assistant that aims to help you manage your digital life while giving you control over your data."
            
            input.contains("token", ignoreCase = true) || 
            input.contains("crypto", ignoreCase = true) ->
                "ARIA tokens (ARI) are the cryptocurrency that powers the ARIA ecosystem. They're built on the Solana blockchain and can be earned by contributing data to the platform."
            
            input.contains("data", ignoreCase = true) ->
                "Your data is valuable! With ARIA, you control what data you share and get rewarded with ARIA tokens for contributing to the ecosystem."
            
            input.length < 10 ->
                "Could you please provide more details so I can assist you better?"
            
            else -> "I understand you're interested in this topic. As ARIA develops, I'll be able to provide more specific assistance. Is there anything else you'd like to know?"
        }
    }
    
    fun clearError() {
        _error.value = null
    }
} 