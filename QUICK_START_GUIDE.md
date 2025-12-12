# 🚀 Guía de Inicio Rápido - API REST Integration

## ⏱️ 5 Minutos para Empezar

---

## ✅ Paso 1: Verificar el Servidor (1 min)

### **Opción A: Verificar servidor local**
```bash
# En la terminal del servidor Spring Boot, deberías ver:
Tomcat started on port(s): 8080 (http)
```

### **Opción B: Probar con curl**
```bash
# Desde tu terminal:
curl http://localhost:8080/api/topics

# Si responde con JSON, ¡servidor OK! ✅
```

### **Opción C: Probar con navegador**
```
Abre: http://localhost:8080/api/topics
Deberías ver JSON con topics
```

---

## ✅ Paso 2: Configurar IP en la App (1 min)

### **Abrir archivo:**
```
app/src/main/java/com/example/gametalk/model/data/network/RetrofitClient.kt
```

### **Cambiar línea 16:**

#### **Para Emulador Android:**
```kotlin
private const val BASE_URL = "http://10.0.2.2:8080/"
```

#### **Para Dispositivo Físico:**
```kotlin
// Reemplaza 192.168.1.X con la IP de tu PC
private const val BASE_URL = "http://192.168.1.100:8080/"
```

#### **¿Cómo saber mi IP?**

**En Windows:**
```bash
ipconfig
# Busca "IPv4 Address" en tu adaptador de red activo
```

**En Linux/Mac:**
```bash
ifconfig
# o
ip addr show
# Busca la IP que comienza con 192.168.x.x
```

---

## ✅ Paso 3: Sincronizar Gradle (1 min)

### **En Android Studio:**
```
1. File > Sync Project with Gradle Files
2. Espera a que termine (verás "Sync finished" en la barra inferior)
```

### **O desde terminal:**
```bash
cd /home/fernang0/AndroidStudioProjects/GameTalk
./gradlew clean build
```

---

## ✅ Paso 4: Ejecutar la App (1 min)

### **Conectar dispositivo o iniciar emulador**
```
1. Conecta tu teléfono con USB debugging
   O
2. Inicia un emulador Android
```

### **Run la app**
```
Click en el botón verde "Run" (▶️)
O presiona: Shift + F10
```

---

## ✅ Paso 5: Probar la Integración (1 min)

### **Opción A: Ver pantalla de ejemplo**

Agrega la ruta en tu navegación:

```kotlin
// En AppNav.kt
composable("api_example") {
    ApiTopicsExampleScreen()
}

// Navega a esta pantalla desde cualquier parte:
navController.navigate("api_example")
```

### **Opción B: Test rápido en código**

Agrega esto en tu `MainActivity.onCreate()`:

```kotlin
val apiRepository = ApiRepository()

lifecycleScope.launch {
    apiRepository.getAllTopics().collect { resource ->
        when (resource) {
            is Resource.Loading -> Log.d("API", "Loading...")
            is Resource.Success -> {
                Log.d("API", "Success! Topics: ${resource.data?.size}")
                resource.data?.forEach { topic ->
                    Log.d("API", "Topic: ${topic.title}")
                }
            }
            is Resource.Error -> {
                Log.e("API", "Error: ${resource.message}")
            }
        }
    }
}
```

Luego abre Logcat y busca "API" para ver los logs.

---

## 🎯 Verificación de Funcionamiento

### **✅ Todo funciona si ves:**
```
Logcat:
D/OkHttp: --> GET http://10.0.2.2:8080/api/topics
D/OkHttp: <-- 200 OK (123ms)
D/API: Success! Topics: 5
D/API: Topic: Mi primer topic
```

### **❌ Hay problemas si ves:**
```
E/API: Error: Unable to resolve host
```
→ **Solución:** Verifica la IP del servidor

```
E/API: Error: Failed to connect
```
→ **Solución:** Verifica que el servidor esté corriendo

```
E/API: Error: timeout
```
→ **Solución:** Verifica tu conexión de red

---

## 🔧 Integración en tu App Existente

### **Paso 1: Inyectar ViewModel**

En tu Composable screen:

```kotlin
@Composable
fun MiPantallaExistente(
    viewModel: ApiTopicViewModel = viewModel()  // ← Agregar esto
) {
    // Tu código existente...
}
```

### **Paso 2: Cargar datos**

```kotlin
val topicsState by viewModel.topicsState.collectAsState()

LaunchedEffect(Unit) {
    viewModel.getAllTopics()  // O getTopicsByCategory(categoryId)
}
```

### **Paso 3: Renderizar según estado**

```kotlin
when (val state = topicsState) {
    is Resource.Loading -> {
        CircularProgressIndicator()
    }
    
    is Resource.Success -> {
        val topics = state.data ?: emptyList()
        LazyColumn {
            items(topics) { topic ->
                // Tu componente TopicCard existente
                TopicCard(topic)
            }
        }
    }
    
    is Resource.Error -> {
        Column {
            Text("Error: ${state.message}")
            Button(onClick = { viewModel.getAllTopics() }) {
                Text("Reintentar")
            }
        }
    }
}
```

---

## 📝 Ejemplo Completo: TopicsScreen con API

