---
type: Reference
title: Data Models
description: Domain entities (Agent, Chat, Message, Provider, User, Server) with serialization, relationships, and state management via StateFlow.
---

# Data Models

BeakShield's data model represents the DAWSON server state. All entities are Kotlin data classes decorated with `@Serializable` for seamless JSON serialization via KotlinX Serialization.

---

## Entity Relationships

```
┌──────────────────────────────────────────┐
│ User (current user identity)             │
│  - uuid: String                          │
│  - createdAt: Long                       │
└────────────────┬─────────────────────────┘
                 │
                 │ owns
                 ▼
┌──────────────────────────────────────────┐
│ Chat (conversation session)              │
│  - uuid: String                          │
│  - userUUID: String (FK to User)         │
│  - agentUUID: String (FK to Agent)       │
│  - messages: StateFlow<List<Message>>    │
└────────────────┬─────────────────────────┘
                 │
                 │ contains
                 ▼
┌──────────────────────────────────────────┐
│ Message (individual message)             │
│  - uuid: String                          │
│  - sourceUUID: String (FK to Actor)      │
│  - destinationUUID: String (FK to Actor) │
│  - chunks: Map<Int, String>              │
│  - type: MsgType                         │
└──────────────────────────────────────────┘

                 ┌─────────────────────────────────────────┐
                 │ Agent (AI agent in the system)          │
                 │  - uuid: String                         │
                 │  - type: AgentType (DAWSON/SQUIREBOT)   │
                 │  - model: LLMModel                      │
                 │  - state: AgentState                    │
                 │  - directories: List<String>            │
                 └──────────────────┬──────────────────────┘
                                    │
                                    │ uses
                                    ▼
                 ┌──────────────────────────────────────────┐
                 │ Provider (LLM service provider)          │
                 │  - type: ProviderType (OpenAI/Anthropic)│
                 │  - apiKey: String                       │
                 │  - availableModels: List<LLMModel>      │
                 │  - defaultModel: LLMModel?              │
                 └──────────────────┬──────────────────────┘
                                    │
                                    │ hosts
                                    ▼
                 ┌──────────────────────────────────────────┐
                 │ LLMModel (available LLM model)           │
                 │  - id: String (model name/identifier)   │
                 │  - contextWindow: Int                   │
                 │  - costPer1kPromptTokens: Float         │
                 │  - costPer1kCompletionTokens: Float     │
                 └──────────────────────────────────────────┘

┌──────────────────────────────────────────┐
│ Server (DAWSON backend configuration)   │
│  - address: String                       │
│  - port: Int                             │
│  - authToken: String                     │
│  - certificateFingerprint: String        │
└──────────────────────────────────────────┘
```

---

## Core Entities

### Agent

Represents an AI agent in the DAWSON system. Can be DAWSON (main orchestrator), SQUIREBOT (conversation-specific), or PAGE (page/document processing).

```kotlin
@Serializable
data class Agent(
    val uuid: String,
    val userUUID: String,
    val type: AgentType = AgentType.SQUIREBOT,
    var mode: Mode = Mode.EGG,                // EGG, COCOON, BUTTERFLY, SWARM
    var model: LLMModel,                      // Selected model for this agent
    var state: AgentState = AgentState.READY, // Current processing state
    var thoughtWindow: Int,                   // Size of thinking/planning window
    var contextWindow: Int,                   // Model's max context window
    var useThinking: Boolean = true,          // Enable extended thinking
    var directories: List<String> = emptyList(), // Accessible file paths
    var updatedTimestamp: Long = Clock.System.now().toEpochMilliseconds()
)

enum class AgentType {
    DAWSON,      // Main orchestrator
    SQUIREBOT,   // Conversation agent
    PAGE;        // Document/page agent
}

enum class Mode {
    EGG,         // Learning/planning
    COCOON,      // Thinking/exploring
    BUTTERFLY,   // Acting/executing
    SWARM;       // Coordinating multiple agents
}

enum class AgentState {
    READY,           // Idle, waiting for input
    AWAITING_INPUT,  // Blocked on user input request
    PROCESSING,      // Running (thinking, acting, responding)
    THINKING,        // Extended thinking (Claude only)
    ACTING,          // Executing tools/actions
    RESPONDING,      // Generating response
    ERROR;           // Error state
    
    val isAwaitingResponse: Boolean
        get() = this in listOf(PROCESSING, THINKING, ACTING, RESPONDING)
}
```

**Key methods:**
- `profileImage: DrawableResource` — returns UI icon for agent type
- `profileColor: Color` — returns UI color for agent type

### Chat

Represents a conversation session between a user and an agent.

