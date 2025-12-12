# 🔄 Guía de Migración: Room (Local) → API REST

## 📋 Comparación: Room vs API REST

| Aspecto | Room (Local) | API REST |
|---------|-------------|----------|
| **Almacenamiento** | SQLite en el dispositivo | Servidor remoto |
| **Datos** | Solo del dispositivo | Compartidos entre usuarios |
| **Conectividad** | No requiere internet | Requiere internet |
| **Velocidad** | Muy rápida | Depende de la red |
| **Sincronización** | No hay | En tiempo real |
| **Complejidad** | Baja | Media |

---

## 🎯 Estrategias de Migración

### **Opción 1: Reemplazo Total (API solamente)**
Eliminar Room completamente y usar solo la API REST.

**✅ Ventajas:**
- Código más simple
- Un solo origen de verdad
- Datos siempre actualizados

**❌ Desventajas:**
- No funciona offline
- Más lento si la conexión es mala

---

### **Opción 2: Híbrido (API + Room como caché)**
Usar la API como fuente principal y Room para caché offline.

**✅ Ventajas:**
- Funciona offline
- Rápido (muestra caché mientras carga)
- Mejor experiencia de usuario

**❌ Desventajas:**
- Más código
- Mayor complejidad
- Hay que gestionar sincronización

---

### **Opción 3: Migración Gradual**
Mantener ambos sistemas y migrar pantalla por pantalla.

**✅ Ventajas:**
- Menos riesgo
- Pruebas incrementales
- Rollback fácil

---

## 🔧 Migración Paso a Paso

### **PASO 1: Identificar qué migrar**

Revisa tu código actual y lista:
- ✅ Qué DAOs tienes
- ✅ Qué Repositories usan Room
- ✅ Qué ViewModels necesitan cambiar
- ✅ Qué pantallas se verán afectadas

Ejemplo en GameTalk actual:
```
✓ UserDao → ApiRepository (users endpoints)
✓ TopicDao → ApiRepository (topics endpoints)
✓ CategoryDao → Mantener local (o crear endpoints)
```

---

### **PASO 2: Crear equivalentes en API**

#### **ANTES (Room):**
```kotlin
// UserDao.kt
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    suspend fun getUserByCredentials(email: String, password: String): User?
    
    @Insert
    suspend fun insert(user: User): Long
}

// UserRepository.kt
class UserRepository(private val userDao: UserDao) {
    suspend fun loginUser(email: String, password: String): User? {
        return userDao.getUserByCredentials(email, password)
    }
    
    suspend fun registerUser(user: User): Long {
        return userDao.insert(user)
    }
}
```

#### **DESPUÉS (API REST):**
```kotlin
// ApiService.kt (ya creado)
@GET("api/users")
suspend fun getAllUsers(): Response<List<UserDTO>>

@POST("api/users")
suspend fun createUser(@Body user: UserCreateDTO): Response<UserDTO>

// ApiRepository.kt (ya creado)
class ApiRepository {
    fun createUser(email: String, password: String, username: String): Flow<Resource<UserDTO>> {
        return safeApiCall {
            apiService.createUser(UserCreateDTO(email, password, username))
        }
    }
}
```

---

### **PASO 3: Actualizar ViewModels**

#### **ANTES (LoginViewModel con Room):**
```kotlin
class LoginViewModel(private val repository: UserRepository) : ViewModel() {
    
    private val _loginState = MutableStateFlow<LoginUIState>(LoginUIState())
    val loginState: StateFlow<LoginUIState> = _loginState.asStateFlow()
    
    fun onLogin(email: String, password: String) {
        viewModelScope.launch {
            val user = repository.loginUser(email, password)
            if (user != null) {
                _loginState.update { it.copy(
                    isAuthenticated = true,
                    userId = user.id
                )}
            } else {
                _loginState.update { it.copy(
                    errorMessage = "Credenciales incorrectas"
                )}
            }
        }
    }
}
```

#### **DESPUÉS (LoginViewModel con API REST):**
```kotlin
class LoginViewModel(private val apiRepository: ApiRepository) : ViewModel() {
    
    private val _loginState = MutableStateFlow<Resource<UserDTO>?>(null)
    val loginState: StateFlow<Resource<UserDTO>?> = _loginState.asStateFlow()
    
    fun onLogin(email: String, password: String) {
        viewModelScope.launch {
            // Opción 1: Si tienes endpoint de login
            apiRepository.login(email, password).collect { resource ->
                _loginState.value = resource
            }
            
            // Opción 2: Si solo tienes getAllUsers (temporal)
            apiRepository.getAllUsers().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val user = resource.data?.find { 
                            it.email == email 
                            // Nota: password no viene en UserDTO
                        }
                        _loginState.value = if (user != null) {
                            Resource.Success(user)
                        } else {
                            Resource.Error("Credenciales incorrectas")
                        }
                    }
                    is Resource.Error -> {
                        _loginState.value = resource
                    }
                    is Resource.Loading -> {
                        _loginState.value = resource
                    }
                }
            }
        }
    }
}
```

---

### **PASO 4: Actualizar Composables**

#### **ANTES (con Room):**
```kotlin
@Composable
fun TopicsScreen(
    categoryId: Int,
    viewModel: TopicViewModel
) {
    val topics by viewModel.getTopicsByCategory(categoryId).collectAsState(initial = emptyList())
    
    LazyColumn {
        items(topics) { topic ->
            TopicCard(topic)
        }
    }
}
```

#### **DESPUÉS (con API REST):**
```kotlin
@Composable
fun TopicsScreen(
    categoryId: Int,
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
                    TopicCard(topic)
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
```

---

### **PASO 5: Actualizar Navigation**

Si estabas pasando objetos `Topic`, ahora pasarás `TopicDTO` o solo IDs:

