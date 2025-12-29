# Arquitectura del Sistema - IHC-Priv

## Diagrama de Arquitectura General

```
╔══════════════════════════════════════════════════════════════════════╗
║                        APLICACIÓN ANDROID                             ║
║                          (Kotlin + Jetpack)                           ║
╠══════════════════════════════════════════════════════════════════════╣
║                                                                       ║
║  ┌─────────────────────────────────────────────────────────────┐   ║
║  │                    CAPA DE PRESENTACIÓN                      │   ║
║  │                                                               │   ║
║  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │   ║
║  │  │   Login &   │  │    Menu &   │  │  Perfil &   │         │   ║
║  │  │  Registro   │  │  Navegación │  │   Datos     │         │   ║
║  │  └─────────────┘  └─────────────┘  └─────────────┘         │   ║
║  │                                                               │   ║
║  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │   ║
║  │  │  Chat Voz   │  │ Evaluación  │  │  Detección  │         │   ║
║  │  │   (ISAIA)   │  │ Psicológica │  │  Emociones  │         │   ║
║  │  └─────────────┘  └─────────────┘  └─────────────┘         │   ║
║  │                                                               │   ║
║  │  ┌─────────────┐  ┌─────────────┐                           │   ║
║  │  │  Historial  │  │   Frases    │                           │   ║
║  │  │  Emociones  │  │Motivacionales│                          │   ║
║  │  └─────────────┘  └─────────────┘                           │   ║
║  │                                                               │   ║
║  └───────────────────────────────────────────────────────────────┘  ║
║                              │                                       ║
║                              ▼                                       ║
║  ┌─────────────────────────────────────────────────────────────┐   ║
║  │                   CAPA DE LÓGICA DE NEGOCIO                  │   ║
║  │                                                               │   ║
║  │  • Validación de datos                                       │   ║
║  │  • Procesamiento de audio (grabación, conversión WAV)        │   ║
║  │  • Reconocimiento y síntesis de voz                          │   ║
║  │  • Gestión de estado de la aplicación                        │   ║
║  │  • Cálculo de resultados psicológicos                        │   ║
║  │  • Agregación de resultados de emociones                     │   ║
║  │                                                               │   ║
║  └───────────────────────────────────────────────────────────────┘  ║
║                              │                                       ║
║                              ▼                                       ║
║  ┌─────────────────────────────────────────────────────────────┐   ║
║  │                      CAPA DE DATOS                            │   ║
║  │                                                               │   ║
║  │  ┌───────────────────┐      ┌─────────────────────┐         │   ║
║  │  │ Almacenamiento    │      │    Firebase         │         │   ║
║  │  │     Local         │      │                     │         │   ║
║  │  │                   │      │  • Authentication   │         │   ║
║  │  │  • chat.json      │      │  • Firestore        │         │   ║
║  │  │  • SharedPrefs    │      │  • Storage          │         │   ║
║  │  │  • Cache          │      │                     │         │   ║
║  │  └───────────────────┘      └─────────────────────┘         │   ║
║  │                                                               │   ║
║  └───────────────────────────────────────────────────────────────┘  ║
║                              │                                       ║
╚══════════════════════════════╪═══════════════════════════════════════╝
                               │
                               ▼
        ┌──────────────────────────────────────────────┐
        │         SERVICIOS EXTERNOS                    │
        │                                               │
        │  ┌──────────────┐  ┌─────────────────────┐  │
        │  │  Groq API    │  │  HuggingFace Spaces │  │
        │  │              │  │                     │  │
        │  │  LLaMA 3-8B  │  │  • 5 Modelos de    │  │
        │  │  (Chat IA)   │  │    Detección de     │  │
        │  │              │  │    Emociones        │  │
        │  └──────────────┘  └─────────────────────┘  │
        │                                               │
        └───────────────────────────────────────────────┘
```

## Flujo de Datos: Chat con IA

