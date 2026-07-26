---
type: Architecture
title: Architecture Overview
description: Kotlin Multiplatform setup, MVVM pattern, reactive state management with Flow, and the data flow pipeline from WebSocket to UI.
---

# Architecture Overview

BeakShield is built on **Kotlin Multiplatform (KMP)** with **Compose Multiplatform** for UI and **Kotlin Flow** for reactive state management. This page covers the overall architecture, layers, and data flow.

---

## Technology Stack

| Component | Technology | Notes |
|-----------|-----------|-------|
| **Language** | Kotlin 2.0 | Multiplatform (JVM, Android, iOS, Web targets) |
| **UI Framework** | Compose Multiplatform | Declarative, composable UI across platforms |
| **Networking** | Ktor Client 3.4.2 | WebSocket + HTTP with multiplatform support |
| **Serialization** | KotlinX Serialization | JSON, code-generated, no reflection |
| **State Management** | Kotlin Flow + StateFlow | Coroutine-based, cold/hot reactive streams |
| **Architecture Pattern** | MVVM | ViewModels expose StateFlow; Composables observe |
| **Persistence** | MultiplatformSettings | Simple key-value store, platform-abstracted |
| **Target Platforms** | JVM, Android, iOS | Web targets (currently disabled) |
| **JDK Version** | 21 | Required; set in gradle.properties |

---

## Kotlin Multiplatform Structure

BeakShield uses KMP's **expect/actual** mechanism to share code across platforms while allowing platform-specific implementations.

### Code Organization

```
composeApp/src/
├── commonMain/              # ~95% of code—shared across all platforms
│   ├── kotlin/com/beakshield/
│   │   ├── App.kt           # Compose entry point (multi-platform)
│   │   ├── BeakShieldApp.kt # Service locator singleton
│   │   ├── Platform.kt      # expect declarations (platform-specific interfaces)
│   │   └── ... (rest of app)
│   └── resources/           # Shared assets (icons, strings)
│
├── androidMain/             # Android-specific implementations
│   ├── kotlin/.../Platform.android.kt      # actual Platform
│   ├── kotlin/.../PreferencesSettings.kt   # Android SharedPreferences
│   ├── kotlin/.../WebSocketClient.kt       # Ktor OkHttp client
│   └── AndroidManifest.xml
│
├── iosMain/                 # iOS-specific implementations
│   ├── kotlin/.../Platform.ios.kt          # actual Platform
│   ├── kotlin/.../PreferencesSettings.kt   # iOS UserDefaults
│   ├── kotlin/.../WebSocketClient.kt       # Ktor Darwin client
│   └── Info.plist
│
├── jvmMain/                 # JVM/Desktop implementations
│   ├── kotlin/.../main.kt               # Desktop Compose window setup
│   ├── kotlin/.../Platform.jvm.kt       # actual Platform
│   ├── kotlin/.../PreferencesSettings.kt # File-based settings
│   └── kotlin/.../WebSocketClient.kt    # Ktor CIO client
│
└── commonTest/              # Shared tests (minimal coverage)
```

### Key `expect`/`actual` Declarations

**`Platform.kt`** defines platform capabilities:
```kotlin
// commonMain/kotlin/com/beakshield/Platform.kt
expect fun getPlatformName(): String
expect fun getDeviceModel(): String
// ... other platform-specific API declarations
```

Each platform implements:
```kotlin
// androidMain/kotlin/.../Platform.android.kt
actual fun getPlatformName() = "Android"
actual fun getDeviceModel() = Build.MODEL
```

This allows common code to call platform features without imports or casts.

---

## MVVM + Reactive State Management

BeakShield follows **Model-View-ViewModel (MVVM)** with **Kotlin Flow** for reactive updates:

### Layers

```
┌─────────────────────────────────────────────────────────┐
│               Composable (UI Views)                      │
│  Observes StateFlow<T> via collectAsState()             │
│  Calls ViewModel methods on user interaction            │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│         ViewModel (*ScreenViewModel)                     │
│  Extends VModel; exposes StateFlow<T>                   │
│  Derives state via combine(), map(), flatMapLatest()    │
│  Calls Dawson methods (send messages, update config)   │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│    Model (Dawson + Server Data Classes)                 │
│  Manages WebSocket connection                          │
│  Routes incoming packets to handlers                   │
│  Updates StateFlow collections (activeChats, etc.)     │
│  Provides public methods (sendMessage, updateAgent)    │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│        Network Layer (WebSocketClient)                   │
│  Maintains WebSocket connection                         │
│  Serializes/deserializes packets (KotlinX JSON)         │
│  Handles chunk buffering for large packets              │
│  Emits incoming packets as SharedFlow                   │
└─────────────────────────────────────────────────────────┘
```

### Data Class Hierarchy

```
Agent          ← Represents an AI agent (DAWSON, SQUIREBOT, PAGE)
Chat           ← A conversation session; contains messages
Message        ← Individual message with content and metadata
Provider       ← LLM provider (OpenAI, Anthropic, Ollama)
LLMModel       ← Available model info (name, context window)
User           ← Current user identity
Server         ← Server configuration and metadata
```

