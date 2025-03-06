package com.aria.assistant.network

import com.aria.assistant.network.models.ApiResponse
import com.aria.assistant.network.models.AssistantResponse
import com.aria.assistant.network.models.CollectedDataResponse
import com.aria.assistant.network.models.UserStatsResponse
import com.aria.assistant.network.models.DataPermissionRequest
import com.aria.assistant.network.models.LoginRequest
import com.aria.assistant.network.models.RegisterRequest
import com.aria.assistant.network.models.SubmitDataRequest
import com.aria.assistant.network.models.TokenRewardRequest
import com.aria.assistant.network.models.walletinfo.WalletInfoResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * ARIA API Service Interface
 */
interface AriaApiService {
    
    /**
     * User login
     */
    @POST("auth/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<ApiResponse<String>> // Returns JWT token
    
    /**
     * User registration
     */
    @POST("auth/register")
    suspend fun register(
        @Body registerRequest: RegisterRequest
    ): Response<ApiResponse<String>> // Returns JWT token
    
    /**
     * Get user profile
     */
    @GET("users/profile")
    suspend fun getUserProfile(): Response<ApiResponse<Any>>
    
    /**
     * Get user statistics
     */
    @GET("users/stats")
    suspend fun getUserStats(): Response<ApiResponse<Any>>
    
    /**
     * Submit collected data
     */
    @POST("data/submit")
    suspend fun submitCollectedData(
        @Body submitDataRequest: SubmitDataRequest
    ): Response<ApiResponse<Any>>
    
    /**
     * Update data permissions
     */
    @PUT("data/permissions")
    suspend fun updateDataPermissions(
        @Body permissionRequest: DataPermissionRequest
    ): Response<ApiResponse<Boolean>>
    
    /**
     * Get wallet information
     */
    @GET("wallet/info/{address}")
    suspend fun getWalletInfo(
        @Path("address") address: String
    ): Response<ApiResponse<WalletInfoResponse>>
    
    /**
     * Request token reward
     */
    @POST("wallet/reward")
    suspend fun requestTokenReward(
        @Body request: TokenRewardRequest
    ): Response<ApiResponse<Any>>
    
    /**
     * Get assistant response
     */
    @POST("assistant/ask")
    suspend fun askAssistant(
        @Body question: Map<String, String>
    ): Response<ApiResponse<Any>>
    
    /**
     * Get learning resources
     */
    @GET("content/learning")
    suspend fun getLearningResources(
        @Query("category") category: String? = null,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): Response<ApiResponse<Any>>
    
    companion object {
        private const val BASE_URL = "https://api.ariaassistant.com/v1/"
        
        /**
         * Create API service instance
         */
        fun create(): AriaApiService {
            val logger = HttpLoggingInterceptor().apply { 
                level = HttpLoggingInterceptor.Level.BODY 
            }
            
            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()
            
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AriaApiService::class.java)
        }
    }
} 