package com.aria.assistant.network.models

import com.google.gson.annotations.SerializedName

/**
 * Generic API Response Class
 */
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null
)

/**
 * User Statistics Response
 */
data class UserStatsResponse(
    @SerializedName("totalRewards") val totalRewards: Double,
    @SerializedName("dataCollected") val dataCollected: Int,
    @SerializedName("dataProcessed") val dataProcessed: Int,
    @SerializedName("tokenBalance") val tokenBalance: Double
)

/**
 * Assistant Response
 */
data class AssistantResponse(
    @SerializedName("message") val message: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("conversationId") val conversationId: String?
)

/**
 * Collected Data Response
 */
data class CollectedDataResponse(
    @SerializedName("dataId") val dataId: String,
    @SerializedName("reward") val reward: Double,
    @SerializedName("timestamp") val timestamp: Long
)

/**
 * Wallet Information Response
 */
data class WalletInfoResponse(
    @SerializedName("address") val address: String,
    @SerializedName("balance") val balance: Double,
    @SerializedName("transactions") val transactions: List<TransactionInfo>? = null
)

/**
 * Transaction Information
 */
data class TransactionInfo(
    @SerializedName("tx_id") val txId: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("type") val type: String,
    @SerializedName("status") val status: String,
    @SerializedName("from_address") val fromAddress: String? = null,
    @SerializedName("to_address") val toAddress: String? = null
)

/**
 * User Information Response
 */
data class UserInfoResponse(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("walletAddress") val walletAddress: String?,
    @SerializedName("dataCollectionEnabled") val dataCollectionEnabled: Boolean,
    @SerializedName("dataPermissions") val dataPermissions: Map<String, Boolean>,
    @SerializedName("createdAt") val createdAt: Long,
    @SerializedName("lastLogin") val lastLogin: Long
)

/**
 * User Statistics
 */
data class UserStats(
    val totalData: Int,
    val totalRewards: Double,
    val level: Int,
    val points: Int,
    val streak: Int
)

/**
 * Assistant Response Data
 */
data class AssistantResponseData(
    val response: String,
    val sources: List<Source>? = null
)

/**
 * Source
 */
data class Source(
    val title: String,
    val url: String? = null,
    val confidence: Double
)

/**
 * Collected Data Result
 */
data class CollectedDataResult(
    val syncedData: List<SyncedDataItem>,
    val totalSynced: Int,
    val totalRewards: Double
)

/**
 * Synced Data Item
 */
data class SyncedDataItem(
    val id: Long,
    val reward: Double
)

/**
 * Login Request
 */
data class LoginRequest(
    val username: String,
    val password: String
)

/**
 * Register Request
 */
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

/**
 * Data Permission Request
 */
data class DataPermissionRequest(
    val permissionType: String,
    val enabled: Boolean
)

/**
 * Submit Data Request
 */
data class SubmitDataRequest(
    val dataItems: List<DataItem>
) {
    data class DataItem(
        val id: Long,
        val type: String,
        val content: String,
        val timestamp: Long
    )
}

/**
 * Token Reward Request
 */
data class TokenRewardRequest(
    val walletAddress: String
) 