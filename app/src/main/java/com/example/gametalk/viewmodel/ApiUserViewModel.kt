package com.example.gametalk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gametalk.model.data.dto.UserDTO
import com.example.gametalk.model.data.repository.ApiRepository
import com.example.gametalk.model.domain.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel de ejemplo para gestionar Users desde la API REST
 * 
 * USO:
 * val viewModel: ApiUserViewModel by viewModels()
 * 
 * // Para login/registro:
 * viewModel.createUser("email@test.com", "password123", "username")
 * val userState by viewModel.userState.collectAsState()
 */
class ApiUserViewModel : ViewModel() {
    
    private val repository = ApiRepository()
    
    // State para lista de usuarios
    private val _usersState = MutableStateFlow<Resource<List<UserDTO>>>(Resource.Loading())
    val usersState: StateFlow<Resource<List<UserDTO>>> = _usersState.asStateFlow()
    
    // State para un usuario individual
    private val _userState = MutableStateFlow<Resource<UserDTO>?>(null)
    val userState: StateFlow<Resource<UserDTO>?> = _userState.asStateFlow()
    
    // State para operaciones (cambio de contraseña)
    private val _operationState = MutableStateFlow<Resource<Unit>?>(null)
    val operationState: StateFlow<Resource<Unit>?> = _operationState.asStateFlow()
    
    /**
     * Obtiene todos los usuarios
     */
    fun getAllUsers() {
        viewModelScope.launch {
            repository.getAllUsers().collect { resource ->
                _usersState.value = resource
            }
        }
    }
    
    /**
     * Obtiene un usuario por su ID
     */
    fun getUserById(userId: Int) {
        viewModelScope.launch {
            repository.getUserById(userId).collect { resource ->
                _userState.value = resource
            }
        }
    }
    
    /**
     * Crea un nuevo usuario (Registro)
     */
    fun createUser(email: String, password: String, username: String) {
        viewModelScope.launch {
            repository.createUser(email, password, username).collect { resource ->
                _userState.value = resource
            }
        }
    }
    
    /**
     * Cambia la contraseña de un usuario
     */
    fun changePassword(userId: Int, newPassword: String) {
        viewModelScope.launch {
            repository.changePassword(userId, newPassword).collect { resource ->
                _operationState.value = resource
            }
        }
    }
    
    /**
     * Limpia el estado del usuario
     */
    fun clearUserState() {
        _userState.value = null
    }
    
    /**
     * Limpia el estado de la operación
     */
    fun clearOperationState() {
        _operationState.value = null
    }
}
