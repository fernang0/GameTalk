package com.example.gametalk.model.data.network

import com.example.gametalk.model.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // ==================== USER ENDPOINTS ====================
    
    @GET("api/users")
    suspend fun getAllUsers(): Response<List<UserDTO>>
    
    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") id: Int): Response<UserDTO>
    
    @POST("api/users")
    suspend fun createUser(@Body user: UserCreateDTO): Response<UserDTO>
    
    @PATCH("api/users/{id}/password")
    suspend fun changePassword(
        @Path("id") id: Int,
        @Body passwordChange: PasswordChangeDTO
    ): Response<Unit>
    
    // ==================== TOPIC ENDPOINTS ====================
    
    @GET("api/topics")
    suspend fun getAllTopics(): Response<List<TopicDTO>>
    
    @GET("api/topics")
    suspend fun getTopicsByCategory(
        @Query("categoryId") categoryId: Int
    ): Response<List<TopicDTO>>
    
    @GET("api/topics")
    suspend fun getTopicsByUser(
        @Query("userId") userId: Int
    ): Response<List<TopicDTO>>
    
    @GET("api/topics/{id}")
    suspend fun getTopicById(@Path("id") id: Int): Response<TopicDTO>
    
    @POST("api/topics")
    suspend fun createTopic(@Body topic: TopicCreateDTO): Response<TopicDTO>
    
    @PUT("api/topics/{id}")
    suspend fun updateTopic(
        @Path("id") id: Int,
        @Body topic: TopicUpdateDTO
    ): Response<TopicDTO>
    
    @DELETE("api/topics/{id}")
    suspend fun deleteTopic(@Path("id") id: Int): Response<Unit>
    
    // ==================== CATEGORY ENDPOINTS ====================
    // Si en el futuro agregas endpoints de categorías, los puedes añadir aquí
}
