package com.example.gametalk.model.domain

import com.example.gametalk.model.data.entities.Category

data class CategoryUIState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
