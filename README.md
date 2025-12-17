# 🎮 GameTalk - Foro de Videojuegos

## 📱 Aplicación Android con Jetpack Compose + API REST

Una aplicación de foro moderna para discusiones sobre videojuegos, construida con las últimas tecnologías de Android.

---

## ✨ Características

### 🔐 **Autenticación**
- Registro de usuarios
- Login con validación
- Gestión de sesiones

### 📝 **Gestión de Topics**
- Crear topics en categorías
- Editar topics propios
- Eliminar topics
- Ver detalles con contador de vistas
- Contador de respuestas

### 🗂️ **Categorías**
- Acción
- RPG
- Estrategia
- Deportes
- Multijugador

### 🌐 **Integración API REST**
- ✅ **NUEVO:** Conexión con backend Spring Boot
- ✅ CRUD completo de topics
- ✅ Gestión de usuarios
- ✅ Sincronización en tiempo real
- ✅ Manejo robusto de errores

---

## 🏗️ Arquitectura

### **Patrón:** MVVM (Model-View-ViewModel)
### **UI:** Jetpack Compose
### **Navegación:** Navigation Compose
### **Async:** Kotlin Coroutines + StateFlow
### **Persistencia Local:** Room Database
### **API REST:** Retrofit + OkHttp
### **Serialización:** Gson

```
UI (Compose) → ViewModel → Repository → API/Room → Backend/SQLite
```

---

## 🚀 Comenzar Rápidamente

### **1. Clonar el repositorio**
```bash
git clone https://github.com/fernang0/GameTalk.git
cd GameTalk
```

### **2. Configurar el backend**

#### **Opción A: Usando API REST (Recomendado)**
1. Inicia tu servidor Spring Boot en puerto 8080
2. Configura la IP en `RetrofitClient.kt`:
```kotlin
private const val BASE_URL = "http://10.0.2.2:8080/" // Emulador
// o
private const val BASE_URL = "http://192.168.X.X:8080/" // Dispositivo físico
```

#### **Opción B: Modo local (Solo Room)**
La app funcionará con almacenamiento local SQLite sin necesidad de servidor.

### **3. Sincronizar dependencias**
```bash
./gradlew clean build
```
O en Android Studio: `File > Sync Project with Gradle Files`

### **4. Ejecutar**
```
Click en Run ▶️ o presiona Shift+F10
```

---

## 📚 Documentación Completa

### **🚀 Para Empezar**
- **[QUICK_START_GUIDE.md](QUICK_START_GUIDE.md)** - Guía de inicio rápido (5 min)
- **[DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md)** - Índice de toda la documentación

### **📖 Para Aprender**
- **[API_INTEGRATION_README.md](API_INTEGRATION_README.md)** - Documentación completa de la API
- **[ARCHITECTURE_DIAGRAM.md](ARCHITECTURE_DIAGRAM.md)** - Diagramas de arquitectura
- **[INTEGRATION_SUMMARY.md](INTEGRATION_SUMMARY.md)** - Resumen ejecutivo

### **🔄 Para Migrar**
- **[MIGRATION_GUIDE.md](MIGRATION_GUIDE.md)** - Guía de migración Room → API

### **💡 Para Desarrollar**
- **[CODE_SNIPPETS_API.kt](CODE_SNIPPETS_API.kt)** - 12 snippets de código reutilizables

---

## 🛠️ Tecnologías Utilizadas

### **Frontend (Android)**
- Kotlin 1.9+
- Jetpack Compose 1.5.4
- Material Design 3
- Navigation Compose 2.8.0
- Lifecycle ViewModel 2.7.0
- StateFlow & Coroutines 1.7.3

### **Persistencia**
- Room Database 2.6.1
- Retrofit 2.9.0
- Gson Converter 2.9.0
- OkHttp 4.12.0

### **Backend (Spring Boot)**
- API REST con endpoints de Users y Topics
- Base de datos relacional (PostgreSQL/MySQL/H2)

---

## 📦 Estructura del Proyecto

```
app/src/main/java/com/example/gametalk/
│
├── model/
│   ├── data/
│   │   ├── dto/              # DTOs para la API REST
│   │   ├── entities/         # Room entities
│   │   ├── dao/              # Room DAOs
│   │   ├── network/          # Retrofit + ApiService
│   │   └── repository/       # Repositories (API + Room)
│   │
│   └── domain/
│       ├── Resource.kt       # Sealed class para estados
│       └── UIStates/         # Estados de UI
│
├── viewmodel/                # ViewModels (MVVM)
│   ├── LoginViewModel.kt
│   ├── CategoryViewModel.kt
│   ├── TopicViewModel.kt
│   ├── ApiTopicViewModel.kt  # ViewModel para API REST
│   └── ApiUserViewModel.kt   # ViewModel para API REST
│
├── ui/
│   ├── screen/               # Pantallas (Composables)
│   │   ├── LoginScreen.kt
│   │   ├── RegisterScreen.kt
│   │   ├── HomeScreen.kt
│   │   ├── CategoriesScreen.kt
│   │   ├── TopicsScreen.kt
│   │   ├── EditTopicScreen.kt
│   │   ├── ApiTopicsExampleScreen.kt    # Ejemplo con API
│   │   └── TopicFormApiScreens.kt       # Formularios con API
│   │
│   └── components/           # Componentes reutilizables
│       └── InputText.kt
│
├── navigation/
│   └── AppNav.kt             # Navegación de la app
│
└── MainActivity.kt           # Punto de entrada
```

---

## 🎯 Endpoints de la API

