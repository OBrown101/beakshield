---
type: Reference
title: Integration & Networking
description: WebSocket protocol, packet types, serialization, server connectivity, authentication, TLS certificate pinning, and connection management.
---

# Integration & Networking

BeakShield communicates with DAWSON server entirely over **WebSocket with TLS** (WSS). This page covers the WebSocket protocol, packet routing, authentication, and connection setup.

---

## Architecture: Network Layer

```
┌─────────────────────────────────────────────────┐
│ Composable / ViewModel                          │
│ (UI layer)                                      │
└──────────────────┬──────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────┐
│ Dawson (State Hub)                              │
│ - Manages connection state                      │
│ - Routes incoming packets                       │
│ - Provides public methods (sendMessage, etc.)   │
└──────────────────┬──────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────┐
│ WebSocketClient                                 │
│ - Maintains WSS connection                      │
│ - Serializes/deserializes packets               │
│ - Handles chunking for large payloads           │
│ - Emits incoming packets to SharedFlow          │
└──────────────────┬──────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────┐
│ Ktor HTTP Client (Platform-specific)            │
│ - Desktop: CIO engine                           │
│ - Android: OkHttp engine                        │
│ - iOS: Darwin engine                           │
└──────────────────┬──────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────┐
│ DAWSON Server (wss://host:port/dawson)          │
└─────────────────────────────────────────────────┘
```

---

## WebSocket Protocol

### Connection

```kotlin
// User enters server details in SystemScreen
address: String = "localhost"
port: Int = 8443
authToken: String = "bearer-token-here"
fingerprint: String = "sha256:deadbeef..." (optional)

// ViewModel calls:
Dawson.connect(address, port, authToken, fingerprint)

// Dawson calls:
WebSocketClient.connect(address, port, authToken, fingerprint)

// WebSocketClient creates Ktor HttpClient with:
- URL: wss://localhost:8443/dawson
- Auth: Bearer token in Authorization header
- TLS: Certificate pinned to fingerprint (if provided)
```

### URL & Headers

```
GET /dawson HTTP/1.1
Host: localhost:8443
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Key: ...
Sec-WebSocket-Version: 13
Authorization: Bearer <auth-token>
```

### Connection Lifecycle

```
1. User enters server address, port, auth token
2. Calls Dawson.connect(...)
3. WebSocketClient creates HttpClient with Ktor WebSocket plugin
4. Attempts WebSocket upgrade
   - If success: connection state → CONNECTED
   - If failure (401, 403, timeout, etc.): state → ERROR
5. Once connected, enters frame-reading loop
6. Reads Text frames, deserializes to WSPacket
7. Emits packet to incomingPackets SharedFlow
8. Dawson collects packets, routes by type
9. On disconnect/error: connection state → DISCONNECTED or ERROR
```

---

## Packet Types & Structure

All communication uses `WSPacket`:

```kotlin
@Serializable
data class WSPacket(
    val type: PacketType,
    val payload: JsonElement,  // Raw JSON, deserialized on-demand
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
) {
    enum class PacketType {
        PING,                    // Client keeps alive
        PONG,                    // Server alive response
        SYNC_STATE,              // Server: entities changed
        USER_DATA,               // Client/Server: messages, files
        AGENT_DATA,              // Client/Server: agent state
        CHAT_DATA,               // Client/Server: chat create/update/delete
        CONFIG_DATA,             // Client/Server: provider, server settings
        USER_INPUT_REQUEST,      // Server: request user input
        USER_INPUT_RESPONSE,     // Client: respond to input request
        ERROR;                   // Either side: error occurred
    }
    
    inline fun <reified T> payloadAs(): T? {
        return try {
            json.decodeFromJsonElement<T>(payload)
        } catch (e: Exception) {
            null
        }
    }
}
```

### Packet JSON Example: USER_DATA

```json
{
  "type": "USER_DATA",
  "payload": {
    "messageContent": "Hello, agent!",
    "chatUUID": "chat-123",
    "sourceUUID": "user-uuid",
    "destinationUUID": "agent-uuid",
    "timestamp": 1234567890,
    "sourceType": "PROMPT",
    "dataType": "TEXT"
  },
  "timestamp": 1234567895
}
```

