// ========================================
// SNIPPETS DE CÓDIGO ÚTILES - API REST
// ========================================

// ============================================================
// 1. LOGIN CON LA API (Verificar usuario y contraseña)
// ============================================================

// En tu LoginViewModel:
class LoginViewModel : ViewModel() {
    private val apiRepository = ApiRepository()
    
    private val _loginState = MutableStateFlow<Resource<UserDTO>?>(null)
    val loginState: StateFlow<Resource<UserDTO>?> = _loginState.asStateFlow()
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            // Obtener todos los usuarios
            apiRepository.getAllUsers().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val users = resource.data ?: emptyList()
                        // Buscar usuario con email y password
                        val user = users.find { 
                            it.email == email 
                            // NOTA: La password no viene en UserDTO por seguridad
                            // Necesitas implementar un endpoint de login en el backend
                        }
                        
                        if (user != null) {
                            _loginState.value = Resource.Success(user)
                        } else {
                            _loginState.value = Resource.Error("Credenciales incorrectas")
                        }
                    }
                    is Resource.Error -> {
                        _loginState.value = Resource.Error(resource.message ?: "Error de conexión")
                    }
                    is Resource.Loading -> {
                        _loginState.value = Resource.Loading()
                    }
                }
            }
        }
    }
}

// MEJOR OPCIÓN: Agregar endpoint de login en el backend
// En ApiService.kt:
@POST("api/users/login")
suspend fun login(@Body credentials: LoginCredentials): Response<UserDTO>

// LoginCredentials.kt:
data class LoginCredentials(
    val email: String,
    val password: String
)


// ============================================================
// 2. REGISTRO DE USUARIO
// ============================================================

