package com.aria.assistant.network.models

import com.google.gson.annotations.SerializedName

/**
 * Submit Data Request
 */
data class SubmitDataRequest(
    @SerializedName("items") val items: List<DataItem>
) {
    data class DataItem(
        @SerializedName("id") val id: Long,
        @SerializedName("type") val type: String,
        @SerializedName("content") val content: String,
        @SerializedName("timestamp") val timestamp: Long
    )
    
    data class Response(
        @SerializedName("syncedData") val syncedData: List<SyncedData>
    )
    
    data class SyncedData(
        @SerializedName("id") val id: Long,
        @SerializedName("reward") val reward: Double
    )
}

/**
 * Token Reward Request
 */
data class TokenRewardRequest(
    @SerializedName("walletAddress") val walletAddress: String
) {
    data class Response(
        @SerializedName("success") val success: Boolean,
        @SerializedName("amount") val amount: Double,
        @SerializedName("txId") val txId: String,
        @SerializedName("timestamp") val timestamp: Long,
        @SerializedName("fromAddress") val fromAddress: String
    )
}

/**
 * Data Permission Request
 */
data class DataPermissionRequest(
    @SerializedName("type") val type: String,
    @SerializedName("enabled") val enabled: Boolean
)

/**
 * Assistant Query Request
 */
data class AssistantQueryRequest(
    @SerializedName("query") val query: String,
    @SerializedName("conversationId") val conversationId: String? = null,
    @SerializedName("language") val language: String = "zh_CN"
) 