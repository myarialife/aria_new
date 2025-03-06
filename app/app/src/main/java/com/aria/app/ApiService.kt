package com.aria.app

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Service for handling API communications with the ARIA backend
 */
class ApiService {
    private val TAG = "ApiService"
    
    // Backend API URL (change this to your actual backend URL)
    private val API_BASE_URL = "http://10.0.2.2:5000/api" // For emulator, use 10.0.2.2 to reference host machine
    
    // JWT token for authenticated requests
    private var authToken: String? = null
    
    /**
     * Set the authentication token for API requests
     */
    fun setAuthToken(token: String?) {
        authToken = token
    }
    
    /**
     * Send a chat message to the AI and get a response
     */
    suspend fun sendChatMessage(message: String): String = withContext(Dispatchers.IO) {
        try {
            val url = URL("$API_BASE_URL/chat/message")
            val connection = url.openConnection() as HttpURLConnection
            
            // Configure the connection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            
            // Add auth token if available
            authToken?.let {
                connection.setRequestProperty("Authorization", "Bearer $it")
            }
            
            connection.doOutput = true
            
            // Create JSON payload
            val requestBody = JSONObject().apply {
                put("message", message)
            }
            
            // Send request
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }
            
            // Parse response
            val responseCode = connection.responseCode
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
                
                // Parse JSON response
                val jsonResponse = JSONObject(response)
                
                if (jsonResponse.getBoolean("success")) {
                    return@withContext jsonResponse.getString("message")
                } else {
                    Log.e(TAG, "API error: ${jsonResponse.optString("message", "Unknown error")}")
                    return@withContext "Sorry, I encountered an error processing your request."
                }
            } else {
                Log.e(TAG, "HTTP Error: $responseCode")
                return@withContext "Sorry, I'm having trouble connecting to my server. Please try again later."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending chat message", e)
            return@withContext "Sorry, there was a technical issue. Please try again later."
        }
    }
    
    /**
     * Fetch chat history for the user
     */
    suspend fun getChatHistory(): List<ChatHistory> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$API_BASE_URL/chat/history")
            val connection = url.openConnection() as HttpURLConnection
            
            // Configure the connection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            
            // Add auth token if available
            authToken?.let {
                connection.setRequestProperty("Authorization", "Bearer $it")
            }
            
            // Parse response
            val responseCode = connection.responseCode
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
                
                // Parse JSON response
                val jsonResponse = JSONObject(response)
                
                if (jsonResponse.getBoolean("success")) {
                    val chatHistoryList = mutableListOf<ChatHistory>()
                    val dataArray = jsonResponse.getJSONArray("data")
                    
                    for (i in 0 until dataArray.length()) {
                        val historyObj = dataArray.getJSONObject(i)
                        val id = historyObj.getString("id")
                        val timestamp = historyObj.getString("timestamp")
                        val messagesArray = historyObj.getJSONArray("messages")
                        
                        val messages = mutableListOf<Message>()
                        for (j in 0 until messagesArray.length()) {
                            val messageObj = messagesArray.getJSONObject(j)
                            val sender = messageObj.getString("sender")
                            val content = messageObj.getString("content")
                            val messageTimestamp = messageObj.getString("timestamp")
                            
                            messages.add(Message(sender, content))
                        }
                        
                        chatHistoryList.add(ChatHistory(id, timestamp, messages))
                    }
                    
                    return@withContext chatHistoryList
                } else {
                    Log.e(TAG, "API error: ${jsonResponse.optString("message", "Unknown error")}")
                    return@withContext emptyList()
                }
            } else {
                Log.e(TAG, "HTTP Error: $responseCode")
                return@withContext emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching chat history", e)
            return@withContext emptyList()
        }
    }
    
    /**
     * Verify a wallet connection
     */
    suspend fun verifyWallet(publicKey: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$API_BASE_URL/auth/verify-wallet")
            val connection = url.openConnection() as HttpURLConnection
            
            // Configure the connection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true
            
            // Create JSON payload
            val requestBody = JSONObject().apply {
                put("publicKey", publicKey)
                // In a real implementation, would also include signature and message
            }
            
            // Send request
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }
            
            // Parse response
            val responseCode = connection.responseCode
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
                
                // Parse JSON response
                val jsonResponse = JSONObject(response)
                
                if (jsonResponse.getBoolean("success")) {
                    return@withContext jsonResponse.getString("token")
                } else {
                    Log.e(TAG, "API error: ${jsonResponse.optString("message", "Unknown error")}")
                    return@withContext null
                }
            } else {
                Log.e(TAG, "HTTP Error: $responseCode")
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying wallet", e)
            return@withContext null
        }
    }
}

/**
 * Data class for chat history
 */
data class ChatHistory(
    val id: String,
    val timestamp: String,
    val messages: List<Message>
) 