// En tu RegisterViewModel:
fun register(email: String, password: String, username: String) {
    viewModelScope.launch {
        apiRepository.createUser(email, password, username).collect { resource ->
            when (resource) {
                is Resource.Success -> {
                    // Usuario registrado exitosamente
                    val newUser = resource.data
                    // Guardar userId en SharedPreferences o DataStore
                    // Navegar a la pantalla principal
                }
                is Resource.Error -> {
                    // Mostrar error
                    _errorMessage.value = resource.message
                }
                is Resource.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }
}


// ============================================================
// 3. GUARDAR SESIÓN DEL USUARIO (SharedPreferences)
// ============================================================

// UserPreferences.kt
class UserPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    
    fun saveUser(userId: Int, username: String, email: String) {
        prefs.edit().apply {
            putInt("user_id", userId)
            putString("username", username)
            putString("email", email)
            putBoolean("is_logged_in", true)
            apply()
        }
    }
    
    fun getUserId(): Int = prefs.getInt("user_id", -1)
    fun getUsername(): String? = prefs.getString("username", null)
    fun isLoggedIn(): Boolean = prefs.getBoolean("is_logged_in", false)
    
    fun logout() {
        prefs.edit().clear().apply()
    }
}

// Uso:
val userPrefs = UserPreferences(context)
userPrefs.saveUser(user.id, user.username, user.email)


// ============================================================
// 4. CARGAR TOPICS AL ABRIR UNA CATEGORÍA
// ============================================================

@Composable
fun CategoryTopicsScreen(
    categoryId: Int,
    categoryName: String,
    viewModel: ApiTopicViewModel = viewModel()
) {
    val topicsState by viewModel.topicsState.collectAsState()
    
    LaunchedEffect(categoryId) {
        viewModel.getTopicsByCategory(categoryId)
    }
    
    when (val state = topicsState) {
        is Resource.Loading -> {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
        
        is Resource.Success -> {
            val topics = state.data ?: emptyList()
            LazyColumn {
                items(topics) { topic ->
                    TopicItem(
                        topic = topic,
                        onClick = { /* Navegar a detalle */ }
                    )
                }
            }
        }
        
        is Resource.Error -> {
            ErrorView(
                message = state.message ?: "Error",
                onRetry = { viewModel.getTopicsByCategory(categoryId) }
            )
        }
    }
}


// ============================================================
// 5. VER DETALLE DE UN TOPIC (e incrementar viewsCount)
// ============================================================

@Composable
fun TopicDetailScreen(
    topicId: Int,
    viewModel: ApiTopicViewModel = viewModel()
) {
    val topicState by viewModel.topicState.collectAsState()
    
    // Al abrir el detalle, se incrementa automáticamente el viewsCount
    LaunchedEffect(topicId) {
        viewModel.getTopicById(topicId)
    }
    
    when (val state = topicState) {
        is Resource.Success -> {
            val topic = state.data
            if (topic != null) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(topic.title, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Por: ${topic.username}")
                    Text("Categoría: ${topic.categoryName}")
                    Text("Vistas: ${topic.viewsCount}")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(topic.description)
                }
            }
        }
        is Resource.Error -> {
            Text("Error: ${state.message}")
        }
        is Resource.Loading -> {
            CircularProgressIndicator()
        }
        null -> {}
    }
}


// ============================================================
// 6. PULL-TO-REFRESH (Recargar topics)
// ============================================================

// Agregar dependencia en build.gradle.kts:
// implementation("com.google.accompanist:accompanist-swiperefresh:0.32.0")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicsWithRefresh(
    categoryId: Int,
    viewModel: ApiTopicViewModel = viewModel()
) {
    val topicsState by viewModel.topicsState.collectAsState()
    val isRefreshing = topicsState is Resource.Loading
    
    val pullRefreshState = rememberPullToRefreshState()
    
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.getTopicsByCategory(categoryId)
        }
    }
    
    Box(
        Modifier
            .fillMaxSize()
            .nestedScroll(pullRefreshState.nestedScrollConnection)
    ) {
        LazyColumn {
            when (val state = topicsState) {
                is Resource.Success -> {
                    items(state.data ?: emptyList()) { topic ->
                        TopicCard(topic)
                    }
                }
                else -> {}
            }
        }
        
        if (pullRefreshState.isRefreshing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}


// ============================================================
// 7. BÚSQUEDA Y FILTRADO DE TOPICS
// ============================================================

@Composable
fun SearchableTopics(
    categoryId: Int,
    viewModel: ApiTopicViewModel = viewModel()
) {
    val topicsState by viewModel.topicsState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    LaunchedEffect(categoryId) {
        viewModel.getTopicsByCategory(categoryId)
    }
    
    Column {
        // Barra de búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Buscar topics...") },
            leadingIcon = { Icon(Icons.Default.Search, null) }
        )
        
        // Lista filtrada
        when (val state = topicsState) {
            is Resource.Success -> {
                val allTopics = state.data ?: emptyList()
                val filteredTopics = if (searchQuery.isBlank()) {
                    allTopics
                } else {
                    allTopics.filter { topic ->
                        topic.title.contains(searchQuery, ignoreCase = true) ||
                        topic.description.contains(searchQuery, ignoreCase = true)
                    }
                }
                
                LazyColumn {
                    items(filteredTopics) { topic ->
                        TopicCard(topic)
                    }
                }
            }
            else -> {}
        }
    }
}


// ============================================================
// 8. VERIFICAR CONECTIVIDAD ANTES DE LLAMAR A LA API
// ============================================================