### Packet Routing in Dawson

```kotlin
scope.launch {
    socket.incomingPackets.collect { packet ->
        when (packet.type) {
            PONG -> { /* server alive */ }
            SYNC_STATE -> {
                packet.payloadAs<SyncState>()?.let { handleSyncState(it) }
            }
            USER_DATA -> {
                packet.payloadAs<UserData>()?.let { handleUserData(it) }
            }
            AGENT_DATA -> {
                packet.payloadAs<AgentData>()?.let { handleAgentData(it) }
            }
            CHAT_DATA -> {
                packet.payloadAs<ChatData>()?.let { handleChatData(it) }
            }
            CONFIG_DATA -> {
                packet.payloadAs<ConfigData>()?.let { handleConfigData(it) }
            }
            USER_INPUT_REQUEST -> {
                packet.payloadAs<UserInputRequest>()?.let { 
                    handleUserInputRequest(it) 
                }
            }
            ERROR -> {
                packet.payloadAs<String>()?.let { errorMsg ->
                    println("Server error: $errorMsg")
                }
            }
        }
    }
}
```

---

## Payload Types

### SyncState

Server tells client which entities changed and need refresh.

```kotlin
@Serializable
data class SyncState(
    val agentUUIDs: List<String> = emptyList(),
    val chatUUIDs: List<String> = emptyList(),
    val userUUIDs: List<String> = emptyList(),
    val providerUUIDs: List<String> = emptyList(),
    val reason: String = ""  // Why sync: "agent-state-changed", etc.
)
```

### UserData

Messages, file attachments, run cancellations.

```kotlin
@Serializable
data class UserData(
    val uuid: String,
    val messageContent: String = "",
    val runUUID: String = "",
    val chatUUID: String = "",
    val sourceUUID: String = "",  // UUID of sender
    val destinationUUID: String = "",  // UUID of recipient
    val sourceType: SourceType = SourceType.PROMPT,
    val dataType: DataType = DataType.TEXT,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val payload: JsonElement? = null,  // For data attachments
    val delivered: Boolean = false
) {
    enum class SourceType { PROMPT, RESPONSE }
    enum class DataType { TEXT, DATA }
    
    inline fun <reified T> payloadAs(): T? { ... }
}
```

### AgentData

Agent state, mode, model, configuration.

```kotlin
@Serializable
data class AgentData(
    val uuid: String,
    val type: String,  // DAWSON, SQUIREBOT, PAGE
    val mode: String,  // EGG, COCOON, BUTTERFLY, SWARM
    val model: JsonElement,  // LLMModel as JSON
    val state: String,  // READY, THINKING, PROCESSING, etc.
    val directories: List<String> = emptyList(),
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)
```

### ChatData

Chat creation, updates, deletion.

```kotlin
@Serializable
data class ChatData(
    val uuid: String,
    val action: String = "UPDATE",  // CREATE, UPDATE, DELETE
    val userUUID: String = "",
    val agentUUID: String = "",
    val title: String = "",
    val subtitle: String = "",
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)
```

### ConfigData

Provider configuration, server settings.

```kotlin
@Serializable
data class ConfigData(
    val action: String = "UPDATE",  // UPDATE_PROVIDER, UPDATE_SERVER
    val provider: JsonElement? = null,  // Provider as JSON
    val server: JsonElement? = null,    // Server config as JSON
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)
```

### UserInputRequest

Server requests user input.

```kotlin
@Serializable
data class UserInputRequest(
    val uuid: String,
    val prompt: String,
    val inputType: String = "TEXT",  // TEXT, YES_NO, MULTIPLE_CHOICE
    val options: List<String> = emptyList(),  // For MULTIPLE_CHOICE
    val timeout: Long? = null  // Milliseconds before auto-timeout
)
```

### UserInputResponse

Client responds to input request.

