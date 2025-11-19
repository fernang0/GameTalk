package com.example.gametalk.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gametalk.model.data.config.AppDatabase
import com.example.gametalk.model.data.entities.Topic
import com.example.gametalk.model.data.repository.TopicRepository
import com.example.gametalk.model.domain.TopicUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TopicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TopicRepository
    private val _uiState = MutableStateFlow(TopicUIState())
    val uiState: StateFlow<TopicUIState> = _uiState.asStateFlow()

    init {
        val topicDao = AppDatabase.getDatabase(application).topicDao()
        repository = TopicRepository(topicDao)
    }

    fun loadTopicsByCategory(categoryId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.getTopicsByCategory(categoryId).collect { topics ->
                    _uiState.update {
                        it.copy(
                            topics = topics,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar los temas: ${e.message}"
                    )
                }
            }
        }
    }

    fun createTopic(categoryId: Int, userId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.title.isBlank()) {
                _uiState.update { it.copy(errorMessage = "El título no puede estar vacío") }
                return@launch
            }
            if (currentState.description.isBlank()) {
                _uiState.update { it.copy(errorMessage = "La descripción no puede estar vacía") }
                return@launch
            }

            try {
                val topic = Topic(
                    categoryId = categoryId,
                    userId = userId,
                    title = currentState.title,
                    description = currentState.description
                )
                repository.insertTopic(topic)
                _uiState.update {
                    it.copy(
                        title = "",
                        description = "",
                        showCreateDialog = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error al crear el tema: ${e.message}")
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun showCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true) }
    }

    fun hideCreateDialog() {
        _uiState.update {
            it.copy(
                showCreateDialog = false,
                title = "",
                description = "",
                errorMessage = null
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // Funciones para edición de temas
    fun loadTopicForEdit(topicId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val topic = repository.getTopicById(topicId)
                if (topic != null) {
                    _uiState.update {
                        it.copy(
                            editTitle = topic.title,
                            editDescription = topic.description,
                            isLoading = false,
                            editSuccess = false,
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No se encontró el tema"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar el tema: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateEditTitle(title: String) {
        _uiState.update { it.copy(editTitle = title) }
    }

    fun updateEditDescription(description: String) {
        _uiState.update { it.copy(editDescription = description) }
    }

    fun updateTopic(topicId: Int, userId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.editTitle.isBlank()) {
                _uiState.update { it.copy(errorMessage = "El título no puede estar vacío") }
                return@launch
            }
            if (currentState.editDescription.isBlank()) {
                _uiState.update { it.copy(errorMessage = "La descripción no puede estar vacía") }
                return@launch
            }

            try {
                val topic = repository.getTopicById(topicId)
                if (topic != null) {
                    val updatedTopic = topic.copy(
                        title = currentState.editTitle,
                        description = currentState.editDescription
                    )
                    repository.updateTopic(updatedTopic)
                    _uiState.update {
                        it.copy(
                            editSuccess = true,
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(errorMessage = "No se encontró el tema")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error al actualizar el tema: ${e.message}")
                }
            }
        }
    }

    fun deleteTopic(topicId: Int) {
        viewModelScope.launch {
            try {
                repository.deleteTopicById(topicId)
                _uiState.update {
                    it.copy(
                        editSuccess = true,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error al eliminar el tema: ${e.message}")
                }
            }
        }
    }
}
