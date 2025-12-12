package com.example.gametalk.model.data.dto

import com.google.gson.annotations.SerializedName

data class TopicDTO(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("categoryId")
    val categoryId: Int,
    
    @SerializedName("userId")
    val userId: Int,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("createdAt")
    val createdAt: Long,
    
    @SerializedName("repliesCount")
    val repliesCount: Int,
    
    @SerializedName("viewsCount")
    val viewsCount: Int,
    
    @SerializedName("lastActivity")
    val lastActivity: Long,
    
    @SerializedName("categoryName")
    val categoryName: String,
    
    @SerializedName("username")
    val username: String
)

data class TopicCreateDTO(
    @SerializedName("categoryId")
    val categoryId: Int,
    
    @SerializedName("userId")
    val userId: Int,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String
)

data class TopicUpdateDTO(
    @SerializedName("categoryId")
    val categoryId: Int,
    
    @SerializedName("userId")
    val userId: Int,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String
)