```
┌─────────────┐
│   Usuario   │
│  Presiona   │
│  Micrófono  │
└──────┬──────┘
       │
       ▼
┌─────────────────────┐
│  SpeechRecognizer   │
│  (Android API)      │
│                     │
│  • Captura voz      │
│  • Convierte a texto│
└──────┬──────────────┘
       │
       ▼ "Texto del usuario"
┌─────────────────────┐
│  ChatFragment       │
│                     │
│  agregarMensaje()   │
│  guardarMensajes()  │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐       ┌──────────────────┐
│  OkHttp Client      │──────▶│   Groq API       │
│                     │       │                  │
│  POST /chat         │       │  LLaMA 3-8B      │
│  + prompt           │       │                  │
└──────┬──────────────┘       └──────────────────┘
       │                              │
       │                              │
       │ ◀────────────────────────────┘
       │     Respuesta empática
       ▼
┌─────────────────────┐
│  ChatFragment       │
│                     │
│  • Guarda mensaje   │
│  • Actualiza UI     │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  TextToSpeech       │
│  (Android API)      │
│                     │
│  • Sintetiza voz    │
│  • Reproduce audio  │
└──────┬──────────────┘
       │
       ▼
┌─────────────┐
│   Usuario   │
│   Escucha   │
│  Respuesta  │
└─────────────┘
```

## Flujo de Datos: Detección de Emociones

```
┌─────────────┐
│   Usuario   │
│  Presiona   │
│  Micrófono  │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────┐
│  EmocionesFragment              │
│                                  │
│  1. Solicita permiso RECORD_AUDIO│
│  2. Configura AudioRecord       │
│     • 16kHz, Mono, 16-bit       │
└──────┬──────────────────────────┘
       │
       ▼
┌─────────────────────────────────┐
│  Grabación de Audio (4 seg)     │
│                                  │
│  AudioRecord.startRecording()   │
│  • Captura datos PCM            │
│  • Almacena en ByteArray        │
└──────┬──────────────────────────┘
       │
       ▼
┌─────────────────────────────────┐
│  Conversión a WAV                │
│                                  │
│  writeWavFile()                  │
│  • Añade headers WAV            │
│  • Guarda en cache/audio.wav    │
└──────┬──────────────────────────┘
       │
       ▼
       │   Envío PARALELO a 5 modelos
       ├──────────────────────────────────────┐
       │                                      │
       ▼                                      ▼
┌────────────────┐                  ┌────────────────┐
│ HF Endpoint 1  │   ...           │ HF Endpoint 5  │
│ /predict       │                  │ /predict_v5    │
│ (inglés)       │                  │ (LSTM custom)  │
└────────┬───────┘                  └────────┬───────┘
         │                                   │
         │ ◀─────────────────────────────────┘
         │   Agregación de Resultados
         ▼
┌─────────────────────────────────┐
│  Procesamiento de Respuestas     │
│                                  │
│  • Parsea JSON de cada modelo    │
│  • Combina resultados            │
│  • Calcula confianza promedio    │
└──────┬──────────────────────────┘
       │
       ▼
┌─────────────────────────────────┐
│  Firebase Firestore              │
│                                  │
│  Guarda en colección "emociones" │
│  • uid                           │
│  • timestamp                     │
│  • resultados por modelo         │
│  • emoción predominante          │
└──────┬──────────────────────────┘
       │
       ▼
┌─────────────────────────────────┐
│  ResultadoFragment               │
│                                  │
│  Muestra resultados visuales     │
│  • Gráficos circulares           │
│  • Porcentajes                   │
│  • Emoción principal             │
└──────┬──────────────────────────┘
       │
       ▼
┌─────────────┐
│   Usuario   │
│ Visualiza   │
│  Emoción    │
└─────────────┘
```

## Flujo de Datos: Evaluación Psicológica