```kotlin
@Composable
fun TopicsScreen(
    categoryId: Int,
    categoryName: String,
    viewModel: ApiTopicViewModel = viewModel()
) {
    val topicsState by viewModel.topicsState.collectAsState()
    
    LaunchedEffect(categoryId) {
        viewModel.getTopicsByCategory(categoryId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(categoryName) })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                            text = "No hay topics en esta categoría",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(topics) { topic ->
                                TopicCard(
                                    topic = topic,
                                    onClick = {
                                        // Navegar a detalle
                                    }
                                )
                            }
                        }
                    }
                }
                
                is Resource.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Error: ${state.message}")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { 
                            viewModel.getTopicsByCategory(categoryId) 
                        }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopicCard(topic: TopicDTO, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = topic.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = topic.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Text("👤 ${topic.username}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(16.dp))
                Text("👁 ${topic.viewsCount}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(16.dp))
                Text("💬 ${topic.repliesCount}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
```

---

## 🎨 Adaptador para tu TopicCard Existente

Si ya tienes un `TopicCard` que usa la entity de Room:

```kotlin
// Extensión para convertir TopicDTO a tu Topic entity
fun TopicDTO.toEntity(): Topic = Topic(
    id = this.id,
    categoryId = this.categoryId,
    userId = this.userId,
    title = this.title,
    description = this.description,
    createdAt = this.createdAt,
    viewsCount = this.viewsCount
)

// Uso:
TopicCard(topic = topicDTO.toEntity())
```

---

## 🔄 Migración Gradual

### **Fase 1: Probar sin cambiar nada**
```kotlin
// Nueva pantalla para probar
composable("api_topics_test") {
    ApiTopicsExampleScreen()
}
```

### **Fase 2: Crear pantallas paralelas**
```kotlin
// Mantén tu TopicsScreen original
composable("topics_old/{categoryId}") { ... }

// Crea nueva con API
composable("topics_new/{categoryId}") { 
    NewTopicsScreenWithApi()
}
```

### **Fase 3: Reemplazar gradualmente**
```kotlin
// Cuando estés seguro, reemplaza la ruta antigua
composable("topics/{categoryId}") { 
    TopicsScreenWithApi()  // ← Usa la nueva
}
```

---

## 🐛 Troubleshooting Rápido

### **Problema: "Unable to resolve host"**
```kotlin
// 1. Verifica que el servidor esté corriendo
// 2. Usa la IP correcta en RetrofitClient.kt
// 3. Para emulador, SIEMPRE usa 10.0.2.2
```

### **Problema: "Cleartext traffic not permitted"**
```xml
<!-- AndroidManifest.xml - Ya agregado, pero verifica: -->
<application android:usesCleartextTraffic="true" ...>
```

### **Problema: App no compila**
```bash
# Limpia y reconstruye:
./gradlew clean build

# O en Android Studio:
Build > Clean Project
Build > Rebuild Project
```

### **Problema: No veo logs de Retrofit**
```
1. Abre Logcat (View > Tool Windows > Logcat)
2. Filtra por "OkHttp"
3. Verifica que el nivel sea "Debug" o "Verbose"
```

---

## 📱 Testing Manual

### **Test 1: Listar Topics**
```
1. Abre la app
2. Navega a categorías
3. Selecciona una categoría
4. Deberías ver topics desde la API
```

### **Test 2: Crear Topic**
```
1. Click en "Nuevo Topic"
2. Llena título y descripción
3. Click en "Crear"
4. Verifica que aparezca en la lista
```

### **Test 3: Ver Detalle**
```
1. Click en un topic
2. Verifica que viewsCount aumente
3. Recarga la lista
4. Verifica que el contador se actualizó
```

### **Test 4: Editar Topic**
```
1. Abre un topic
2. Click en "Editar"
3. Cambia el título
4. Guarda
5. Verifica que el cambio se refleje
```

### **Test 5: Eliminar Topic**
```
1. Abre un topic
2. Click en "Eliminar"
3. Confirma
4. Verifica que desaparezca de la lista
```

---

## 🎉 ¡Listo!

Si llegaste hasta aquí y todo funciona:

✅ **Servidor conectado**  
✅ **App sincronizada**  
✅ **Topics cargando desde API**  
✅ **CRUD completo funcionando**

---

## 📚 Siguiente Paso

Lee la documentación completa en:
- **API_INTEGRATION_README.md** → Documentación detallada
- **MIGRATION_GUIDE.md** → Migrar de Room a API
- **CODE_SNIPPETS_API.kt** → Ejemplos de código
- **ARCHITECTURE_DIAGRAM.md** → Entender la arquitectura

---

## 🆘 ¿Necesitas Ayuda?

1. **Revisa los logs** en Logcat filtrando por "API" u "OkHttp"
2. **Verifica la IP** del servidor en RetrofitClient.kt
3. **Prueba el servidor** directamente con curl o navegador
4. **Lee los mensajes de error** en Resource.Error

---

**¡Tu app está lista para usar la API REST! 🚀**

*Tiempo total de setup: ~5 minutos*
*Dificultad: ⭐⭐ (Fácil)*
