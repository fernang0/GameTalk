package com.example.gametalk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gametalk.model.domain.LoginUIState
import com.example.gametalk.model.domain.RegisterUIState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

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

        // Simular login (aquí irían las llamadas a tu API/BD)
        _loginState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            delay(1500) // Simular llamada de red

            // Ejemplo: validación hardcodeada
            if (state.email == "test@test.com" && state.password == "123456") {
                _loginState.update { it.copy(isLoading = false) }
                onSuccess()
            } else {
                _loginState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Credenciales incorrectas"
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

        // Simular registro
        _registerState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            delay(1500)

            // Aquí guardarías en BD o llamarías a tu API
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
        }
    }
}