All decorated with `@Serializable` for KotlinX JSON support.

---

## State Management Flow

### The Dawson Class: Central State Hub

`Dawson` is the single source of truth for all server data:

```kotlin
class Dawson {
    // StateFlow collections that ViewModels observe
    val activeAgents: StateFlow<List<Agent>>       // All known agents
    val activeChats: StateFlow<List<Chat>>         // All conversations
    val activeProviders: StateFlow<List<Provider>> // Available providers
    val users: StateFlow<List<User>>               // Users
    val pendingInputRequests: StateFlow<List<UserInputRequest>>
    val currentUserUUID: StateFlow<String?>        // Current user ID
    
    // Public methods to send actions back to server
    fun sendMessage(chatUUID: String, content: String)
    fun updateAgent(agent: Agent)
    fun deleteChat(chatUUID: String)
    fun addDirectory(chatUUID: String, path: String)
    // ... more
}
```

### Reactive Data Flow Example

**User sends a message:**

1. **Composable** calls `viewModel.sendMessage("Hello")`
2. **ViewModel** calls `Dawson.sendMessage(chatUUID, "Hello")`
3. **Dawson** sends WebSocket packet: `USER_DATA { message = "Hello", ... }`
4. **WebSocketClient** deserializes incoming response: `USER_DATA` or `CHAT_DATA` with new message
5. **Dawson.handleUserData()** or **handleChatData()** updates `_activeChats` StateFlow
6. **ViewModel's derived flow** (computed via `combine()`) recomputes its `messages` StateFlow
7. **Composable** observes `messages.collectAsState()`, re-renders with new message bubble

```
User Input → ViewModel → Dawson → WebSocket → Server → Response
                                                           ↓
Composable ← ViewModel ← Dawson (StateFlow update) ← Packet Handler
```

### ViewModel Pattern: The VModel Base

All screen ViewModels extend the `VModel` interface:

```kotlin
interface VModel {
    val railContent: StateFlow<RailContent?>  // Sidebar content
}

class ChatsScreenViewModel : VModel {
    private val scope = CoroutineScope(Dispatchers.Default)
    
    // Private mutable state (prefixed with _)
    private val _chatUUIDSelected = MutableStateFlow<String?>(null)
    
    // Public immutable state (exposed without _)
    val chatUUIDSelected = _chatUUIDSelected.asStateFlow()
    
    // Derived/computed state (cached with stateIn)
    val currentChat: StateFlow<Chat?> = combine(
        _chatUUIDSelected,
        BeakShieldApp.dawson.activeChats
    ) { uuid, chats ->
        chats.find { it.uuid == uuid }
    }.stateIn(scope, SharingStarted.Eagerly, null)
    
    // Public methods to handle user actions
    fun selectChat(uuid: String) {
        _chatUUIDSelected.value = uuid
    }
    
    fun sendMessage(content: String) {
        val uuid = _chatUUIDSelected.value ?: return
        BeakShieldApp.dawson.sendMessage(uuid, content)
    }
}
```

**Key conventions:**
- Private mutable flows: `_name` (e.g., `_chatUUIDSelected`)
- Public immutable flows: `name` (e.g., `chatUUIDSelected`)
- Derived/computed flows: use `combine()` or `flatMapLatest()` + `stateIn()`
- Scope: always `CoroutineScope(Dispatchers.Default)` for background work

---

## WebSocket Packet Routing

All server communication is **packet-based** and routed through `Dawson`:

### Packet Types

| Type | Direction | Payload | Handled By |
|------|-----------|---------|-----------|
| `PONG` | Server → Client | (empty) | `handlePong()` |
| `SYNC_STATE` | Server → Client | List of entity UUIDs to refresh | `handleSyncState()` |
| `USER_DATA` | Both | Messages, file attachments, run cancellations | `handleUserData()` |
| `AGENT_DATA` | Both | Agent state, mode, model, settings | `handleAgentData()` |
| `CHAT_DATA` | Both | Chat creation, updates, deletion | `handleChatData()` |
| `CONFIG_DATA` | Both | Provider API keys, server settings | `handleConfigData()` |
| `USER_INPUT_REQUEST` | Server → Client | Request for user input (blocking call) | `handleUserInputRequest()` |
| `USER_INPUT_RESPONSE` | Client → Server | Response to input request | (sent via `sendUserInputResponse()`) |

### Packet Flow

```
WebSocketClient.handleIncomingText()
  ↓
deserialize to WSPacket
  ↓
emit to incomingPackets: SharedFlow<WSPacket>
  ↓
Dawson.scope collects incomingPackets
  ↓
when (packet.type) { SYNC_STATE → ..., USER_DATA → ..., etc. }
  ↓
Update StateFlow (activeChats, activeAgents, etc.)
  ↓
ViewModels observe StateFlow, recompute derived flows
  ↓
Composables observe ViewModels, re-render
```

### Sending Data Back

Client-initiated actions go through public methods on `Dawson`:

```kotlin
fun sendMessage(chatUUID: String, content: String) {
    scope.launch {
        val packet = WSPacket(
            type = PacketType.USER_DATA,
            payload = UserData(
                messageContent = content,
                chatUUID = chatUUID,
                // ... more fields
            )
        )
        socket.send(packet)
    }
}
```

---

## Service Locator: BeakShieldApp

All top-level services and ViewModels are accessed through a singleton:

```kotlin
object BeakShieldApp {
    val preferences: Preferences by lazy { Preferences() }
    val dawson: Dawson by lazy { Dawson() }
    val notifications: Notifications by lazy { Notifications() }
    val baseScreenViewModel by lazy { BaseScreenViewModel() }
    val chatsScreenViewModel by lazy { ChatsScreenViewModel() }
    val systemScreenViewModel by lazy { SystemScreenViewModel() }
    // ... more ViewModels
    
    fun onCreate() {
        // Initialize all services on app startup
        preferences
        dawson
        notifications
        // ... force lazy initialization of ViewModels
    }
}
```

**Why this pattern?**
- Avoids dependency injection boilerplate in a client app
- Single initialization point in `onCreate()`
- Easy to access from any Composable or ViewModel without constructor injection

**Accessed from anywhere:**
```kotlin
@Composable
fun ChatView() {
    val viewModel = BeakShieldApp.chatsScreenViewModel
    val chats by viewModel.chats.collectAsState()
    // ... render
}
```

---

## Serialization: KotlinX JSON

All data classes use `@Serializable` for KotlinX JSON support:

```kotlin
@Serializable
data class Chat(
    val uuid: String,
    val agentUUID: String,
    val userUUID: String,
    val createdAt: String,
    val messages: List<Message> = emptyList(),
    val mode: String? = null,
    val model: String? = null,
    // ... more fields
)
```

**Serialization Config:**
```kotlin
// In WebSocketClient
val json = Json {
    ignoreUnknownKeys = true  // Server may add new fields
    encodeDefaults = false    // Skip null/default values
}
```

**No reflection**: KotlinX generates serializer code at compile time, enabling multiplatform support and faster serialization.

---

## Coroutine Lifecycle

### ViewModels

Each ViewModel manages its own `CoroutineScope`:

```kotlin
class ChatsScreenViewModel : VModel {
    private val scope = CoroutineScope(Dispatchers.Default)
    
    // Scope lives for app lifetime; not cancelled on screen exit
    // (Consider adding proper cleanup for long-term apps)
}
```

**Current limitation**: Scopes are never cancelled, so if an app unloads/reloads screens, old coroutines may persist. For production, consider adding a lifecycle interface to cancel scope on ViewModel destruction.

### Composables

Composables use `rememberCoroutineScope()` for temporary launch blocks:

```kotlin
@Composable
fun MyButton() {
    val scope = rememberCoroutineScope()
    
    Button(onClick = {
        scope.launch {
            // This coroutine is cancelled when Composable leaves composition
        }
    })
}
```

---

## Performance Considerations

### StateFlow Subscriptions

- **Eager sharing**: ViewModels use `SharingStarted.Eagerly` so flows are active even if no subscribers exist (wastes CPU if not observed)
- **Derived flows are cached**: `combine()` results are cached with `stateIn()` to avoid recomputation on every Composable recomposition
- **No unsubscription**: Currently, there's no active unsubscription from flows; long-running apps may leak memory if screens are frequently added/removed

**Recommendation**: For large apps, implement lifecycle-aware scope cancellation.

### Scroll Performance

- **ChatView**: Uses `LazyColumn` for messages (efficient even with many items)
- **Large histories**: No pagination yet; may cause lag on very old/large chats
- **Planned**: Message pagination or virtual scroll optimization

### Network Optimization

- **Packet chunking**: Large payloads are split into 32KB chunks and reassembled on arrival
- **Sync polling**: Currently runs every few seconds; sync is not event-driven
- **Batch updates**: Multiple entities can arrive in a single packet to reduce overhead

---

## Development & Debugging

### Logging

- `Dawson` and `WebSocketClient` use `println()` for key state transitions
- Log statements are visible in IDE console and Android Logcat
- Filter by package name for easier debugging: `BeakShield`, `WebSocketClient`, `Dawson`

### Hot Reload

- **Compose Hot Reload** plugin is enabled
- Changes to Composable functions reload without full rebuild
- Useful for UI iteration; state is preserved if scope survives reload

### Server Connection State

Observable via `Dawson.connectionState`:

```kotlin
@Composable
fun ConnectionIndicator() {
    val connState by BeakShieldApp.dawson.connectionState.collectAsState()
    
    when (connState.state) {
        CONNECTED -> Icon(green)
        DISCONNECTED -> Icon(gray)
        ERROR -> Icon(red, connState.description)
    }
}
```

---

## See Also

- [Screens & UI](../screens/overview.md) — Composable patterns and screen hierarchy
- [Data Models](../data-models/overview.md) — Entity definitions and serialization
- [Workflows](../workflows/overview.md) — Key user flows and state transitions
- [Integration & Networking](../integration/overview.md) — WebSocket setup and server connectivity

---

*Last Updated: Generated by OpenWiki*
