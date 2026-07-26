---
type: Quickstart
title: BeakShield Code Wiki
description: A guide to BeakShield, a Kotlin Multiplatform companion app for DAWSON AI orchestration. Covers architecture, UI patterns, data flow, and workflows.
---

# BeakShield Code Wiki

**BeakShield** is a cross-platform desktop and mobile companion application for the DAWSON AI orchestration server. It provides a clean, intuitive interface for managing AI agents, conversations, provider configurations, permissions, and system knowledge—turning DAWSON's powerful backend into an approachable tool.

This wiki documents BeakShield's code architecture, key workflows, UI patterns, data models, and operational guidance for developers working on the application.

---

## Quick Navigation

- **[Architecture Overview](./architecture/overview.md)** — Kotlin Multiplatform setup, MVVM, data flow, and WebSocket integration
- **[Screens & UI](./screens/overview.md)** — Screen hierarchy, composable patterns, and the reusable component library  
- **[Data Models](./data-models/overview.md)** — Domain entities (Agent, Chat, Message, Provider, User) and serialization
- **[Workflows](./workflows/overview.md)** — Key user flows: chatting, provider setup, permissions, sync, and state management
- **[Integration & Networking](./integration/overview.md)** — WebSocket client, server connectivity, authentication, and packet routing
- **[Operations & Setup](./operations/overview.md)** — Building, running, debugging, and development setup for each target platform

---

## What BeakShield Does

### Current Features (Working)

- **Multi-chat Interface**: Each conversation has its own dedicated Squirebot agent context
- **Provider Management**: Switch between OpenAI, Anthropic, and Ollama models without leaving the app
- **Secure Connectivity**: WebSocket with TLS certificate pinning to DAWSON server
- **Permission Visibility**: Interact with DAWSON's permission system through a UI layer
- **Directory Workspaces**: Configure which directories DAWSON agents can access
- **Model Selection**: Choose preferred and default models per provider and per conversation
- **System Configuration**: Manage server settings, networking, and agent state directly in the app

### Planned Features

- DAWSON dashboard and live activity monitoring
- Knowledge browser (browse, search, inspect MemPalace)
- Royal Decree management
- Claude Skills library integration
- User profiles and agent hierarchy visualization
- Voice interaction
- Background task management

---

## Project Structure

```
composeApp/src/
├── commonMain/
│   ├── kotlin/com/beakshield/
│   │   ├── BeakShieldApp.kt          # Singleton app state holder
│   │   ├── App.kt                    # Compose entry point
│   │   ├── Preferences.kt            # Settings persistence
│   │   ├── GlobalProperties.kt       # Theme colors, constants
│   │   │
│   │   ├── composables/              # Reusable UI components (Boxes, Buttons, Tables, etc.)
│   │   ├── screens/                  # Screen implementations (Main, Chats, System, etc.)
│   │   ├── tablecells/               # Cell ViewModels for table rows
│   │   ├── viewModels/               # Screen ViewModels extending VModel base
│   │   ├── notifications/            # Alert and notification system
│   │   │
│   │   ├── dawson/                   # Server data model (Agents, Chats, Messages, Providers, Users)
│   │   ├── websocket/                # WebSocket client and packet types
│   │   │
│   ├── commonTest/                   # Shared test code
├── androidMain/                      # Android-specific platform code
├── iosMain/                          # iOS-specific platform code
├── jvmMain/                          # JVM Desktop entry point
└── jsMain/, wasmJsMain/, webMain/   # Web targets (disabled/planned)

build.gradle.kts                       # Kotlin Multiplatform configuration
```

---

## Key Concepts

### MVVM + Flow Reactive Pattern

BeakShield uses **MVVM (Model-View-ViewModel)** with **Kotlin Flow** for state management:

- **Models**: Entities in `com.beakshield.dawson.*` (Agent, Chat, Message, etc.) are serialized to/from DAWSON server
- **ViewModels**: Each screen has a `*ScreenViewModel` extending `VModel` interface, exposing `StateFlow<T>` for UI state
- **Views**: Composable functions observe ViewModels via `collectAsState()` and update reactively

Example flow:
```
User clicks "Send Message" 
  → ChatView calls chatsScreenViewModel.sendMessage(...)
    → ViewModel updates local state
      → Calls Dawson.sendMessage(...) 
        → WebSocket sends USER_DATA packet
          → Server responds with CHAT_DATA or USER_DATA updates
            → Packet handler updates Dawson's activeChats/activeAgents
              → ViewModel's derived flows recompute
                → Composables re-render with new messages
```

### Singleton Service Locator: BeakShieldApp

All top-level services are accessed through the `BeakShieldApp` singleton:

