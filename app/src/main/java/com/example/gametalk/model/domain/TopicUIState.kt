package com.example.gametalk.model.domain

import com.example.gametalk.model.data.entities.Topic

data class TopicUIState(
    val topics: List<Topic> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val title: String = "",
    val description: String = "",
    val showCreateDialog: Boolean = false,
    // Para edición
    val editTitle: String = "",
    val editDescription: String = "",
    val editSuccess: Boolean = false
)
