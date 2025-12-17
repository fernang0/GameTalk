# 📱 Integración API REST - GameTalk

## 📋 Resumen

Se ha integrado completamente la API REST de Spring Boot en la aplicación Android GameTalk usando:
- **Retrofit** para las llamadas HTTP
- **Coroutines** para operaciones asíncronas
- **StateFlow** para manejo reactivo de estado
- **MVVM** con Repository Pattern

---

## 🗂️ Estructura de Archivos Creados

```
app/src/main/java/com/example/gametalk/
│
├── model/
│   ├── data/
│   │   ├── dto/
│   │   │   ├── UserDTO.kt           # Modelos de datos de usuario
│   │   │   ├── TopicDTO.kt          # Modelos de datos de topics
│   │   │   └── CategoryDTO.kt       # Modelo de datos de categoría
│   │   │
│   │   ├── network/
│   │   │   ├── ApiService.kt        # Interfaz con todos los endpoints
│   │   │   └── RetrofitClient.kt    # Configuración de Retrofit
│   │   │
│   │   └── repository/
│   │       └── ApiRepository.kt     # Repository con manejo de errores
│   │
│   └── domain/
│       └── Resource.kt              # Sealed class para estados de respuesta
│
├── viewmodel/
│   ├── ApiTopicViewModel.kt         # ViewModel para Topics
│   └── ApiUserViewModel.kt          # ViewModel para Users
│
└── ui/
    └── screen/
        └── ApiTopicsExampleScreen.kt  # Ejemplo de UI con la API
```

---

## 🔧 Configuración Necesaria

### 1. **Cambiar la URL del servidor**

Edita `/app/src/main/java/com/example/gametalk/model/data/network/RetrofitClient.kt`:

```kotlin
private const val BASE_URL = "http://TU_IP_AQUI:8080/"
```

**Opciones de IP:**
- **Emulador Android**: `http://10.0.2.2:8080/` (localhost de tu PC)
- **Dispositivo físico**: `http://192.168.X.X:8080/` (IP de tu PC en la red local)
- **Servidor remoto**: `http://tu-servidor.com:8080/`

### 2. **Sincronizar dependencias**

Después de agregar las dependencias en `build.gradle.kts`, ejecuta:
```bash
./gradlew clean build
```

O en Android Studio: **File > Sync Project with Gradle Files**

### 3. **Verificar permisos**

Ya se agregaron automáticamente en `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

---

## 📚 Cómo Usar la API

### **1. En tu ViewModel**

```kotlin
class MiViewModel : ViewModel() {
    private val repository = ApiRepository()
    
    private val _topicsState = MutableStateFlow<Resource<List<TopicDTO>>>(Resource.Loading())
    val topicsState: StateFlow<Resource<List<TopicDTO>>> = _topicsState.asStateFlow()
    
    fun loadTopics() {
        viewModelScope.launch {
            repository.getAllTopics().collect { resource ->
                _topicsState.value = resource
            }
        }
    }
}
```

### **2. En tu Composable**

```kotlin
@Composable
fun MiPantalla(viewModel: MiViewModel = viewModel()) {
    val topicsState by viewModel.topicsState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadTopics()
    }
    
    when (val state = topicsState) {
        is Resource.Loading -> CircularProgressIndicator()
        
        is Resource.Success -> {
            val topics = state.data ?: emptyList()
            LazyColumn {
                items(topics) { topic ->
                    Text(topic.title)
                }
            }
        }
        
        is Resource.Error -> {
            Text("Error: ${state.message}")
        }
    }
}
```

---

## 🎯 Operaciones Disponibles

### **USERS**

```kotlin
// Obtener todos los usuarios
repository.getAllUsers()

// Obtener usuario por ID
repository.getUserById(userId = 1)

// Crear usuario (registro)
repository.createUser(
    email = "user@example.com",
    password = "password123",
    username = "username"
)

// Cambiar contraseña
repository.changePassword(
    userId = 1,
    newPassword = "newPassword123"
)
```

### **TOPICS**

```kotlin
// Obtener todos los topics
repository.getAllTopics()

// Filtrar por categoría
repository.getTopicsByCategory(categoryId = 1)

// Filtrar por usuario
repository.getTopicsByUser(userId = 1)

// Obtener topic por ID (incrementa viewsCount)
repository.getTopicById(topicId = 1)

// Crear topic
repository.createTopic(
    categoryId = 1,
    userId = 1,
    title = "Mi primer topic",
    description = "Descripción del topic"
)

// Actualizar topic
repository.updateTopic(
    topicId = 1,
    categoryId = 1,
    userId = 1,
    title = "Título actualizado",
    description = "Descripción actualizada"
)