```kotlin
// Access from any ViewModel or Composable:
BeakShieldApp.preferences        // Settings & app configuration
BeakShieldApp.dawson            // Server data and WebSocket connection
BeakShieldApp.notifications     // Alert/popup system
BeakShieldApp.chatsScreenViewModel  // Screen ViewModels
// ... more ViewModels
```

This avoids dependency injection overhead in a desktop/mobile client and provides a single initialization point (`onCreate()`).

### WebSocket Packet Routing

All server communication flows through `Dawson` class:

1. **WebSocketClient** maintains connection and deserialization
2. **Dawson** routes incoming packets by type (SYNC_STATE, AGENT_DATA, CHAT_DATA, USER_DATA, etc.)
3. Handlers update `StateFlow` collections (`activeChats`, `activeAgents`, `activeProviders`)
4. ViewModels subscribe to these flows; Composables subscribe to ViewModels
5. Sync polling refreshes stale entities; UI changes are pushed back via `sendMessage`, `updateAgent`, etc.

---

## Getting Started

### Prerequisites

- **JDK 21** (required by project; set in `gradle.properties`)
- **Kotlin 2.0** (via Gradle)
- **DAWSON Server** running somewhere (localhost or remote; requires auth token and optional TLS fingerprint)

### For Desktop (JVM)

```bash
# Run the desktop app
./gradlew jvmRun

# Build JAR or native binary
./gradlew jvmDistributionReleaseJar
```

### For Android

```bash
# Requires Android Studio and Android SDK
./gradlew installDebug   # Install to connected device/emulator
# Or: open in Android Studio → Gradle → Tasks → android → installDebug
```

### For iOS

```bash
# Requires Xcode and iOS SDK
# Best: open iosApp/ in Xcode and build/run from there
# Or: ./gradlew iosSimulatorArm64Binaries
```

See [Operations & Setup](./operations/overview.md) for detailed build, debug, and development workflows.

---

## Key Files & First Reads

**For new developers**, read these in order:

1. **[README.md](../README.md)** — Feature overview and philosophy (5 min)
2. **[Architecture Overview](./architecture/overview.md)** — Data flow, Kotlin Multiplatform, and MVVM pattern (10 min)
3. **[Screens Overview](./screens/overview.md)** — Screen hierarchy and Composable patterns (10 min)
4. **[Integration Overview](./integration/overview.md)** — WebSocket, server setup, and packet types (10 min)
5. **[DAWSON.md](../DAWSON.md)** — Detailed integration guide with DAWSON server (reference)

**By component:**

- Changing UI components or layouts? → [Screens & UI](./screens/overview.md)
- Adding a new server entity or field? → [Data Models](./data-models/overview.md)
- Modifying chat flow or state? → [Workflows](./workflows/overview.md)
- Server connectivity or auth issues? → [Integration & Networking](./integration/overview.md)
- Build or platform-specific issues? → [Operations & Setup](./operations/overview.md)

---

## Architecture Highlights

### Kotlin Multiplatform (KMP)

BeakShield uses **Kotlin Multiplatform Compose** to target:
- **JVM/Desktop** (primary focus, via Compose Desktop)
- **Android** (via Compose)
- **iOS** (via Compose Multiplatform)
- **Web** (via Compose for Web; currently disabled)

**Common code**: ~95% of the app logic and UI is in `commonMain/`
**Platform-specific**: Only networking (Ktor client plugins), file pickers, and preferences APIs vary by platform.

### Reactive State Management

- **Kotlin Flow & StateFlow** for observable, suspendable state
- **`combine()`, `flatMapLatest()`, `map()`** for derived/computed flows
- **`stateIn(scope, SharingStarted.Eagerly, initialValue)`** to cache computed flows

No Redux, Mobx, or heavy DI framework—just Kotlin's coroutine-aware primitives.

### UI Composables

- **Compose Multiplatform** for declarative, composable UI
- **Reusable component library** in `composables/` (BasicBox, BasicButton, BasicInputField, TableView, etc.)
- **Screen ViewModels** bridge state and UI, each extending `VModel` interface
- **Modifier-first pattern**: modifier is always the first parameter in custom composables

---

## Common Workflows

### Add a New Server Entity Field

1. Update the data class in `src/commonMain/kotlin/com/beakshield/dawson/Entity.kt`
2. Update the `@Serializable` annotation if needed
3. Update any ViewModel that derives state from that entity
4. Update the Composable(s) that display it
5. No server changes needed; DAWSON will send the new field in packets

### Chat Flow: Send a Message

