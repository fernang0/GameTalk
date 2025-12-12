package com.example.gametalk.model.data.repository

import com.example.gametalk.model.data.dto.*
import com.example.gametalk.model.data.network.RetrofitClient
import com.example.gametalk.model.domain.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response

class ApiRepository {
    
    private val apiService = RetrofitClient.apiService
    
    // ==================== HELPER FUNCTIONS ====================
    
    private fun <T> handleResponse(response: Response<T>): Resource<T> {
        return if (response.isSuccessful) {
            response.body()?.let {
                Resource.Success(it)
            } ?: Resource.Error("Respuesta vacía del servidor")
        } else {
            val errorMessage = when (response.code()) {
                400 -> "Solicitud incorrecta"
                401 -> "No autorizado"
                403 -> "Acceso prohibido"
                404 -> "No encontrado"
                500 -> "Error interno del servidor"
                503 -> "Servicio no disponible"
                else -> "Error: ${response.code()} - ${response.message()}"
            }
            Resource.Error(errorMessage)
        }
    }
    
    private fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Flow<Resource<T>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiCall()
            emit(handleResponse(response))
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("Unable to resolve host") == true -> 
                    "No se pudo conectar al servidor. Verifica la URL y tu conexión."
                e.message?.contains("timeout") == true -> 
                    "Tiempo de espera agotado. El servidor no responde."
                e.message?.contains("Failed to connect") == true -> 
                    "Error de conexión. Verifica que el servidor esté ejecutándose."
                else -> "Error de red: ${e.message ?: "Error desconocido"}"
            }
            emit(Resource.Error(errorMessage))
        }
    }.flowOn(Dispatchers.IO)
    
    // ==================== USER OPERATIONS ====================
    
    fun getAllUsers(): Flow<Resource<List<UserDTO>>> = safeApiCall {
        apiService.getAllUsers()
    }
    
    fun getUserById(id: Int): Flow<Resource<UserDTO>> = safeApiCall {
        apiService.getUserById(id)
    }
    
    fun createUser(email: String, password: String, username: String): Flow<Resource<UserDTO>> = safeApiCall {
        apiService.createUser(UserCreateDTO(email, password, username))
    }
    
    fun changePassword(userId: Int, newPassword: String): Flow<Resource<Unit>> = safeApiCall {
        apiService.changePassword(userId, PasswordChangeDTO(newPassword))
    }
    
    // ==================== TOPIC OPERATIONS ====================
    
    fun getAllTopics(): Flow<Resource<List<TopicDTO>>> = safeApiCall {
        apiService.getAllTopics()
    }
    
    fun getTopicsByCategory(categoryId: Int): Flow<Resource<List<TopicDTO>>> = safeApiCall {
        apiService.getTopicsByCategory(categoryId)
    }
    
    fun getTopicsByUser(userId: Int): Flow<Resource<List<TopicDTO>>> = safeApiCall {
        apiService.getTopicsByUser(userId)
    }
    
    fun getTopicById(id: Int): Flow<Resource<TopicDTO>> = safeApiCall {
        apiService.getTopicById(id)
    }
    
    fun createTopic(
        categoryId: Int,
        userId: Int,
        title: String,
        description: String
    ): Flow<Resource<TopicDTO>> = safeApiCall {
        apiService.createTopic(TopicCreateDTO(categoryId, userId, title, description))
    }
    
    fun updateTopic(
        topicId: Int,
        categoryId: Int,
        userId: Int,
        title: String,
        description: String
    ): Flow<Resource<TopicDTO>> = safeApiCall {
        apiService.updateTopic(topicId, TopicUpdateDTO(categoryId, userId, title, description))
    }
    
    fun deleteTopic(topicId: Int): Flow<Resource<Unit>> = safeApiCall {
        apiService.deleteTopic(topicId)
    }
}