#### **ANTES:**
```kotlin
navController.navigate("editTopic/${topic.id}/${topic.title}/${topic.description}")
```

#### **DESPUÉS (recomendado):**
```kotlin
// Solo pasar el ID, luego cargar desde API
navController.navigate("editTopic/${topic.id}")

// En EditTopicScreen:
LaunchedEffect(topicId) {
    viewModel.getTopicById(topicId)
}
```

---

## 🔄 Enfoque Híbrido (API + Room)

Si quieres lo mejor de ambos mundos:

### **TopicRepository Híbrido:**
```kotlin
class HybridTopicRepository(
    private val apiRepository: ApiRepository,
    private val topicDao: TopicDao
) {
    /**
     * Estrategia:
     * 1. Emitir datos de caché inmediatamente (Room)
     * 2. Obtener datos frescos de la API
     * 3. Actualizar caché con datos de la API
     * 4. Emitir datos actualizados
     */
    fun getTopicsByCategory(categoryId: Int): Flow<Resource<List<Topic>>> = flow {
        emit(Resource.Loading())
        
        // 1. Datos de caché (Room)
        val cachedTopics = topicDao.getTopicsByCategory(categoryId).first()
        if (cachedTopics.isNotEmpty()) {
            emit(Resource.Success(cachedTopics))
        }
        
        // 2. Datos de la API
        apiRepository.getTopicsByCategory(categoryId).collect { apiResource ->
            when (apiResource) {
                is Resource.Success -> {
                    val apiTopics = apiResource.data
                    if (apiTopics != null) {
                        // 3. Actualizar caché
                        val entities = apiTopics.map { it.toEntity() }
                        topicDao.deleteByCategory(categoryId)
                        topicDao.insertAll(entities)
                        
                        // 4. Emitir datos frescos
                        emit(Resource.Success(entities))
                    }
                }
                is Resource.Error -> {
                    // Si la API falla, mantener caché
                    if (cachedTopics.isEmpty()) {
                        emit(Resource.Error(apiResource.message ?: "Error"))
                    }
                }
                else -> {}
            }
        }
    }.flowOn(Dispatchers.IO)
}

// Extensión para convertir DTO a Entity
fun TopicDTO.toEntity(): Topic = Topic(
    id = this.id,
    categoryId = this.categoryId,
    userId = this.userId,
    title = this.title,
    description = this.description,
    createdAt = this.createdAt,
    viewsCount = this.viewsCount
)
```

---

## 🧪 Testing de la Migración

### **Checklist de pruebas:**

- [ ] **Sin internet:**
  - ¿La app muestra datos de caché?
  - ¿Muestra mensaje de error apropiado?
  - ¿No crashea?

- [ ] **Con internet lenta:**
  - ¿Muestra loading mientras carga?
  - ¿No congela la UI?
  - ¿Muestra caché primero?

- [ ] **Crear topic:**
  - ¿Se crea en la API?
  - ¿Aparece en la lista?
  - ¿Se actualiza la caché?

- [ ] **Editar topic:**
  - ¿Se actualiza en la API?
  - ¿Se refleja el cambio?
  - ¿Se actualiza la caché?

- [ ] **Eliminar topic:**
  - ¿Se elimina de la API?
  - ¿Desaparece de la lista?
  - ¿Se elimina de la caché?

---

## 🎯 Recomendaciones Finales

### **Para GameTalk actual:**

1. **Users**: Migrar completamente a API
   - Login/registro deben ser remotos
   - Guardar solo userId localmente (SharedPreferences)

2. **Topics**: Enfoque híbrido
   - API como fuente principal
   - Room como caché para offline

3. **Categories**: Mantener local
   - Son estáticas (no cambian)
   - No necesitan sincronización

### **Orden de migración sugerido:**

1. ✅ **Semana 1**: Registro y login (API)
2. ✅ **Semana 2**: Listado de topics (API + caché)
3. ✅ **Semana 3**: Crear/editar/eliminar topics (API)
4. ✅ **Semana 4**: Testing y refinamiento

---

## 🐛 Problemas Comunes y Soluciones

### **Problema 1: "Datos se cargan muy lento"**
**Solución:** Implementar caché con Room (híbrido)

### **Problema 2: "App no funciona sin internet"**
**Solución:** Agregar modo offline con Room como respaldo

### **Problema 3: "Datos desactualizados"**
**Solución:** Implementar pull-to-refresh y auto-refresh cada X minutos

### **Problema 4: "Demasiadas llamadas a la API"**
**Solución:** Implementar caché de corto plazo (5-10 minutos)

---

## 📊 Comparación de Código

### **Complejidad:**
- **Solo Room**: ⭐⭐ (Baja)
- **Solo API**: ⭐⭐⭐ (Media)
- **Híbrido**: ⭐⭐⭐⭐ (Alta)

### **Experiencia de usuario:**
- **Solo Room**: ⭐⭐ (Limitado, no compartido)
- **Solo API**: ⭐⭐⭐ (Bueno, depende de red)
- **Híbrido**: ⭐⭐⭐⭐⭐ (Excelente)

### **Mantenimiento:**
- **Solo Room**: ⭐⭐⭐⭐⭐ (Fácil)
- **Solo API**: ⭐⭐⭐⭐ (Medio)
- **Híbrido**: ⭐⭐ (Difícil)

---

## 🚀 Próximos Pasos

Una vez completada la migración:

1. **Implementar autenticación JWT**
2. **Agregar WebSockets para notificaciones en tiempo real**
3. **Implementar sistema de replies/comentarios**
4. **Agregar imágenes de perfil**
5. **Sistema de likes/favoritos**

---

**¡Migración completada! Tu app ahora usa API REST de forma profesional. 🎉**
