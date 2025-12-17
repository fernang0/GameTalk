package com.example.gametalk.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gametalk.model.domain.Resource
import com.example.gametalk.viewmodel.ApiTopicViewModel

/**
 * EJEMPLO: Formulario para crear un nuevo topic usando la API REST
 * 
 * Esta pantalla muestra:
 * - Formulario con validación
 * - Manejo de estado de carga
 * - Manejo de errores
 * - Feedback al usuario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTopicApiScreen(
    categoryId: Int,
    userId: Int,
    categoryName: String,
    onTopicCreated: () -> Unit,
    viewModel: ApiTopicViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val operationState by viewModel.operationState.collectAsState()
    
    // Observar el resultado de la operación
    LaunchedEffect(operationState) {
        when (val state = operationState) {
            is Resource.Success -> {
                // Topic creado exitosamente
                viewModel.clearOperationState()
                onTopicCreated()
            }
            is Resource.Error -> {
                // Mostrar error
                errorMessage = state.message ?: "Error al crear el topic"
                showError = true
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Topic en $categoryName") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = operationState !is Resource.Loading
            )
            
            // Descripción
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                maxLines = 10,
                enabled = operationState !is Resource.Loading
            )
            
            // Mensaje de error
            if (showError) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Botón de crear
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "El título no puede estar vacío"
                        showError = true
                    } else if (description.isBlank()) {
                        errorMessage = "La descripción no puede estar vacía"
                        showError = true
                    } else {
                        showError = false
                        viewModel.createTopic(
                            categoryId = categoryId,
                            userId = userId,
                            title = title,
                            description = description
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = operationState !is Resource.Loading
            ) {
                if (operationState is Resource.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (operationState is Resource.Loading) "Creando..." else "Crear Topic")
            }
        }
    }
}

/**
 * EJEMPLO: Editar un topic existente
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTopicApiScreen(
    topicId: Int,
    categoryId: Int,
    userId: Int,
    initialTitle: String,
    initialDescription: String,
    onTopicUpdated: () -> Unit,
    viewModel: ApiTopicViewModel = viewModel()
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val operationState by viewModel.operationState.collectAsState()
    
    LaunchedEffect(operationState) {
        when (operationState) {
            is Resource.Success -> {
                viewModel.clearOperationState()
                onTopicUpdated()
            }
            else -> {}
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Topic") },
            text = { Text("¿Estás seguro de que quieres eliminar este topic?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteTopic(topicId)
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Topic") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                enabled = operationState !is Resource.Loading
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                enabled = operationState !is Resource.Loading
            )
            
            // Botón actualizar
            Button(
                onClick = {
                    viewModel.updateTopic(
                        topicId = topicId,
                        categoryId = categoryId,
                        userId = userId,
                        title = title,
                        description = description
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = operationState !is Resource.Loading
            ) {
                if (operationState is Resource.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Actualizar")
            }
            
            // Botón eliminar
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                enabled = operationState !is Resource.Loading
            ) {
                Text("Eliminar Topic")
            }
        }
    }
}
