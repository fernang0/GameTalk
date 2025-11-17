package com.example.gametalk.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gametalk.model.data.config.AppDatabase
import com.example.gametalk.model.data.repository.UserRepository
import com.example.gametalk.model.domain.LoginUIState
import com.example.gametalk.model.domain.RegisterUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepository

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        userRepository = UserRepository(userDao)
    }

    // ═══════════════════════════════════════════════
    // LOGIN STATE
    // ═══════════════════════════════════════════════
    private val _loginState = MutableStateFlow(LoginUIState())
    val loginState: StateFlow<LoginUIState> = _loginState.asStateFlow()

    fun onEmailChange(email: String) {
        _loginState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _loginState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onLogin(onSuccess: () -> Unit) {
        val state = _loginState.value

        // Validación básica
        if (state.email.isBlank() || state.password.isBlank()) {
            _loginState.update {
                it.copy(errorMessage = "Email y contraseña son obligatorios")
            }
            return
        }

        _loginState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = userRepository.loginUser(state.email.trim(), state.password)

            result.onSuccess { user ->
                _loginState.update { it.copy(isLoading = false) }
                onSuccess()
            }.onFailure { ex ->
                _loginState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ex.message ?: "Error al iniciar sesión"
                    )
                }
            }
        }
    }

    // ═══════════════════════════════════════════════
    // REGISTER STATE
    // ═══════════════════════════════════════════════
    private val _registerState = MutableStateFlow(RegisterUIState())
    val registerState: StateFlow<RegisterUIState> = _registerState.asStateFlow()

    fun onUsernameChange(username: String) {
        _registerState.update {
            it.copy(
                username = username,
                usernameError = if (username.isBlank()) "El nombre es obligatorio" else null
            )
        }
    }

    fun onRegisterEmailChange(email: String) {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        _registerState.update {
            it.copy(
                email = email,
                emailError = when {
                    email.isBlank() -> "El email es obligatorio"
                    !email.matches(emailPattern.toRegex()) -> "Email inválido"
                    else -> null
                }
            )
        }
    }

    fun onRegisterPasswordChange(password: String) {
        _registerState.update {
            it.copy(
                password = password,
                passwordError = if (password.length < 6)
                    "Mínimo 6 caracteres" else null
            )
        }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _registerState.update {
            it.copy(
                confirmPassword = confirmPassword,
                confirmPasswordError = if (confirmPassword != _registerState.value.password)
                    "Las contraseñas no coinciden" else null
            )
        }
    }

    fun onRegister(onSuccess: () -> Unit) {
        val state = _registerState.value

        // Re-validar todo
        _registerState.update {
            it.copy(
                usernameError = if (it.username.isBlank()) "Obligatorio" else null,
                emailError = if (it.email.isBlank()) "Obligatorio" else null,
                passwordError = if (it.password.length < 6) "Mínimo 6 caracteres" else null,
                confirmPasswordError = if (it.confirmPassword != it.password)
                    "No coinciden" else null
            )
        }

        if (state.hasErrors()) return

        _registerState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val result = userRepository.registerUser(
                email = state.email.trim(),
                password = state.password,
                username = state.username.trim()
            )

            result.onSuccess { user ->
                _registerState.update {
                    it.copy(
                        isLoading = false,
                        username = "",
                        email = "",
                        password = "",
                        confirmPassword = ""
                    )
                }
                onSuccess()
            }.onFailure { ex ->
                _registerState.update {
                    it.copy(
                        isLoading = false,
                        emailError = ex.message ?: "Error al registrarse"
                    )
                }
            }
        }
    }
}