```kotlin
@Serializable
data class UserInputResponse(
    val requestUUID: String,
    val response: String,  // User's chosen option or text input
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)
```

---

## Serialization Configuration

Located in `WebSocketClient.kt`:

```kotlin
class WebSocketClient {
    private val serializer = WSPacket.serializer()
    
    companion object {
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
            prettyPrint = false  // For production; set true for debugging
        }
    }
}
```

### Forward/Backward Compatibility

- **`ignoreUnknownKeys = true`**: Server adds new fields → client ignores gracefully
- **`encodeDefaults = false`**: Only send non-default values → smaller payloads

Example: Server adds `newField` to Agent. Client still deserializes because `ignoreUnknownKeys` is true.

---

## Large Payload Handling: Chunking

For messages larger than 32KB:

### Chunking on Send

```kotlin
fun send(packet: WSPacket) {
    val packetJson = json.encodeToString(packet)
    val maxChunkSize = 32_000
    
    if (packetJson.length > maxChunkSize) {
        // Split into chunks
        for (i in 0..packetJson.lastIndex step maxChunkSize) {
            val chunk = packetJson.substring(
                i, minOf(i + maxChunkSize, packetJson.length)
            )
            session?.send(Frame.Text(chunk))
        }
    } else {
        session?.send(Frame.Text(packetJson))
    }
}
```

### Reassembly on Receive

```kotlin
private val chunkBuffers = mutableMapOf<String, MutableList<String?>>()

fun handleIncomingText(text: String) {
    // Attempt to parse as complete packet
    val packet: WSPacket? = try {
        json.decodeFromString(text)
    } catch (e: Exception) {
        null
    }
    
    if (packet != null) {
        // Complete packet; emit
        _incomingPackets.emit(packet)
    } else {
        // Possible chunk; buffer it
        // Reconstruct: if buffer looks complete, deserialize
        val reconstructed = reconstructFromChunks(text)
        if (reconstructed != null) {
            _incomingPackets.emit(reconstructed)
        }
    }
}
```

---

## Authentication & TLS

### Bearer Token

```kotlin
client?.webSocket(url) {
    bearerAuth(authToken)  // Adds: Authorization: Bearer <token>
}
```

### Certificate Pinning

```kotlin
fun dawsonHttpClient(fingerprint: String): HttpClient {
    return HttpClient {
        install(WebSockets)
        engine {
            // Platform-specific engine config
            // Ktor uses engine's TLS configuration
        }
        install(CertificatePinning) {
            if (fingerprint.isNotEmpty()) {
                pin(fingerprint)
            }
        }
    }
}
```

**Certificate pinning prevents MITM attacks:**
- Client computes SHA-256 hash of server certificate
- Compares against user-provided fingerprint
- If mismatch: connection rejected

**Obtaining fingerprint:**
```bash
# Linux/macOS
openssl s_client -connect localhost:8443 -showcerts | \
  openssl x509 -outform DER | \
  openssl dgst -sha256 -hex
```

---

## Connection State Management

### ServerConnState

```kotlin
data class ServerConnState(
    var state: ConnState = DISCONNECTED,
    private val msg: String = ""
) {
    enum class ConnState {
        CONNECTED,
        DISCONNECTED,
        ERROR;
    }
    
    val color: Color
        get() = when (state) {
            CONNECTED -> lightGreenColor
            DISCONNECTED, ERROR -> dangerColor
        }
    
    val message: String
        get() = when (state) {
            CONNECTED -> "Connected"
            DISCONNECTED -> "Disconnected"
            ERROR -> "Error"
        }
    
    val description: String
        get() = when (state) {
            CONNECTED -> "Your server is currently configured and connected."
            DISCONNECTED -> "Enter your server information and connect."
            ERROR -> msg
        }
}
```

### State Transitions

```
[DISCONNECTED] --connect()--> [CONNECTING...] --success--> [CONNECTED]
                                                 |
                                              failure
                                                 |
                                                 v
                                            [ERROR]
                                                 |
                                             retry
                                                 |
                                                 v
                                           [CONNECTING...]
```