1. User types in ChatView and presses send
2. ChatView calls `chatsScreenViewModel.sendMessage(...)`
3. ViewModel calls `Dawson.sendMessage(...)` with the message content
4. `Dawson` sends a USER_DATA packet via WebSocket
5. Server processes, creates Message, broadcasts back
6. Packet handler in Dawson updates `activeChats` StateFlow
7. ViewModel's derived `messages` flow recomputes
8. Composable re-renders with new message bubble

### Switch Providers or Models

1. User selects a new provider in SystemScreen
2. ViewModel sends CONFIG_DATA packet to DAWSON
3. Server validates, updates provider config, broadcasts back
4. Dawson's `activeProviders` StateFlow updates
5. All screens observing that provider reactively update their model selection UI

---

## Development & Debugging

- **Live Reload**: Enabled via `composeHotReload` plugin (changes to Composables reload without full rebuild)
- **Logging**: Use `println()` throughout; both Dawson and WebSocketClient log key state transitions
- **Server Connection**: Check `Dawson.connectionState` StateFlow in UI for connection status
- **Network Debugging**: Ktor client logs WebSocket frames; useful for packet inspection

See [Operations & Setup](./operations/overview.md) for platform-specific debug setup (Android Studio, Xcode, etc.).

---

## Important Notes

### Testing

Minimal test coverage currently (see `commonTest/`). Most testing is manual through the running app.

### First Launch

On first launch, `BeakShieldApp.onCreate()` initializes:
- Preferences (defaults to `isFirstLaunch = true`, then sets to `false`)
- Dawson singleton (creates WebSocket client; does not auto-connect until user enters server details)
- Notifications system
- All ViewModels (lazy-initialized)

### Performance Considerations

- **StateFlow subscriptions are eager** by default; derived flows using `combine()` are cached with `stateIn()` to avoid recomputation
- **Scroll performance**: ChatView uses `LazyColumn`; large message lists may need pagination or virtualization (not yet implemented)
- **Network**: Sync polling runs every few seconds; consider batching if sync becomes slow

### Platform Differences

- **Android/iOS**: File picker uses platform APIs; Android uses MediaStore, iOS uses UIDocument picker
- **Desktop**: Uses native file dialog (AWT/Swing compatibility layer)
- **Preferences**: Android uses SharedPreferences; iOS uses UserDefaults; Desktop uses file-based MultiplatformSettings
- **WebSocket**: Desktop uses Ktor CIO client; Android uses OkHttp; iOS uses Darwin; all unified by Ktor's abstraction

---

## Future Work & Backlog

### Planned Features

- **Knowledge Browser**: Browse and search MemPalace entries
- **Royal Decree Management**: Create, edit, delete agent directives
- **Skills Library**: Integrate Claude Skills into the UI
- **Dashboard**: Real-time monitoring of agent activity, task status, memory usage
- **Agent Hierarchy Visualization**: Visual graph of agent relationships and delegations
- **Voice Interaction**: Speak to agents, receive voice responses
- **Web Target**: Full web app version (currently disabled; would use Compose for Web)

### Known Limitations

- No pagination for large chat histories (may cause scroll lag)
- No search/filter in chat list (marked as "Planned")
- Knowledge browser not yet implemented
- Mobile platforms (Android/iOS) less polished than Desktop (design focus has been on JVM)

---

## Conventions & Standards

### Naming

- **Composables**: `PascalCase`, prefixed by scope (e.g., `BasicBox`, `ChatView`, `ChatTableCell`)
- **ViewModels**: `*ScreenViewModel` (e.g., `ChatsScreenViewModel`)
- **Data Classes**: `PascalCase` matching domain (e.g., `Agent`, `Chat`, `Message`)
- **Private StateFlows**: `_camelCase` (mutable); exposed as `camelCase` (immutable)

### Modifier Placement

Always put `modifier: Modifier = Modifier` as the **first parameter** in custom composables:

```kotlin
@Composable
fun BasicBox(
    modifier: Modifier = Modifier,
    bgColor: Color = cardColor,
    content: @Composable () -> Unit = {}
) { ... }
```

### Coroutine Scope

ViewModels create their own scope on `Dispatchers.Default`:
```kotlin
private val scope = CoroutineScope(Dispatchers.Default)
```

Composables use `rememberCoroutineScope()` for launch blocks.

---

## Getting Help

- **DAWSON Integration**: See [DAWSON.md](../DAWSON.md) for detailed server integration patterns
- **UI Issues**: Check [Screens & UI](./screens/overview.md) for composable library and screen patterns
- **Data Flow Questions**: See [Workflows](./workflows/overview.md) and [Architecture](./architecture/overview.md)
- **Platform-Specific Problems**: See [Operations & Setup](./operations/overview.md)

---

*Last Updated: Generated by OpenWiki*
