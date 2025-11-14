package com.example.gametalk.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gametalk.ui.components.InputText
import com.example.gametalk.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.registerState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro") },
                navigationIcon = {
                    IconButton(onClick = onRegisterSuccess) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Crear Cuenta",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Username
                    InputText(
                        valor = state.username,
                        label = "Nombre de Usuario",
                        onChange = viewModel::onUsernameChange,
                        error = state.usernameError,
                        enabled = !state.isLoading
                    )

                    // Email
                    InputText(
                        valor = state.email,
                        label = "Email",
                        onChange = viewModel::onRegisterEmailChange,
                        error = state.emailError,
                        keyboardType = KeyboardType.Email,
                        enabled = !state.isLoading
                    )

                    // Password
                    InputText(
                        valor = state.password,
                        label = "Contraseña",
                        onChange = viewModel::onRegisterPasswordChange,
                        error = state.passwordError,
                        isPassword = true,
                        enabled = !state.isLoading
                    )

                    // Confirm Password
                    InputText(
                        valor = state.confirmPassword,
                        label = "Confirmar Contraseña",
                        onChange = viewModel::onConfirmPasswordChange,
                        error = state.confirmPasswordError,
                        isPassword = true,
                        enabled = !state.isLoading
                    )

                    // Error Message
                    state.errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Register Button
                    Button(
                        onClick = { viewModel.onRegister(onRegisterSuccess) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading && !state.hasErrors()
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Registrarse")
                        }
                    }
                }
            }
        }
    }
}