```
┌─────────────┐
│   Usuario   │
│   Inicia    │
│  Test Psico │
└──────┬──────┘
       │
       ▼
┌───────────────────────────────────────┐
│  PsicoFragment                         │
│                                        │
│  leerPreguntasDesdeAssets()            │
│  • Lee preguntas_psicologicas.json    │
│  • 100 preguntas en 10 categorías     │
└──────┬────────────────────────────────┘
       │
       ▼
┌───────────────────────────────────────┐
│  Selección de Preguntas                │
│                                        │
│  seleccionarPreguntasDistribuidas()    │
│  • 3 preguntas aleatorias por bloque  │
│  • Total: 30 preguntas                │
│  • Orden aleatorio                    │
└──────┬────────────────────────────────┘
       │
       ▼
       │   LOOP: Para cada pregunta
       │
┌──────▼────────────────────────────────┐
│  Mostrar Pregunta                      │
│                                        │
│  mostrarPregunta()                     │
│  • Texto de pregunta                  │
│  • RadioButtons (Nunca/A veces/etc)   │
│  • ProgressBar                        │
└──────┬────────────────────────────────┘
       │
       ▼
┌───────────────────────────────────────┐
│  Usuario Selecciona Respuesta          │
│  (0 = Nunca, 1 = A veces,             │
│   2 = Frecuentemente, 3 = Siempre)    │
└──────┬────────────────────────────────┘
       │
       ▼
┌───────────────────────────────────────┐
│  Registrar Puntaje                     │
│                                        │
│  • puntajeTotal += valor              │
│  • puntajesPorBloque[bloque] += valor │
└──────┬────────────────────────────────┘
       │
       │   SI hay más preguntas: loop
       │   SI NO:
       ▼
┌───────────────────────────────────────┐
│  Calcular Resultados                   │
│                                        │
│  mostrarResultadoFinal()               │
│  • Puntaje por categoría              │
│  • Nivel: Bajo/Promedio/Normal        │
│  • Identificar 3 peores categorías    │
└──────┬────────────────────────────────┘
       │
       ▼
┌───────────────────────────────────────┐
│  Firebase Firestore                    │
│                                        │
│  usuarios/{uid}.update()               │
│  • antecedente1 (peor categoría)      │
│  • antecedente2                       │
│  • antecedente3                       │
└──────┬────────────────────────────────┘
       │
       ▼
┌───────────────────────────────────────┐
│  Mostrar Feedback                      │
│                                        │
│  • Resultados por categoría           │
│  • Interpretación textual             │
│  • Opción de reiniciar                │
└──────┬────────────────────────────────┘
       │
       ▼
┌─────────────┐
│   Usuario   │
│ Visualiza   │
│ Resultados  │
└─────────────┘
```

## Arquitectura de Navegación

```
                    ┌──────────────────┐
                    │  MainActivity    │
                    │                  │
                    │  NavHostFragment │
                    └────────┬─────────┘
                             │
            ┌────────────────┼────────────────┐
            │                │                │
            ▼                ▼                ▼
    ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
    │LoginFragment │  │MenuFragment  │  │PerfilFragment│
    └──────┬───────┘  └──────┬───────┘  └──────┬───────┘
           │                 │                  │
           │        ┌────────┼────────┐         │
           │        │        │        │         │
           ▼        ▼        ▼        ▼         ▼
    ┌──────────┐ ┌────┐  ┌────┐  ┌────┐  ┌──────────┐
    │Registro  │ │Chat│  │Test│  │Emo │  │ Datos    │
    │Fragment  │ │Frag│  │Frag│  │Frag│  │Fragment  │
    └──────────┘ └─┬──┘  └─┬──┘  └─┬──┘  └──────────┘
                   │       │       │
                   ▼       ▼       ▼
              ┌────────┐ ┌────┐ ┌────────┐
              │VozFrag │ │Psico│ │Resultado│
              └────────┘ └────┘ └────────┘
                          Frag     Frag

    BaseMenuFragment (clase base abstracta)
           ▲
           │ extends
           │
    ┌──────┴──────────────────────────┐
    │                                  │
    VozFragment                 EmocionesFragment
    ChatFragment                FrasesFragment
    TestFragment                HistorialFragment
    etc.
```

## Modelo de Datos

### Entidades Principales

#### 1. ChatMessage
```kotlin
data class ChatMessage(
    val texto: String,
    val esUsuario: Boolean
)
```

#### 2. Pregunta
```kotlin
data class Pregunta(
    val id: Int,
    val bloque: Int,
    val categoria: String,
    val texto: String,
    val opciones: List<Opcion>
)
```

#### 3. Opcion
```kotlin
data class Opcion(
    val texto: String,
    val valor: Int
)
```

### Estructura Firebase Firestore

```
firestore/
├── usuarios/
│   └── {uid}/
│       ├── nombre: String
│       ├── email: String
│       ├── fechaNacimiento: String
│       ├── fotoPerfil: String (URL)
│       ├── antecedente1: String
│       ├── antecedente2: String
│       ├── antecedente3: String
│       ├── fechaRegistro: Timestamp
│       └── ultimaActualizacion: Timestamp
│
└── emociones/
    └── {emocionId}/
        ├── uid: String
        ├── fecha: Timestamp
        ├── emociones: Map
        │   ├── ingles: Map<String, Double>
        │   ├── espanol: Map<String, Double>
        │   ├── nuevo_modelo: Map<String, Double>
        │   ├── modelo_voz_propia: Map<String, Double>
        │   └── modelo_voz_propia_lstm: Map<String, Double>
        ├── emocionPredominante: String
        └── confianza: Double
```

## Patrones de Diseño Utilizados

### 1. **Single Activity Architecture**
- Una sola Activity (MainActivity)
- Múltiples Fragments para cada pantalla
- Navegación mediante Navigation Component