---

## Keeping Alive: Ping/Pong

WebSocket may timeout after inactivity:

```kotlin
// In Dawson.init():
connectTimerJob = scope.launch {
    while (isActive) {
        delay(30_000)  // Every 30 seconds
        socket.send(WSPacket(PacketType.PING))
    }
}

// In Dawson.handlePong():
fun handlePong() {
    println("Server pong")
}
```

**Server responds with PONG**, keeping connection alive.

---

## Error Handling

### Network Errors

```kotlin
try {
    client?.webSocket(url) { ... }
} catch (e: ConnectException) {
    setConnState(ERROR, "Failed to connect: ${e.message}")
} catch (e: TimeoutException) {
    setConnState(ERROR, "Connection timeout")
} catch (e: Exception) {
    setConnState(ERROR, e.message ?: "Unknown error")
}
```

### Auth Errors

```kotlin
// Server responds with 401 Unauthorized
// Ktor throws an exception during WebSocket upgrade
// Caught above; shows "Failed to connect" or "Unauthorized"

// User must update token in SystemScreen and retry
```

### Message Send Errors

```kotlin
fun sendMessage(chatUUID: String, content: String) {
    scope.launch {
        try {
            val packet = WSPacket(...)
            socket.send(packet)
            // Set timeout for delivery confirmation
            delay(5000)
            if (!deliveryConfirmed) {
                // Show retry button to user
            }
        } catch (e: Exception) {
            // Network error; show notification
        }
    }
}
```

---

## Server Configuration Persistence

User's server details are saved to preferences:

```kotlin
class Preferences {
    val serverAddress: String = preferences.getString("server_address", "")
    val serverPort: Int = preferences.getInt("server_port", 8443)
    val authToken: String = preferences.getString("auth_token", "")
    val certificateFingerprint: String = preferences.getString("cert_fingerprint", "")
}
```

On app launch, Dawson auto-connects if details are configured:

```kotlin
BeakShieldApp.onCreate() {
    // ... init services
    
    if (preferences.serverAddress.isNotEmpty()) {
        Dawson.connect(
            preferences.serverAddress,
            preferences.serverPort,
            preferences.authToken,
            preferences.certificateFingerprint
        )
    }
}
```

---

## Debugging WebSocket Communication

### Enable Pretty-Print

```kotlin
val json = Json {
    prettyPrint = true  // ← For debugging only
}
```

### Log Incoming Packets

```kotlin
scope.launch {
    socket.incomingPackets.collect { packet ->
        println("Packet: TYPE=${packet.type}, PAYLOAD=${packet.payload.toString().take(100)}")
        // ... handle packet
    }
}
```

### Inspect Raw Frames

Using browser DevTools or Wireshark to inspect WebSocket frames:

```bash
# macOS/Linux: capture WebSocket traffic
tcpdump -i lo port 8443 -A

# Or use browser DevTools (F12) → Network → WS filter
```

---

## Platform-Specific Networking

### Desktop (JVM)

```kotlin
// jvmMain/kotlin/.../WebSocketClient.kt
val client = HttpClient(CIO) {
    install(WebSockets)
    // CIO = Coroutine I/O
}
```

### Android

```kotlin
// androidMain/kotlin/.../WebSocketClient.kt
val client = HttpClient(OkHttp) {
    install(WebSockets)
    engine {
        // OkHttp-specific config (okhttp3 client)
    }
}
```

### iOS

```kotlin
// iosMain/kotlin/.../WebSocketClient.kt
val client = HttpClient(Darwin) {
    install(WebSockets)
    // Darwin = native iOS networking
}
```

All expose the same API; platform differences are abstracted by Ktor.

---

## See Also

- [Architecture Overview](../architecture/overview.md) — How packets flow through the app
- [Workflows](../workflows/overview.md) — Detailed packet sequences for common flows
- [Data Models](../data-models/overview.md) — Entity structure and relationships
- [DAWSON.md](../DAWSON.md) — Server-side integration patterns

---

*Last Updated: Generated by OpenWiki*
