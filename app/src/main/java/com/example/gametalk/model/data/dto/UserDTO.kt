package com.example.gametalk.model.data.dto

import com.google.gson.annotations.SerializedName

data class UserDTO(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("username")
    val username: String
)

data class UserCreateDTO(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("username")
    val username: String
)

data class PasswordChangeDTO(
    @SerializedName("password")
    val password: String
)