### **Users** (`/api/users`)
```
GET    /api/users              - Obtener todos los usuarios
GET    /api/users/{id}         - Obtener usuario por ID
POST   /api/users              - Crear usuario (registro)
PATCH  /api/users/{id}/password - Cambiar contraseña
```

### **Topics** (`/api/topics`)
```
GET    /api/topics                    - Obtener todos los topics
GET    /api/topics?categoryId={id}    - Filtrar por categoría
GET    /api/topics?userId={id}        - Filtrar por usuario
GET    /api/topics/{id}               - Obtener topic (incrementa vistas)
POST   /api/topics                    - Crear topic
PUT    /api/topics/{id}               - Actualizar topic
DELETE /api/topics/{id}               - Eliminar topic
```

---

## 💻 Ejemplos de Código

### **Obtener topics desde la API**
```kotlin
val viewModel: ApiTopicViewModel by viewModels()
val topicsState by viewModel.topicsState.collectAsState()

LaunchedEffect(Unit) {
    viewModel.getAllTopics()
}

when (val state = topicsState) {
    is Resource.Loading -> CircularProgressIndicator()
    is Resource.Success -> LazyColumn { items(state.data) { ... } }
    is Resource.Error -> Text("Error: ${state.message}")
}
```

### **Crear un topic**
```kotlin
viewModel.createTopic(
    categoryId = 1,
    userId = currentUserId,
    title = "Mi primer topic",
    description = "Descripción del topic"
)
```

---

## 🧪 Testing

### **Testing Manual**
1. Registro de usuario nuevo
2. Login con credenciales
3. Navegar por categorías
4. Crear un topic
5. Editar el topic
6. Eliminar el topic
7. Verificar contador de vistas

### **Verificar API**
```bash
# Test con curl
curl http://localhost:8080/api/topics

# O en navegador
http://localhost:8080/api/topics
```

---

## 🐛 Solución de Problemas

### **Error: "Unable to resolve host"**
- Verifica que el servidor Spring Boot esté corriendo
- Usa `10.0.2.2` para emulador (no `localhost`)
- Usa la IP local de tu PC para dispositivo físico

### **Error: "Cleartext traffic not permitted"**
- Ya configurado en `AndroidManifest.xml` con `usesCleartextTraffic="true"`

### **App muy lenta**
- Considera implementar sistema híbrido (API + Room como caché)
- Ver [MIGRATION_GUIDE.md](MIGRATION_GUIDE.md)

### **Más soluciones**
- Ver [QUICK_START_GUIDE.md](QUICK_START_GUIDE.md) → Troubleshooting
- Ver [API_INTEGRATION_README.md](API_INTEGRATION_README.md) → Solución de Problemas

---

## 📊 Estado del Proyecto

### ✅ **Completado**
- [x] UI con Jetpack Compose
- [x] Navegación con drawer y rutas
- [x] Autenticación local
- [x] CRUD de topics (local)
- [x] Room Database
- [x] ViewModels con StateFlow
- [x] **Integración API REST completa**
- [x] **Repository pattern**
- [x] **Manejo de errores robusto**
- [x] **Documentación exhaustiva**

### 🚧 **En Desarrollo / Futuro**
- [ ] Autenticación JWT con la API
- [ ] Sistema de replies/comentarios
- [ ] WebSockets para notificaciones
- [ ] Imágenes de perfil
- [ ] Sistema de likes/favoritos
- [ ] Búsqueda avanzada
- [ ] Filtros y ordenamiento
- [ ] Dark mode

---

## 🤝 Contribuir

1. Fork el proyecto
2. Crea tu feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push al branch (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

---

## 📝 Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.

---

## 👨‍💻 Autor

**Fernando Henríquez**
- GitHub: [@fernang0](https://github.com/fernang0)
- Repositorio: [GameTalk](https://github.com/fernang0/GameTalk)

---

## 🎓 Aprendizaje

Este proyecto fue desarrollado como parte del aprendizaje de:
- Jetpack Compose
- MVVM Architecture
- Kotlin Coroutines
- REST API Integration
- Android Development Best Practices

---

## 📚 Recursos

- [Documentación de Android](https://developer.android.com/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Retrofit](https://square.github.io/retrofit/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-guide.html)
- [Material Design 3](https://m3.material.io/)

---

## 🌟 Características Destacadas

### 🎨 **UI Moderna**
- Material Design 3
- Animaciones fluidas
- Responsive design

### ⚡ **Performance**
- Coroutines para async
- StateFlow para estado reactivo
- LazyColumn para listas eficientes

### 🔐 **Seguridad**
- Validación de inputs
- Manejo seguro de contraseñas
- Permisos apropiados

### 🌐 **Conectividad**
- Manejo de errores de red
- Estados de carga
- Retry automático

---

## 📞 Soporte

¿Tienes preguntas o problemas?

1. **Lee la documentación** en [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md)
2. **Revisa los ejemplos** en [CODE_SNIPPETS_API.kt](CODE_SNIPPETS_API.kt)
3. **Abre un issue** en GitHub
4. **Contacta al autor** via GitHub

---

## 🎉 Agradecimientos

Gracias a:
- La comunidad de Android Developers
- Los creadores de Jetpack Compose
- Los mantenedores de Retrofit
- Todos los que contribuyen al ecosistema de Kotlin

---

**⭐ Si te gusta este proyecto, dale una estrella en GitHub!**

*Última actualización: Diciembre 12, 2025*  
*Versión: 2.0.0 (Con integración API REST)*  
*Estado: ✅ En producción*