```kotlin
@Serializable
data class Chat(
    val uuid: String,
    val userUUID: String,
    val agentUUID: String,
    var title: String = "",                  // User-facing chat title
    var subtitle: String = "",               // Optional subtitle
    var updatedTimestamp: Long = Clock.System.now().toEpochMilliseconds()
) {
    @Transient
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    
    @Transient
    val messages = _messages.asStateFlow()   // Observed by UI
    
    // Update message delivery status
    fun setDelivered(msgUUID: String)
    
    // Add or update pending message during streaming
    fun addPendingMessage(newMsg: Message, dataIndex: Int)
    
    // Add chunk of streamed message
    fun addMessageChunk(msgUUID: String, dataIndex: Int, chunk: String)
    
    // Clear all messages
    fun clearMessages()
}
```

**Key properties:**
- `messages: StateFlow<List<Message>>` — observed by ChatView for rendering
- `title` — derived from agent name or first message (editable by user)
- `updatedTimestamp` — used for sorting chat list by recent activity

### Message

Represents a single message (prompt or response) in a chat.

```kotlin
data class Message(
    val uuid: String,
    val dataUUID: String,                    // Unique identifier for this message/run
    val sourceUUID: String,                  // UUID of sender (user/agent)
    val destinationUUID: String,             // UUID of recipient
    val type: MsgType = MsgType.TEXT_PROMPT, // Content type
    var chunks: MutableMap<Int, String> = mutableMapOf(),
    var numChunks: Int? = null,              // Total expected chunks (null = not complete)
    val isStream: Boolean = false,           // Is this a streamed message?
    val delivered: Boolean = false,          // Delivery status
    var createdTimestamp: Long = Clock.System.now().toEpochMilliseconds(),
    var updatedTimestamp: Long = Clock.System.now().toEpochMilliseconds()
) {
    enum class MsgType {
        TEXT_PROMPT,       // User text input
        TEXT_THINKING,     // Agent's thinking/planning
        TEXT_RESPONSE,     // Agent's text response
        TOOL_CALL_NAME,    // Tool invocation name
        TOOL_CALL_RESULT,  // Tool execution result
        DATA_PROMPT,       // File/data input
        DATA_RESPONSE;     // File/data response
    }
    
    // Reconstruct full message from chunks
    val fullContent: String
        get() = chunks.toSortedMap().values.joinToString("")
    
    // Check if all chunks received
    val isComplete: Boolean
        get() = numChunks != null && chunks.size == numChunks
}
```

**Key design:**
- **Chunking**: Large messages are split into 32KB chunks for network efficiency
- **Streaming**: `isStream = true` for real-time responses (e.g., Claude thinking)
- **Delivery tracking**: `delivered` flag indicates server receipt
- **Grouping**: Messages with same `dataUUID` are part of the same run/transaction

### Provider

Represents an LLM service provider (OpenAI, Anthropic, Ollama).

```kotlin
@Serializable
data class Provider(
    val type: ProviderType,                    // OPENAI, ANTHROPIC, OLLAMA
    var apiKey: String = "",                   // API key (empty if using OAuth)
    var useOAuth: Boolean = false,             // Use OAuth flow instead of API key
    var availableModels: List<LLMModel> = emptyList(),
    var preferredModelIDs: List<String> = emptyList(),
    var defaultModelID: String = "",
    val updatedTimestamp: Long = Clock.System.now().toEpochMilliseconds()
) {
    enum class ProviderType {
        OLLAMA,       // Local LLM
        OPENAI,       // OpenAI API
        ANTHROPIC;    // Anthropic API
    }
    
    val preferredModels: List<LLMModel>
        get() = availableModels.filter { preferredModelIDs.contains(it.id) }
    
    val defaultModel: LLMModel?
        get() = availableModels.firstOrNull { it.id == defaultModelID }
}
```

**Key design:**
- **API Key vs. OAuth**: Can use API key (simple) or OAuth (secure, browser-based)
- **Model lists**: `availableModels` are fetched from provider on auth
- **Preferred models**: Subset of available models, user can select preferred ones
- **Default model**: Default model for new chats using this provider

### LLMModel

Information about an available language model.

```kotlin
@Serializable
data class LLMModel(
    val id: String,                          // Model identifier (e.g., "gpt-4-turbo")
    val name: String = id,                   // User-friendly name
    val contextWindow: Int = 8192,           // Max context size in tokens
    val costPer1kPromptTokens: Float = 0f,   // Cost for input tokens
    val costPer1kCompletionTokens: Float = 0f, // Cost for output tokens
    val capabilities: List<String> = emptyList(),  // ["vision", "thinking", etc.]
    val isAvailable: Boolean = true
)
```

**Used by:**
- Agent model selection
- Provider model list
- UI for displaying available models per provider

### User

Represents the current user of the application.