// Eliminar topic
repository.deleteTopic(topicId = 1)
```

---

## 🔍 Manejo de Errores

La clase `Resource` maneja tres estados:

```kotlin
sealed class Resource<T> {
    class Success<T>(data: T)           // ✅ Éxito
    class Error<T>(message: String)     // ❌ Error
    class Loading<T>()                  // ⏳ Cargando
}
```

**Mensajes de error personalizados:**
- **400**: "Solicitud incorrecta"
- **401**: "No autorizado"
- **404**: "No encontrado"
- **500**: "Error interno del servidor"
- **Connection errors**: "No se pudo conectar al servidor"
- **Timeout**: "Tiempo de espera agotado"

---

## 🧪 Prueba la Integración

### **Opción 1: Pantalla de ejemplo**

Navega a `ApiTopicsExampleScreen` para ver la lista de topics desde la API.

### **Opción 2: Testing manual**

1. **Verifica que tu servidor Spring Boot esté corriendo** en `http://localhost:8080`

2. **Prueba un endpoint** con curl:
```bash
curl http://localhost:8080/api/topics
```

3. **Ejecuta la app** y observa los logs de Retrofit en Logcat:
```
D/OkHttp: --> GET http://10.0.2.2:8080/api/topics
D/OkHttp: <-- 200 OK (123ms)
```

---

## 🚀 Migrar de Room a API REST

Si quieres cambiar del almacenamiento local (Room) a la API:

### **Antes (Room):**
```kotlin
class TopicViewModel(private val repository: TopicRepository) : ViewModel() {
    val topics: Flow<List<Topic>> = repository.getAllTopics()
}
```

### **Después (API REST):**
```kotlin
class TopicViewModel(private val apiRepository: ApiRepository) : ViewModel() {
    private val _topicsState = MutableStateFlow<Resource<List<TopicDTO>>>(Resource.Loading())
    val topicsState = _topicsState.asStateFlow()
    
    init {
        loadTopics()
    }
    
    fun loadTopics() {
        viewModelScope.launch {
            apiRepository.getAllTopics().collect { resource ->
                _topicsState.value = resource
            }
        }
    }
}
```

---

## ⚙️ Configuración Avanzada

### **Cambiar timeout**

En `RetrofitClient.kt`:
```kotlin
private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)  // Conexión
    .readTimeout(60, TimeUnit.SECONDS)     // Lectura
    .writeTimeout(60, TimeUnit.SECONDS)    // Escritura
    .build()
```

### **Agregar headers personalizados**

```kotlin
private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer TOKEN")
            .build()
        chain.proceed(request)
    }
    .build()
```

### **Deshabilitar logs en producción**

```kotlin
private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor.Level.BODY
    } else {
        HttpLoggingInterceptor.Level.NONE
    }
}
```

---

## 📊 Modelos de Datos (DTOs)

### **TopicDTO**
```kotlin
data class TopicDTO(
    val id: Int,
    val categoryId: Int,
    val userId: Int,
    val title: String,
    val description: String,
    val createdAt: Long,           // Timestamp en milisegundos
    val repliesCount: Int,
    val viewsCount: Int,
    val lastActivity: Long,        // Timestamp en milisegundos
    val categoryName: String,      // Nombre de la categoría
    val username: String           // Nombre del usuario
)
```

### **UserDTO**
```kotlin
data class UserDTO(
    val id: Int,
    val email: String,
    val username: String
)
```

---

## 🐛 Solución de Problemas

### **Error: "Unable to resolve host"**
- ✅ Verifica que el servidor Spring Boot esté corriendo
- ✅ Usa la IP correcta (`10.0.2.2` para emulador)
- ✅ Verifica el firewall de tu PC

### **Error: "Cleartext traffic not permitted"**
- ✅ Ya configurado en `AndroidManifest.xml` con `android:usesCleartextTraffic="true"`

### **Error de timeout**
- ✅ Aumenta el timeout en `RetrofitClient.kt`
- ✅ Verifica la velocidad de tu conexión

### **Error 404**
- ✅ Verifica que la URL base sea correcta
- ✅ Verifica que los endpoints coincidan con el backend

---

## 📖 Recursos Adicionales

- [Documentación de Retrofit](https://square.github.io/retrofit/)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [StateFlow y SharedFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)
- [MVVM Architecture](https://developer.android.com/topic/architecture)

---

## ✅ Checklist de Implementación

- [x] Dependencias agregadas en `build.gradle.kts`
- [x] DTOs creados (UserDTO, TopicDTO, CategoryDTO)
- [x] ApiService con todos los endpoints
- [x] RetrofitClient configurado
- [x] ApiRepository con manejo de errores
- [x] Resource sealed class para estados
- [x] Permisos en AndroidManifest
- [x] ViewModels de ejemplo (ApiTopicViewModel, ApiUserViewModel)
- [x] Pantalla de ejemplo (ApiTopicsExampleScreen)

---

## 🎓 Próximos Pasos Sugeridos

1. **Implementar autenticación con JWT** (si tu API lo usa)
2. **Agregar caché con Room** (híbrido: API + almacenamiento local)
3. **Implementar paginación** en las listas largas
4. **Agregar retry automático** en caso de fallos de red
5. **Usar Hilt/Dagger** para inyección de dependencias

---

**¡La integración está completa y lista para usar! 🚀**
