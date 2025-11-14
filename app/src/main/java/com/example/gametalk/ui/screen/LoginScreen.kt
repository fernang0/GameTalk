package com.example.gametalk.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gametalk.ui.components.InputText
import com.example.gametalk.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onAuthenticated: () -> Unit,
    onRegister: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.loginState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
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
                // Título
                Text(
                    text = "GameTalk",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Iniciar Sesión",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Email
                InputText(
                    valor = state.email,
                    label = "Email",
                    onChange = viewModel::onEmailChange,
                    keyboardType = KeyboardType.Email,
                    enabled = !state.isLoading
                )

                // Password
                InputText(
                    valor = state.password,
                    label = "Contraseña",
                    onChange = viewModel::onPasswordChange,
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

                // Login Button
                Button(
                    onClick = { viewModel.onLogin(onAuthenticated) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Ingresar")
                    }
                }

                // Register Link
                TextButton(
                    onClick = onRegister,
                    enabled = !state.isLoading
                ) {
                    Text("¿No tienes cuenta? Regístrate")
                }
            }
        }
    }
}