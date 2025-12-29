# Documentación Técnica Detallada - Sistema de Voz para Ayuda Emocional

## Índice
1. [Descripción General](#descripción-general)
2. [Arquitectura del Proyecto](#arquitectura-del-proyecto)
3. [Tecnologías y Dependencias](#tecnologías-y-dependencias)
4. [Funcionalidades Principales](#funcionalidades-principales)
5. [Estructura del Proyecto](#estructura-del-proyecto)
6. [Flujo de Navegación](#flujo-de-navegación)
7. [Componentes Clave](#componentes-clave)
8. [Base de Datos y Almacenamiento](#base-de-datos-y-almacenamiento)
9. [APIs y Servicios Externos](#apis-y-servicios-externos)
10. [Guía de Instalación y Configuración](#guía-de-instalación-y-configuración)
11. [Seguridad y Privacidad](#seguridad-y-privacidad)

---

## Descripción General

**IHC-Priv** es una aplicación móvil Android avanzada desarrollada en Kotlin que funciona como un **asistente emocional inteligente**. La aplicación permite a los usuarios mantener conversaciones por voz con un asistente empático llamado **ISAIA** (Inteligencia de Soporte Asistido por IA), diseñado para proporcionar apoyo emocional y psicológico.

### Propósito Principal
Proporcionar una herramienta de bienestar emocional que:
- Permite a los usuarios expresar sus emociones de forma natural mediante voz
- Detecta el estado emocional del usuario a través del análisis de voz
- Ofrece respuestas empáticas y personalizadas usando IA
- Realiza evaluaciones psicológicas interactivas
- Mantiene un historial de emociones para seguimiento personalizado
- Proporciona frases motivacionales contextuales

### Estado del Proyecto
Actualmente en **desarrollo activo**, con características en expansión y mejora continua.

---

## Arquitectura del Proyecto

### Patrón Arquitectónico
La aplicación sigue una arquitectura **MVVM simplificada** con:
- **Single Activity Architecture**: Una actividad principal (`MainActivity`) que hospeda múltiples fragmentos
- **Navigation Component**: Gestión de navegación mediante Navigation Graph
- **Fragment-based UI**: Cada funcionalidad principal implementada como Fragment independiente

### Estructura de Capas

```
┌─────────────────────────────────────┐
│     Capa de Presentación (UI)      │
│  (Fragments + ViewBinding)          │
├─────────────────────────────────────┤
│     Capa de Lógica de Negocio      │
│  (Gestión de Estado + Validaciones) │
├─────────────────────────────────────┤
│     Capa de Datos                   │
│  (Firebase + Local Storage)         │
├─────────────────────────────────────┤
│     Servicios Externos              │
│  (Groq API + HuggingFace)          │
└─────────────────────────────────────┘
```

### Componentes Principales

1. **MainActivity**: Actividad contenedora principal
2. **Fragments**: 15+ fragmentos especializados para cada funcionalidad
3. **Base Classes**: `BaseMenuFragment` para funcionalidad compartida
4. **Models**: Clases de datos (`ChatMessage`, `Pregunta`, `Opcion`)
5. **Firebase Integration**: Autenticación y base de datos
6. **API Clients**: OkHttp para comunicación con APIs externas

---

## Tecnologías y Dependencias

### Stack Tecnológico Principal

#### Lenguaje y Framework
- **Kotlin**: Lenguaje de programación principal
- **Android SDK**: API nivel 29-35 (Android 10 - 15)
- **Jetpack Components**: Componentes modernos de Android

#### Bibliotecas Core de Android
```gradle
androidx.core:core-ktx
androidx.appcompat:appcompat
androidx.constraintlayout:constraintlayout
androidx.activity:activity
com.google.android.material:material
```

#### Navegación
```gradle
androidx.navigation:navigation-fragment-ktx
androidx.navigation:navigation-ui-ktx
```
- **Safe Args**: Paso de datos tipado entre fragmentos

#### Firebase (Backend as a Service)
```gradle
com.google.firebase:firebase-bom (Platform)
firebase-auth: Autenticación de usuarios
firebase-firestore: Base de datos NoSQL en la nube
firebase-storage: Almacenamiento de archivos (imágenes de perfil)
```

#### Networking
```gradle
com.squareup.okhttp3:okhttp
```
- Cliente HTTP para comunicación con APIs REST
- Utilizado para Groq API (LLM) y HuggingFace (detección de emociones)

#### Procesamiento de Datos
```gradle
com.google.code.gson:gson
```
- Serialización/deserialización JSON
- Almacenamiento local de mensajes de chat
- Lectura de preguntas psicológicas desde assets

#### Manejo de Imágenes
```gradle
com.github.bumptech.glide:glide
com.github.bumptech.glide:compiler (kapt)
de.hdodenhof:circleimageview
```
- Glide: Carga y caché de imágenes
- CircleImageView: Vistas circulares para fotos de perfil

#### Concurrencia
```gradle
org.jetbrains.kotlinx:kotlinx-coroutines-android
androidx.lifecycle:lifecycle-runtime-ktx
```

#### Testing
```gradle
junit:junit
androidx.test.ext:junit
androidx.test.espresso:espresso-core
```

### Características del Build

```gradle
compileSdk = 35
minSdk = 29
targetSdk = 35

Java Version: 11
Kotlin JVM Target: 11

BuildFeatures:
  - ViewBinding: Enabled
  - BuildConfig: Enabled

Plugins:
  - android.application
  - kotlin-android
  - androidx.navigation.safeargs
  - google.services
  - kotlin-kapt
```

---

## Funcionalidades Principales

### 1. Sistema de Autenticación

**Fragments**: `LoginFragment`, `RegistroFragment`

#### Login
- Autenticación mediante Firebase Authentication
- Soporte para email/contraseña
- Validación de campos en tiempo real
- Manejo de sesiones persistentes

#### Registro
- Creación de nuevas cuentas
- Captura de información del usuario:
  - Nombre completo
  - Email
  - Contraseña
  - Fecha de nacimiento
  - Foto de perfil (opcional)
- Almacenamiento de datos en Firestore

### 2. Chat de Voz Inteligente (ISAIA)

**Fragments**: `VozFragment`, `ChatFragment`

#### Funcionalidad de Voz
- **Reconocimiento de voz en español**: Uso de `SpeechRecognizer` de Android
- **Síntesis de voz (TTS)**: Respuestas habladas en español mexicano
- **Interfaz Push-to-Talk**: Mantener presionado para hablar
- **Feedback visual**: Indicador de estado del micrófono

#### Procesamiento con IA
```kotlin
// Flujo de procesamiento
Usuario habla → SpeechRecognizer → Texto
    ↓
Texto → Groq API (LLaMA 3) → Respuesta empática
    ↓
Respuesta → TextToSpeech → Audio hablado
    ↓
Almacenamiento → chat.json (local)
```

#### Características del Chat
- **Modelo de IA**: LLaMA 3-8B (via Groq API)
- **Personalidad**: Consejero emocional empático
- **Persistencia**: Mensajes guardados localmente en JSON
- **Historial**: Visualización de conversación completa
- **UI diferenciada**: Burbujas distintas para usuario y bot

### 3. Evaluación Psicológica Interactiva

**Fragment**: `PsicoFragment`

#### Características
- **100 preguntas** organizadas en **10 categorías**:
  1. Autoestima
  2. Ansiedad
  3. Depresión
  4. Estrés
  5. Relaciones
  6. Emociones
  7. Motivación
  8. Autocontrol
  9. Bienestar
  10. Identidad

#### Metodología
- Selección aleatoria: 3 preguntas por categoría (30 total)
- Escala Likert de 4 puntos:
  - Nunca (0 puntos)
  - A veces (1 punto)
  - Frecuentemente (2 puntos)
  - Siempre (3 puntos)

#### Análisis de Resultados
```kotlin
// Cálculo de nivel por categoría
puntaje <= 3:  "Nivel Bajo" (requiere atención)
puntaje 4-6:   "Nivel Promedio"
puntaje >= 7:  "Nivel Normal"
```

#### Almacenamiento
- Las 3 categorías con menor puntuación se guardan como "antecedentes" en Firestore
- Se usan para personalizar respuestas y frases motivacionales

### 4. Detección de Emociones por Voz

**Fragment**: `EmocionesFragment`

#### Tecnología
- **Grabación de audio**: 4 segundos en formato WAV (16kHz, mono, 16-bit)
- **Procesamiento**: Envío a 5 modelos de IA especializados vía HuggingFace

#### Modelos de Detección
```kotlin
endpoints = {
    "ingles": "/predict",
    "espanol": "/predict_es",
    "nuevo_modelo": "/predict_v3",
    "modelo_voz_propia": "/predict_v4",
    "modelo_voz_propia_lstm": "/predict_v5"
}
```

#### Emociones Detectadas
- Alegría
- Tristeza
- Enojo
- Miedo
- Sorpresa
- Neutral

#### Flujo de Procesamiento
```
Usuario presiona micrófono
    ↓
Grabación de 4 segundos (WAV)
    ↓
Envío paralelo a 5 modelos
    ↓
Agregación de resultados
    ↓
Navegación a ResultadoFragment
    ↓
Guardado en Firestore como historial
```

### 5. Historial de Emociones

**Fragment**: `HistorialFragment`

- Visualización cronológica de emociones detectadas
- Filtrado por fecha
- Estadísticas de emociones predominantes
- Gráficos de tendencias emocionales
- Sincronización con Firebase Firestore

### 6. Frases Motivacionales

**Fragment**: `FrasesFragment`, `FraseResultadoFragment`

#### Personalización
- Frases contextuales basadas en:
  - Estado emocional actual
  - Antecedentes psicológicos
  - Historial de emociones

#### Categorías
- Motivación
- Autoestima
- Manejo del estrés
- Relaciones
- Bienestar general

### 7. Perfil de Usuario

**Fragment**: `PerfilFragment`, `DatosFragment`

#### Información del Perfil
- Foto de perfil (subida a Firebase Storage)
- Nombre completo
- Email
- Fecha de nacimiento
- Antecedentes psicológicos
- Estadísticas de uso

#### Funcionalidades
- Edición de datos personales
- Cambio de foto de perfil
- Visualización de progreso emocional
- Cierre de sesión

### 8. Menú de Navegación

**Fragment**: `MenuFragment`, `BaseMenuFragment`

#### Estructura
- Menú hamburguesa accesible desde todas las pantallas
- Acceso rápido a:
  - Chat con ISAIA
  - Evaluación psicológica
  - Detección de emociones
  - Historial
  - Frases motivacionales
  - Perfil
  - Cerrar sesión

---

## Estructura del Proyecto

```
IHC-Priv/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── assets/
│   │   │   │   └── preguntas_psicologicas.json (100 preguntas)
│   │   │   ├── java/com/practica/proyectoihc/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── model/
│   │   │   │   │   ├── ChatMessage.kt
│   │   │   │   │   ├── Pregunta.kt
│   │   │   │   │   └── Opcion.kt
│   │   │   │   └── ui/
│   │   │   │       ├── base/
│   │   │   │       │   └── BaseMenuFragment.kt
│   │   │   │       ├── LoginFragment.kt
│   │   │   │       ├── RegistroFragment.kt
│   │   │   │       ├── MenuFragment.kt
│   │   │   │       ├── VozFragment.kt
│   │   │   │       ├── ChatFragment.kt
│   │   │   │       ├── PsicoFragment.kt
│   │   │   │       ├── TestFragment.kt
│   │   │   │       ├── EmocionesFragment.kt
│   │   │   │       ├── ResultadoFragment.kt
│   │   │   │       ├── HistorialFragment.kt
│   │   │   │       ├── FrasesFragment.kt
│   │   │   │       ├── FraseResultadoFragment.kt
│   │   │   │       ├── PerfilFragment.kt
│   │   │   │       └── DatosFragment.kt
│   │   │   └── res/
│   │   │       ├── layout/ (18 archivos XML)
│   │   │       ├── navigation/
│   │   │       │   └── nav_graph.xml
│   │   │       ├── values/
│   │   │       ├── drawable/
│   │   │       └── mipmap/
│   │   ├── androidTest/
│   │   └── test/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── local.properties (no versionado - contiene GROQ_API_KEY)
├── google-services.json (no versionado - config Firebase)
└── README.md
```

---

## Flujo de Navegación

### Grafo de Navegación

```
[LoginFragment]
    │
    ├─── Registro → [RegistroFragment]
    │
    └─── Login exitoso → [MenuFragment]
                             │
                             ├─── Chat → [VozFragment] → [ChatFragment]
                             │
                             ├─── Test → [TestFragment]
                             │              └─── [PsicoFragment]
                             │
                             ├─── Emociones → [EmocionesFragment]
                             │                     └─── [ResultadoFragment]
                             │
                             ├─── Historial → [HistorialFragment]
                             │
                             ├─── Frases → [FrasesFragment]
                             │                 └─── [FraseResultadoFragment]
                             │
                             └─── Perfil → [PerfilFragment]
                                              └─── Editar → [DatosFragment]
```

### Navegación con Safe Args

La aplicación utiliza el plugin Safe Args de Navigation Component para paso de datos tipado:

```kotlin
// Ejemplo: Pasar resultado de emoción
val action = EmocionesFragmentDirections
    .actionEmocionesFragmentToResultadoFragment(resultado_emocion)
findNavController().navigate(action)

// Ejemplo: Navegar al chat
val action = VozFragmentDirections.actionVozFragmentToChatFragment()
findNavController().navigate(action)
```

---

## Componentes Clave

### MainActivity.kt
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Configuración del NavHostFragment
        // Gestión del Navigation Controller
    }
    
    override fun onSupportNavigateUp(): Boolean {
        // Manejo del botón "Atrás"
    }
}
```

**Responsabilidades**:
- Alojar el NavHostFragment
- Gestionar la navegación global
- Configurar el soporte para el botón "Atrás"

### BaseMenuFragment.kt
```kotlin
abstract class BaseMenuFragment : Fragment() {
    protected fun setupMenuNavigation(menuIcon: View) {
        // Navegación al menú desde cualquier pantalla
    }
}
```

**Propósito**:
- Clase base para fragments que necesitan acceso al menú
- Evita duplicación de código
- Proporciona navegación consistente

### ChatFragment.kt

**Componentes principales**:
```kotlin
private lateinit var speechRecognizer: SpeechRecognizer
private lateinit var tts: TextToSpeech
private val mensajes: MutableList<ChatMessage>
private val GROQ_API_KEY: String
```

**Funciones clave**:
- `inicializarVoz()`: Configura SpeechRecognizer y TTS
- `iniciarEscucha()`: Comienza reconocimiento de voz
- `detenerEscucha()`: Finaliza reconocimiento
- `enviarAGroq(texto: String)`: Envía mensaje a la API de LLM
- `agregarMensaje()`: Guarda mensaje en el historial
- `guardarMensajes()`: Persiste en JSON local
- `cargarMensajes()`: Recupera historial al iniciar

**Prompt del Sistema**:
```kotlin
val prompt = """
    Eres un consejero emocional comprensivo. 
    Devuelve una respuesta hablada al usuario:
    "$texto"
    Reglas:
    - No repitas lo que dijo el usuario.
    - Sé empático, claro y natural. No exagerado.
    - Solo responde con el mensaje. Nada más.
""".trimIndent()
```

### PsicoFragment.kt

**Flujo de evaluación**:
```kotlin
1. Cargar preguntas desde assets/preguntas_psicologicas.json
2. Seleccionar 3 preguntas aleatorias por cada 10 bloques (30 total)
3. Barajar orden de presentación
4. Mostrar pregunta con RadioButtons
5. Capturar respuesta y calcular puntaje
6. Avanzar a siguiente pregunta
7. Al finalizar:
   - Calcular puntaje por categoría
   - Identificar 3 categorías más bajas
   - Guardar en Firestore como antecedentes
   - Mostrar feedback al usuario
```

### EmocionesFragment.kt

**Proceso de grabación y análisis**:
```kotlin
1. Solicitar permiso de RECORD_AUDIO
2. Configurar AudioRecord:
   - Sample Rate: 16000 Hz
   - Encoding: PCM 16-bit
   - Canal: Mono
3. Grabar durante 4 segundos
4. Convertir PCM a formato WAV con headers
5. Enviar archivo a 5 endpoints de HuggingFace
6. Recibir predicciones de emociones
7. Agregar resultados
8. Guardar en Firebase
9. Mostrar en ResultadoFragment
```

**Formato WAV**:
```kotlin
Header WAV:
- RIFF header
- Formato: PCM
- Canales: 1 (mono)
- Sample Rate: 16000 Hz
- Bits per Sample: 16
- Data chunk con audio PCM
```

---

## Base de Datos y Almacenamiento

### Firebase Firestore

#### Estructura de Colecciones

**Colección: `usuarios`**
```json
{
  "uid": "firebase_user_id",
  "nombre": "Nombre Completo",
  "email": "usuario@example.com",
  "fechaNacimiento": "1990-01-01",
  "fotoPerfil": "gs://bucket/profile_images/uid.jpg",
  "antecedente1": "Depresión",
  "antecedente2": "Ansiedad",
  "antecedente3": "Estrés",
  "fechaRegistro": Timestamp,
  "ultimaActualizacion": Timestamp
}
```

**Colección: `emociones`**
```json
{
  "uid": "firebase_user_id",
  "fecha": Timestamp,
  "emociones": {
    "ingles": { "alegria": 0.8, "tristeza": 0.1, ... },
    "espanol": { "alegria": 0.75, "tristeza": 0.15, ... },
    "nuevo_modelo": { "alegria": 0.82, "tristeza": 0.08, ... },
    "modelo_voz_propia": { ... },
    "modelo_voz_propia_lstm": { ... }
  },
  "emocionPredominante": "alegria",
  "confianza": 0.79
}
```

### Almacenamiento Local

#### Archivos JSON
**`chat.json`** (Internal Storage)
```json
[
  {
    "texto": "Hola, ¿cómo estás?",
    "esUsuario": true
  },
  {
    "texto": "Hola, estoy aquí para ayudarte...",
    "esUsuario": false
  }
]
```

#### Shared Preferences
- Estado de sesión
- Preferencias de usuario
- Tokens de autenticación

### Firebase Storage

**Estructura de almacenamiento**:
```
gs://ihc-priv.appspot.com/
└── profile_images/
    ├── uid1.jpg
    ├── uid2.jpg
    └── uid3.jpg
```

---

## APIs y Servicios Externos

### 1. Groq API (LLM)

**Endpoint**: `https://api.groq.com/openai/v1/chat/completions`

**Modelo**: LLaMA 3-8B-8192

**Autenticación**: Bearer Token (API Key en `local.properties`)

**Request**:
```json
{
  "model": "llama3-8b-8192",
  "messages": [
    {
      "role": "user",
      "content": "Prompt del sistema + mensaje del usuario"
    }
  ]
}
```

**Response**:
```json
{
  "choices": [
    {
      "message": {
        "content": "Respuesta empática del asistente"
      }
    }
  ]
}
```

**Configuración en código**:
```kotlin
val GROQ_API_KEY = BuildConfig.GROQ_API_KEY

val request = Request.Builder()
    .url("https://api.groq.com/openai/v1/chat/completions")
    .addHeader("Authorization", "Bearer $GROQ_API_KEY")
    .addHeader("Content-Type", "application/json")
    .post(json.toString().toRequestBody())
    .build()
```

### 2. HuggingFace Spaces API

**Base URL**: `https://McBikan-DeteccionEmociones.hf.space`

**Endpoints**:
- `/predict` - Modelo en inglés
- `/predict_es` - Modelo en español
- `/predict_v3` - Modelo mejorado
- `/predict_v4` - Modelo personalizado con datos propios
- `/predict_v5` - Modelo LSTM personalizado

**Request**: Multipart con archivo WAV

**Response**:
```json
{
  "emociones": {
    "alegria": 0.75,
    "tristeza": 0.10,
    "enojo": 0.05,
    "miedo": 0.05,
    "sorpresa": 0.03,
    "neutral": 0.02
  },
  "emocion_predominante": "alegria",
  "confianza": 0.75
}
```

**Implementación**:
```kotlin
val client = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .build()

val requestFile = audioFile.asRequestBody("audio/wav".toMediaTypeOrNull())

val request = Request.Builder()
    .url(endpoint_url)
    .post(requestFile)
    .addHeader("Content-Type", "audio/wav")
    .build()
```

### 3. Firebase Services

#### Firebase Authentication
- Autenticación con email/contraseña
- Gestión de sesiones
- Reset de contraseñas
- Verificación de email

#### Firebase Firestore
- Base de datos NoSQL en tiempo real
- Sincronización automática
- Consultas offline
- Seguridad con reglas

#### Firebase Storage
- Almacenamiento de imágenes de perfil
- Compresión automática
- URLs de descarga seguras

---

## Guía de Instalación y Configuración

### Requisitos Previos

1. **Android Studio**: Arctic Fox (2020.3.1) o superior
2. **JDK**: Java 11
3. **Android SDK**: API 29-35
4. **Cuenta Firebase**: Proyecto configurado
5. **API Key de Groq**: Registro en groq.com

### Pasos de Instalación

#### 1. Clonar el Repositorio
```bash
git clone https://github.com/McBikan/IHC-Priv.git
cd IHC-Priv
```

#### 2. Configurar Firebase

**Descargar `google-services.json`**:
1. Ir a Firebase Console
2. Seleccionar el proyecto
3. Ir a Project Settings
4. Descargar `google-services.json`
5. Colocar en `app/google-services.json`

**Habilitar servicios en Firebase**:
- Authentication > Email/Password
- Firestore Database
- Storage

#### 3. Configurar API Key de Groq

Crear archivo `local.properties` en la raíz del proyecto:
```properties
sdk.dir=/path/to/Android/sdk
GROQ_API_KEY=tu_api_key_aqui
```

#### 4. Sincronizar Gradle
```bash
./gradlew sync
```

#### 5. Compilar la Aplicación
```bash
./gradlew build
```

#### 6. Ejecutar en Emulador o Dispositivo
- Conectar dispositivo físico o iniciar emulador
- Run > Run 'app'

### Configuración de Permisos

**AndroidManifest.xml**:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

### Configuración de Reglas de Firestore

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /usuarios/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /emociones/{emocionId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### Configuración de Reglas de Storage

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /profile_images/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

---

## Seguridad y Privacidad

### Medidas de Seguridad Implementadas

#### 1. Autenticación
- **Firebase Authentication**: Sistema robusto de autenticación
- **No almacenamiento local de contraseñas**: Gestión por Firebase
- **Sesiones seguras**: Tokens manejados automáticamente

#### 2. Protección de API Keys
- **BuildConfig**: API keys en build.gradle (no en código)
- **local.properties**: Archivo no versionado con secrets
- **ProGuard**: Ofuscación en releases

#### 3. Permisos Granulares
- Solicitud de permisos en tiempo de ejecución
- Explicación al usuario del uso de cada permiso
- Manejo de denegación de permisos

#### 4. Comunicación Segura
- **HTTPS**: Todas las comunicaciones cifradas
- **Certificate Pinning**: Posible implementación futura
- **Timeouts**: Configurados para evitar ataques DoS

#### 5. Datos Sensibles
- **Firestore Security Rules**: Control de acceso por usuario
- **Storage Rules**: Solo el usuario puede modificar su perfil
- **Encriptación en tránsito**: Automática por Firebase
- **Encriptación en reposo**: Automática por Firebase

### Privacidad

#### Datos Recopilados
1. **Información de perfil**:
   - Nombre
   - Email
   - Fecha de nacimiento
   - Foto de perfil (opcional)

2. **Datos emocionales**:
   - Resultados de evaluaciones psicológicas
   - Emociones detectadas por voz
   - Historial de conversaciones (local)

3. **Datos de audio**:
   - Grabaciones temporales (eliminadas tras análisis)
   - No se almacenan audios en servidor

#### Uso de Datos
- **Personalización**: Mejorar respuestas y recomendaciones
- **Análisis de tendencias**: Historial emocional del usuario
- **NO compartido con terceros**: Datos privados del usuario

#### Derechos del Usuario
- **Acceso**: Ver todos sus datos
- **Modificación**: Editar información personal
- **Eliminación**: Solicitar borrado de cuenta (en desarrollo)

### Cambios en la Licencia

**⚠️ IMPORTANTE**: 

A partir del **14 de julio de 2025**, este proyecto **NO está bajo licencia MIT**.

- **Todos los derechos reservados** al autor
- **Prohibida la reutilización, copia o redistribución** sin autorización expresa
- Versiones anteriores bajo MIT siguen disponibles bajo esos términos
- Para colaboración o uso comercial, contactar al autor

---

## Mejoras Futuras Planificadas

### Funcionalidades en Desarrollo
1. **Modo Offline Completo**: Funcionamiento sin conexión
2. **Notificaciones Push**: Recordatorios de bienestar
3. **Ejercicios de Respiración**: Guías interactivas
4. **Meditaciones Guiadas**: Contenido de audio
5. **Análisis de Sentimiento en Chat**: Detección emocional en texto
6. **Gráficos Avanzados**: Visualización de progreso emocional
7. **Exportación de Datos**: PDF con historial
8. **Modo Oscuro**: Tema dark mode
9. **Multiidioma**: Soporte para más idiomas
10. **Widget de Inicio**: Acceso rápido

### Optimizaciones Técnicas
1. **Migración a Jetpack Compose**: UI moderna
2. **Room Database**: Cache local estructurado
3. **WorkManager**: Sincronización en background
4. **Hilt**: Inyección de dependencias
5. **Paging 3**: Carga eficiente de historial
6. **DataStore**: Reemplazo de SharedPreferences
7. **Testing**: Incrementar cobertura de tests

---

## Contacto y Soporte

**Desarrollador**: McBikan

**Repositorio**: [github.com/McBikan/IHC-Priv](https://github.com/McBikan/IHC-Priv)

**Contacto**: Para consultas, colaboraciones o uso del proyecto, contactar al autor a través de GitHub.

---

## Conclusión

**IHC-Priv** es una aplicación de bienestar emocional completa que combina:
- **Tecnologías modernas de Android** (Kotlin, Jetpack, Firebase)
- **Inteligencia Artificial avanzada** (LLMs, detección de emociones)
- **Diseño centrado en el usuario** (UX empática y accesible)
- **Privacidad y seguridad** (Protección de datos sensibles)

La aplicación está en constante evolución, agregando nuevas características para proporcionar mejor soporte emocional a los usuarios.

---

*Última actualización: Diciembre 2024*
