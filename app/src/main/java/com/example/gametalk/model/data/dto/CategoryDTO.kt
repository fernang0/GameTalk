package com.example.gametalk.model.data.dto

import com.google.gson.annotations.SerializedName

data class CategoryDTO(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("icon")
    val icon: String,
    
    @SerializedName("topicsCount")
    val topicsCount: Int
)
