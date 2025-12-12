package com.example.gametalk.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gametalk.model.data.dto.TopicDTO
import com.example.gametalk.model.domain.Resource
import com.example.gametalk.viewmodel.ApiTopicViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * EJEMPLO DE PANTALLA usando la API REST
 * 
 * Esta pantalla muestra cómo integrar la API en tu UI con Jetpack Compose
 * 
 * CARACTERÍSTICAS:
 * - Lista todos los topics desde la API
 * - Maneja estados de carga, éxito y error
 * - Muestra información enriquecida (usuario, categoría, contador de vistas)
 * - Formato de fechas legible
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiTopicsExampleScreen(
    viewModel: ApiTopicViewModel = viewModel()
) {
    val topicsState by viewModel.topicsState.collectAsState()
    
    // Cargar topics al iniciar la pantalla
    LaunchedEffect(Unit) {
        viewModel.getAllTopics()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Topics desde API REST") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = topicsState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                is Resource.Success -> {
                    val topics = state.data ?: emptyList()
                    
                    if (topics.isEmpty()) {
                        Text(
                            text = "No hay topics disponibles",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(topics) { topic ->
                                TopicCard(topic = topic)
                            }
                        }
                    }
                }
                
                is Resource.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "❌ Error",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message ?: "Error desconocido",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.getAllTopics() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopicCard(topic: TopicDTO) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Título del topic
            Text(
                text = topic.title,
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Descripción
            Text(
                text = topic.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Información del topic
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "👤 ${topic.username}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "📁 ${topic.categoryName}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "👁 ${topic.viewsCount} vistas",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "💬 ${topic.repliesCount} respuestas",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Fecha de creación
            Text(
                text = "Creado: ${formatDate(topic.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Última actividad
            Text(
                text = "Última actividad: ${formatDate(topic.lastActivity)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Formatea timestamp (milisegundos) a fecha legible
 */
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