```kotlin
@Serializable
data class User(
    val uuid: String,
    val name: String = "",
    val email: String? = null,
    val createdAt: String = Clock.System.now().toString()
) {
    companion object {
        val defaultUser = User(
            uuid = "default-user-uuid",
            name = "User",
            email = null
        )
    }
}
```

**Note**: Currently, BeakShield uses a hardcoded `defaultUser` for development. Multi-user support is planned.

### Server

Configuration for connecting to DAWSON backend.

```kotlin
@Serializable
data class Server(
    var address: String = "",                // IP or hostname
    var port: Int = 8443,                    // Port (default TLS)
    var authToken: String = "",              // Bearer token
    var certificateFingerprint: String = "", // SHA-256 fingerprint (optional)
    var version: String = ""                 // Server version
)
```

**Stored in preferences** so user doesn't re-enter server details on each launch.

---

## Message Flow & Streaming

### Streaming Messages (Real-Time Responses)

When an agent responds with extended thinking or streaming:

1. **Server sends multiple MESSAGE packets** with chunks
2. **Each chunk** has `dataUUID` (same for all chunks), `dataIndex` (0, 1, 2, ...), and payload
3. **Client reassembles** in `Message.chunks` map: `{ 0 → "chunk0", 1 → "chunk1", ... }`
4. **When complete**: `numChunks` is set; UI recomputes from `fullContent`

Example:
```
Server: Message { dataUUID: "msg-1", dataIndex: 0, payload: "Hello " }
        Message { dataUUID: "msg-1", dataIndex: 1, payload: "world" }
        Message { dataUUID: "msg-1", numChunks: 2 }  ← signals completion

Client constructs:
        chunks = { 0 → "Hello ", 1 → "world" }
        numChunks = 2
        fullContent = "Hello world"
```

---

## State Isolation: @Transient Fields

Some entity fields are transient (not serialized/deserialized):

```kotlin
@Serializable
data class Chat(...) {
    @Transient
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
}
```

**Why?** Messages come from separate WebSocket packets (MESSAGE_DATA), not as part of the Chat payload. The StateFlow is client-side only; chat title/metadata is serialized, but message list is managed separately.

---

## Serialization Configuration

Located in `WebSocketClient.kt`:

```kotlin
val json = Json {
    ignoreUnknownKeys = true  // Server may add new fields; don't fail
    encodeDefaults = false     // Don't send null/default values
}
```

**Benefits:**
- **Forward compatibility**: Server adds new fields → client ignores them gracefully
- **Bandwidth**: Default values are omitted (only send what changed)
- **Type safety**: No reflection; code-generated serializers at compile time

---

## Adding a New Entity Field

**Scenario**: Server adds a new field to Agent.

1. **Update data class**:
   ```kotlin
   data class Agent(
       // ... existing fields
       val newField: String = "",  // Add default to maintain serialization
   )
   ```

2. **No breaking change** because:
   - `ignoreUnknownKeys = true` on client → server's old packets still deserialize
   - Server's new field has a default → old client packets still deserialize

3. **Update ViewModels/UI** that display the field (if needed)

---

## Entity Lifecycle

### Creation

1. Server creates entity (e.g., new Chat)
2. Sends packet with entity data
3. Dawson handler deserializes and adds to StateFlow
4. ViewModels/UI observe StateFlow and render

### Updates

1. User or server changes entity (e.g., chat title, agent mode)
2. If user-initiated: ViewModel calls Dawson method → WebSocket packet
3. Server processes, broadcasts update
4. Client deserializes, updates StateFlow
5. UI recomputes and re-renders

### Deletion

1. User or server deletes entity (e.g., delete chat)
2. Server sends CHAT_DATA packet with delete action
3. Dawson removes from StateFlow
4. UI recomputes (e.g., chat list shrinks)

---

## Collections & Lists

**StateFlow collections** in Dawson:

```kotlin
val activeAgents: StateFlow<List<Agent>>       // All agents (immutable list)
val activeChats: StateFlow<List<Chat>>         // All chats
val activeProviders: StateFlow<List<Provider>> // All providers
val users: StateFlow<List<User>>               // All users
val pendingInputRequests: StateFlow<List<UserInputRequest>>
```

**Updates are via `.update { ... }`**:

```kotlin
_activeChats.update { chats ->
    chats.map { chat ->
        if (chat.uuid == updatedUUID) updatedChat else chat
    }
}
```

This ensures subscribers are notified of changes even if the list reference hasn't changed (compare objects within).

---

## See Also

- [Architecture Overview](../architecture/overview.md) — How entities flow through the app
- [Workflows](../workflows/overview.md) — State transitions and user flows
- [Integration & Networking](../integration/overview.md) — Packet types and serialization

---

*Last Updated: Generated by OpenWiki*