// NetworkUtils.kt
class NetworkUtils(private val context: Context) {
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

// Uso en ViewModel:
fun loadTopics() {
    if (!networkUtils.isNetworkAvailable()) {
        _topicsState.value = Resource.Error("No hay conexión a internet")
        return
    }
    
    viewModelScope.launch {
        repository.getAllTopics().collect { resource ->
            _topicsState.value = resource
        }
    }
}


// ============================================================
// 9. COMBINACIÓN DE ROOM (CACHÉ) + API REST
// ============================================================

// TopicRepository híbrido:
class HybridTopicRepository(
    private val apiRepository: ApiRepository,
    private val roomDao: TopicDao
) {
    fun getTopics(forceRefresh: Boolean = false): Flow<Resource<List<Topic>>> = flow {
        emit(Resource.Loading())
        
        if (!forceRefresh) {
            // Primero emitir datos de la caché
            val cachedTopics = roomDao.getAllTopics().first()
            if (cachedTopics.isNotEmpty()) {
                emit(Resource.Success(cachedTopics))
            }
        }
        
        // Luego obtener datos frescos de la API
        apiRepository.getAllTopics().collect { apiResource ->
            when (apiResource) {
                is Resource.Success -> {
                    val apiTopics = apiResource.data
                    if (apiTopics != null) {
                        // Guardar en Room
                        roomDao.deleteAll()
                        roomDao.insertAll(apiTopics.map { it.toEntity() })
                        
                        // Emitir datos actualizados
                        emit(Resource.Success(apiTopics.map { it.toEntity() }))
                    }
                }
                is Resource.Error -> {
                    // Si falla la API pero tenemos caché, mantener los datos de caché
                    val cachedTopics = roomDao.getAllTopics().first()
                    if (cachedTopics.isNotEmpty()) {
                        emit(Resource.Success(cachedTopics))
                    } else {
                        emit(Resource.Error(apiResource.message ?: "Error"))
                    }
                }
                else -> {}
            }
        }
    }
}


// ============================================================
// 10. CAMBIAR DINÁMICAMENTE LA URL DEL SERVIDOR
// ============================================================

// SettingsScreen.kt
@Composable
fun ServerConfigScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    var serverUrl by remember { mutableStateOf("http://10.0.2.2:8080") }
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Configuración del Servidor", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = serverUrl,
            onValueChange = { serverUrl = it },
            label = { Text("URL del servidor") },
            placeholder = { Text("http://192.168.1.100:8080") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                // Guardar en SharedPreferences
                viewModel.saveServerUrl(serverUrl)
                // Recrear ApiService con nueva URL
                RetrofitClient.setBaseUrl(serverUrl)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar y Aplicar")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "Ejemplos:\n" +
            "• Emulador: http://10.0.2.2:8080\n" +
            "• Dispositivo físico: http://192.168.1.X:8080\n" +
            "• Servidor remoto: https://miservidor.com",
            style = MaterialTheme.typography.bodySmall
        )
    }
}


// ============================================================
// 11. MOSTRAR FECHA RELATIVA (hace 2 horas, hace 3 días)
// ============================================================

fun getRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Hace un momento"
        diff < 3600_000 -> "Hace ${diff / 60_000} minutos"
        diff < 86400_000 -> "Hace ${diff / 3600_000} horas"
        diff < 604800_000 -> "Hace ${diff / 86400_000} días"
        else -> {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

// Uso:
Text("Última actividad: ${getRelativeTime(topic.lastActivity)}")


// ============================================================
// 12. ORDENAR TOPICS POR DIFERENTES CRITERIOS
// ============================================================

@Composable
fun TopicsWithSorting(
    categoryId: Int,
    viewModel: ApiTopicViewModel = viewModel()
) {
    val topicsState by viewModel.topicsState.collectAsState()
    var sortBy by remember { mutableStateOf("lastActivity") }
    
    Column {
        // Selector de ordenamiento
        Row(modifier = Modifier.padding(16.dp)) {
            FilterChip(
                selected = sortBy == "lastActivity",
                onClick = { sortBy = "lastActivity" },
                label = { Text("Recientes") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = sortBy == "views",
                onClick = { sortBy = "views" },
                label = { Text("Más vistos") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = sortBy == "replies",
                onClick = { sortBy = "replies" },
                label = { Text("Más comentados") }
            )
        }
        
        // Lista ordenada
        when (val state = topicsState) {
            is Resource.Success -> {
                val topics = state.data ?: emptyList()
                val sortedTopics = when (sortBy) {
                    "views" -> topics.sortedByDescending { it.viewsCount }
                    "replies" -> topics.sortedByDescending { it.repliesCount }
                    else -> topics.sortedByDescending { it.lastActivity }
                }
                
                LazyColumn {
                    items(sortedTopics) { topic ->
                        TopicCard(topic)
                    }
                }
            }
            else -> {}
        }
    }
}
