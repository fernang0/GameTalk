package com.example.gametalk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gametalk.model.data.dto.TopicDTO
import com.example.gametalk.model.data.repository.ApiRepository
import com.example.gametalk.model.domain.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel de ejemplo para gestionar Topics desde la API REST
 * 
 * USO:
 * val viewModel: ApiTopicViewModel by viewModels()
 * 
 * // En tu Composable:
 * val topicsState by viewModel.topicsState.collectAsState()
 * 
 * when (topicsState) {
 *     is Resource.Loading -> CircularProgressIndicator()
 *     is Resource.Success -> LazyColumn { ... }
 *     is Resource.Error -> Text((topicsState as Resource.Error).message)
 * }
 */
class ApiTopicViewModel : ViewModel() {
    
    private val repository = ApiRepository()
    
    // State para lista de topics
    private val _topicsState = MutableStateFlow<Resource<List<TopicDTO>>>(Resource.Loading())
    val topicsState: StateFlow<Resource<List<TopicDTO>>> = _topicsState.asStateFlow()
    
    // State para un topic individual
    private val _topicState = MutableStateFlow<Resource<TopicDTO>?>(null)
    val topicState: StateFlow<Resource<TopicDTO>?> = _topicState.asStateFlow()
    
    // State para operaciones (crear, actualizar, eliminar)
    private val _operationState = MutableStateFlow<Resource<Any>?>(null)
    val operationState: StateFlow<Resource<Any>?> = _operationState.asStateFlow()
    
    /**
     * Obtiene todos los topics
     */
    fun getAllTopics() {
        viewModelScope.launch {
            repository.getAllTopics().collect { resource ->
                _topicsState.value = resource
            }
        }
    }
    
    /**
     * Obtiene topics filtrados por categoría
     */
    fun getTopicsByCategory(categoryId: Int) {
        viewModelScope.launch {
            repository.getTopicsByCategory(categoryId).collect { resource ->
                _topicsState.value = resource
            }
        }
    }
    
    /**
     * Obtiene topics de un usuario específico
     */
    fun getTopicsByUser(userId: Int) {
        viewModelScope.launch {
            repository.getTopicsByUser(userId).collect { resource ->
                _topicsState.value = resource
            }
        }
    }
    
    /**
     * Obtiene un topic por su ID
     */
    fun getTopicById(topicId: Int) {
        viewModelScope.launch {
            repository.getTopicById(topicId).collect { resource ->
                _topicState.value = resource
            }
        }
    }
    
    /**
     * Crea un nuevo topic
     */
    fun createTopic(
        categoryId: Int,
        userId: Int,
        title: String,
        description: String
    ) {
        viewModelScope.launch {
            repository.createTopic(categoryId, userId, title, description).collect { resource ->
                _operationState.value = resource
                if (resource is Resource.Success) {
                    // Recargar la lista después de crear
                    getAllTopics()
                }
            }
        }
    }
    
    /**
     * Actualiza un topic existente
     */
    fun updateTopic(
        topicId: Int,
        categoryId: Int,
        userId: Int,
        title: String,
        description: String
    ) {
        viewModelScope.launch {
            repository.updateTopic(topicId, categoryId, userId, title, description).collect { resource ->
                _operationState.value = resource
                if (resource is Resource.Success) {
                    // Recargar la lista después de actualizar
                    getAllTopics()
                }
            }
        }
    }
    
    /**
     * Elimina un topic
     */
    fun deleteTopic(topicId: Int) {
        viewModelScope.launch {
            repository.deleteTopic(topicId).collect { resource ->
                _operationState.value = resource
                if (resource is Resource.Success) {
                    // Recargar la lista después de eliminar
                    getAllTopics()
                }
            }
        }
    }
    
    /**
     * Limpia el estado de la operación
     */
    fun clearOperationState() {
        _operationState.value = null
    }
}