### 2. **Repository Pattern (Implícito)**
- Firebase actúa como repositorio remoto
- Almacenamiento local (JSON, SharedPrefs) como caché

### 3. **Factory Pattern**
- Construcción de vistas dinámicas (RadioButtons)
- Creación de items de chat

### 4. **Observer Pattern**
- Listeners en Firebase para cambios en tiempo real
- RecognitionListener para eventos de voz
- Callbacks de OkHttp para respuestas asíncronas

### 5. **Template Method Pattern**
- BaseMenuFragment define estructura común
- Fragments específicos implementan detalles

### 6. **Strategy Pattern**
- Múltiples modelos de detección de emociones
- Selección dinámica de endpoints

## Dependencias entre Componentes

```
MainActivity
    │
    ├── Navigation Controller
    │   └── NavHostFragment
    │       └── Fragments
    │
    └── ViewBinding
        └── Layout Resources

Fragments
    │
    ├── depende de → Models (ChatMessage, Pregunta, etc.)
    ├── depende de → Firebase SDK
    ├── depende de → OkHttp
    ├── depende de → Gson
    ├── depende de → Android APIs (Speech, TTS, Audio)
    └── depende de → Glide (para imágenes)

Firebase
    │
    ├── Authentication → gestiona sesiones
    ├── Firestore → almacena datos estructurados
    └── Storage → almacena archivos

APIs Externas
    │
    ├── Groq API → genera respuestas de IA
    └── HuggingFace → detecta emociones
```

## Ciclo de Vida de la Aplicación

```
┌─────────────────────────────────────────────────────────────┐
│                    INICIO DE LA APP                          │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
                 ┌────────────────────┐
                 │  MainActivity      │
                 │  onCreate()        │
                 │  • Setup NavHost   │
                 └──────────┬─────────┘
                            │
                            ▼
                 ┌────────────────────┐
                 │ LoginFragment      │
                 │ onViewCreated()    │
                 └──────────┬─────────┘
                            │
                  ┌─────────┴─────────┐
                  │                   │
            NO AUTH               AUTH OK
                  │                   │
                  ▼                   ▼
         ┌────────────────┐  ┌────────────────┐
         │ Mostrar Login  │  │ MenuFragment   │
         │ • Email/Pass   │  │ • Opciones     │
         └────────────────┘  └────────┬───────┘
                                      │
                      ┌───────────────┼───────────────┐
                      │               │               │
                      ▼               ▼               ▼
              ┌────────────┐  ┌────────────┐  ┌────────────┐
              │Chat/Voz    │  │ Evaluación │  │ Emociones  │
              │• Escucha   │  │• Test      │  │• Detecta   │
              │• Responde  │  │• Guarda    │  │• Analiza   │
              └────────────┘  └────────────┘  └────────────┘
                      │               │               │
                      └───────────────┼───────────────┘
                                      │
                                      ▼
                          ┌────────────────────┐
                          │ Datos guardados    │
                          │ en Firebase        │
                          │ & Local Storage    │
                          └────────────────────┘
```

## Manejo de Errores y Casos Límite

### Estrategia de Manejo de Errores

```
Error Detection
    │
    ├── Network Errors
    │   ├── No Internet → Mostrar mensaje, sugerir retry
    │   ├── Timeout → Reintentar con backoff
    │   └── API Error → Log + mensaje usuario
    │
    ├── Permission Errors
    │   ├── Audio denied → Explicar necesidad, solicitar nuevamente
    │   └── Storage denied → Funcionalidad limitada
    │
    ├── Authentication Errors
    │   ├── Invalid credentials → Mensaje específico
    │   ├── User not found → Sugerir registro
    │   └── Session expired → Re-login
    │
    └── Data Errors
        ├── Invalid JSON → Usar defaults
        ├── Missing data → Validación + feedback
        └── Firebase error → Retry + log
```

### Casos Límite Manejados

1. **Usuario sin conexión**: Uso de datos en caché
2. **API Key inválida**: Error descriptivo al desarrollador
3. **Micrófono no disponible**: Desactivar funcionalidad de voz
4. **Archivo de preguntas corrupto**: Manejo de excepción JSON
5. **Sesión expirada**: Redirección automática a login
6. **Respuesta de IA vacía**: Mensaje de error amigable

---

Este documento proporciona una visión completa de la arquitectura del sistema IHC-Priv, facilitando el entendimiento técnico y el mantenimiento del proyecto.
