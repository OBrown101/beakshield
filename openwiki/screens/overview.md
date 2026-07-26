---
type: Reference
title: Screens & UI Architecture
description: Screen hierarchy, Composable patterns, reusable component library (BasicBox, BasicButton, TableView), navigation, and modifier-first conventions.
---

# Screens & UI Architecture

BeakShield's UI is built with **Compose Multiplatform**, following a reusable component library pattern and **MVVM** screen architecture. This page covers screen organization, composable patterns, navigation, and the shared component library.

---

## Screen Hierarchy

The app is organized into a main navigation structure with a left sidebar (rail) and a content area:

```
BaseScreen (root composable, handles navigation)
├── Sidebar Navigation Rail (nav buttons for each screen)
├── MainScreen
│   ├── MainBg (background image)
│   ├── MainHeader (welcome, "Start Chat" button)
│   └── DashboardStatus (status cards - pending feature)
│
├── ChatsScreen
│   ├── ChatsSideRail (chat list, new chat button)
│   ├── ChatsTableView (chat selection grid)
│   └── ChatView
│       ├── AgentProfileView (agent info)
│       ├── ChatBubbleViews (message bubbles with timestamps)
│       ├── InputRequestView (server blocking input dialog)
│       └── ChatInputField (message composition area)
│
├── SystemScreen
│   ├── ServerView (server connection, address, port)
│   ├── ProviderView
│   │   ├── ProviderTableView (list of providers)
│   │   ├── ProviderTableCell (individual provider row)
│   │   └── ProviderConfigView (add/edit provider)
│   └── (other system config screens)
│
├── AgentsScreen (list and details of agents - planned)
├── KnowledgeScreen (MemPalace browser - planned)
├── DecreesScreen (Royal Decrees - planned)
├── SkillsScreen (Claude Skills - planned)
└── ProfileScreen (user settings - planned)
```

### Screen Components

| Screen | ViewModel | Location | Status |
|--------|-----------|----------|--------|
| **Main** | `MainScreenViewModel` | `screens/mainScreen/` | ✅ Working |
| **Chats** | `ChatsScreenViewModel` | `screens/chatsScreen/` | ✅ Working |
| **System** | `SystemScreenViewModel` | `screens/systemScreen/` | ✅ Working |
| **Agents** | `AgentsScreenViewModel` | `screens/` | 🚧 Structure only |
| **Knowledge** | `KnowledgeScreenViewModel` | `screens/` | 🚧 Structure only |
| **Decrees** | `DecreesScreenViewModel` | `screens/` | 🚧 Structure only |
| **Skills** | `SkillsScreenViewModel` | `screens/` | 🚧 Structure only |
| **Profile** | `ProfileScreenViewModel` | `screens/` | 🚧 Structure only |

---

## BaseScreen: Navigation Root

`BaseScreen` is the root composable that manages navigation between screens and displays the sidebar.

```kotlin
@Composable
fun BaseScreen() {
    val navController = rememberNavController()
    val currentRoute by navController.currentBackStackEntryAsState()
    
    // Layout: Sidebar + Content
    Row(modifier = Modifier.fillMaxSize()) {
        // Left sidebar with nav buttons
        NavSidebar(
            onNavClick = { destination ->
                navController.navigate(destination.name)
            }
        )
        
        // Main content area with NavHost
        Box(modifier = Modifier.weight(1f)) {
            NavHost(navController, startDestination = Destination.MAIN.name) {
                composable(Destination.MAIN.name) { MainScreen(...) }
                composable(Destination.CHATS.name) { ChatsScreen(...) }
                composable(Destination.SYSTEM.name) { SystemScreen(...) }
                // ... more screens
            }
        }
    }
}
```

**Key responsibilities:**
- Manages navigation controller and routing
- Renders sidebar with nav buttons and status indicator
- Displays version and connection status
- Shows alert notifications (via `AlertPopupView`)

---

## Composable Naming & Patterns

### Naming Conventions

**Composable names follow a hierarchy:**

| Category | Example | Purpose |
|----------|---------|---------|
| **Basic Components** | `BasicBox`, `BasicButton`, `BasicInputField`, `BasicLabel` | Reusable, generic UI elements |
| **Screen Components** | `ChatScreen`, `SystemScreen`, `MainScreen` | Top-level screen containers |
| **Section/View Components** | `ChatView`, `ProviderTableView`, `ServerConfigView` | Subsections of screens |
| **Row/Cell Components** | `ChatTableCell`, `ProviderTableCell` | Table rows or list items |
| **Dialog/Modal Components** | `InputRequestView`, `ProviderConfigView` | Popups or modal panels |

### Modifier-First Pattern

**All custom composables follow a consistent parameter order:**

```kotlin
@Composable
fun BasicBox(
    modifier: Modifier = Modifier,              // ← ALWAYS FIRST
    bgColor: Color = cardColor,
    borderColor: Color = borderColor,
    borderRadius: Int = 12,
    content: @Composable () -> Unit = {}
) {
    Box(modifier = modifier.then(...))
}

// Usage:
BasicBox(
    modifier = Modifier.fillMaxWidth().padding(10.dp),  // Modifier is first
    bgColor = Color.Blue,
    borderColor = Color.White
)
```

**Why?** Allows flexible sizing/positioning while keeping type-safe defaults for colors, corners, etc.

### Preview Annotations

Many composables include `@Preview` for live preview in IDE:

```kotlin
@Preview
@Composable
fun BasicBox(...) { ... }

@PreviewScreenSizes
@Composable
fun ChatScreen(...) { ... }
```

---

## Reusable Component Library

Located in `src/commonMain/kotlin/com/beakshield/composables/`, these are the building blocks for all screens:

### Boxes & Containers

| Component | File | Purpose |
|-----------|------|---------|
| `BasicBox` | `Boxes.kt` | Rounded box with background, border, optional content |
| `BasicCard` | `Boxes.kt` | Card-style container (variant of BasicBox) |

### Buttons

| Component | File | Purpose |
|-----------|------|---------|
| `BasicRoundedBtn` | `Buttons.kt` | Rounded button with customizable colors, click handler |
| `BasicToggleBtn` | `Buttons.kt` | Toggle button (on/off state) |
| `BasicIconBtn` | `Buttons.kt` | Icon-only button |

### Text Input

| Component | File | Purpose |
|-----------|------|---------|
| `BasicInputField` | `Textfields.kt` | Text input field with label, placeholder, validation |
| `BasicLabel` | `Textfields.kt` | Simple text label |
| `BasicSecureField` | `Textfields.kt` | Password field (masked input) |

### Complex Components

| Component | File | Purpose |
|-----------|------|---------|
| `TableView` | `TableView.kt` | Generic table/grid with columns, sorting, pagination |
| `BasicScrollbar` | `Scrollbar.kt` | Custom scrollbar appearance |
| `Dropdown` | `Dropdown.kt` | Dropdown menu with options |

### Example: BasicBox

```kotlin
@Composable
fun BasicBox(
    modifier: Modifier = Modifier,
    bgColor: Color = cardColor,
    borderColor: Color = borderColor,
    borderRadius: Int = 12,
    content: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(borderRadius.dp))
            .background(bgColor)
            .border(
                BorderStroke(1.dp, borderColor),
                shape = RoundedCornerShape(borderRadius.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
```

**Used throughout the app for consistent styling and rounded corners.**

---

## Screen Implementation: ChatsScreen

`ChatsScreen` is the most complex screen, demonstrating screen ViewModel patterns:

### Structure

```
ChatsScreen
├── ChatsSideRail
│   ├── Chat list (filtered, sorted by recent)
│   └── "New Chat" button
├── ChatsTableView (grid/table showing all chats)
│   └── ChatTableCell (individual chat row)
└── ChatView (when chat selected)
    ├── AgentProfileView (agent info, mode, model)
    ├── Message list (LazyColumn of ChatBubbleViews)
    ├── InputRequestView (if server requests input)
    └── ChatInputField (text input + send button)
```

### ViewModel: ChatsScreenViewModel

```kotlin
class ChatsScreenViewModel : VModel {
    private val scope = CoroutineScope(Dispatchers.Default)
    
    // Selected chat UUID
    private val _chatUUIDSelected = MutableStateFlow<String?>(null)
    val chatUUIDSelected = _chatUUIDSelected.asStateFlow()
    
    // Derived flows (computed)
    val currentChat: StateFlow<Chat?> = combine(
        _chatUUIDSelected,
        BeakShieldApp.dawson.activeChats
    ) { uuid, chats ->
        chats.find { it.uuid == uuid }
    }.stateIn(scope, SharingStarted.Eagerly, null)
    
    // Messages of the selected chat
    val messages: StateFlow<List<Message>> = 
        currentChat.flatMapLatest { chat ->
            if (chat == null) flowOf(emptyList())
            else flowOf(chat.messages)
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())
    
    // Public methods
    fun selectChat(uuid: String) {
        _chatUUIDSelected.value = uuid
    }
    
    fun sendMessage(content: String) {
        val uuid = _chatUUIDSelected.value ?: return
        BeakShieldApp.dawson.sendMessage(uuid, content)
    }
    
    fun deleteChat(uuid: String) {
        BeakShieldApp.dawson.deleteChat(uuid)
    }
    
    fun startNewChat() {
        BeakShieldApp.dawson.createChat()  // Server returns UUID
    }
}
```

### Composable: ChatsScreen

```kotlin
@Composable
fun ChatsScreen(
    modifier: Modifier = Modifier,
    chatsScreenViewModel: ChatsScreenViewModel
) {
    val selectedChatUUID by chatsScreenViewModel.chatUUIDSelected.collectAsState()
    val currentChat by chatsScreenViewModel.currentChat.collectAsState()
    val messages by chatsScreenViewModel.messages.collectAsState()
    
    Row(modifier = modifier.fillMaxSize()) {
        // Left sidebar with chat list
        ChatsSideRail(
            onSelectChat = { uuid -> chatsScreenViewModel.selectChat(uuid) },
            onNewChat = { chatsScreenViewModel.startNewChat() }
        )
        
        // Main content: selected chat or empty state
        if (currentChat != null) {
            ChatView(
                chat = currentChat,
                messages = messages,
                onSendMessage = { content -> 
                    chatsScreenViewModel.sendMessage(content)
                }
            )
        } else {
            EmptyState()
        }
    }
}
```

---

## Navigation & Routing

### Destination Enum

```kotlin
enum class Destination {
    MAIN,
    CHATS,
    AGENTS,
    KNOWLEDGE,
    DECREES,
    SKILLS,
    PROFILE,
    SYSTEM;

    val label: String
        get() = when(this) {
            MAIN -> "Dawson"
            CHATS -> "Chats"
            AGENTS -> "Agents"
            // ... etc
        }
    
    val icon: DrawableResource
        get() = when(this) {
            MAIN -> Res.drawable.nav_btn_dawson
            CHATS -> Res.drawable.nav_btn_chats
            // ... etc
        }
}
```

### Navigation from Composables

```kotlin
val navController = rememberNavController()

Button(onClick = {
    navController.navigate(Destination.CHATS.name)
}) {
    Text("Go to Chats")
}
```

### ViewModels Access NavController

No direct access; instead, ViewModels provide callbacks that Composables execute:

```kotlin
// ViewModel method
fun selectChat(uuid: String) {
    _chatUUIDSelected.value = uuid
}

// Composable navigates on state change
Button(onClick = {
    viewModel.selectChat(chatUuid)
    navController.navigate(Destination.CHATS.name)
})
```

---

## State Collection in Composables

All Composables observe ViewModel flows using `collectAsState()`:

```kotlin
@Composable
fun ChatView(viewModel: ChatsScreenViewModel) {
    // Convert Flow<T> to State<T> for composition
    val messages by viewModel.messages.collectAsState()
    val selectedUUID by viewModel.chatUUIDSelected.collectAsState()
    
    // Recompose when these values change
    LazyColumn {
        items(messages) { msg ->
            ChatBubble(msg)
        }
    }
}
```

**Key point**: `collectAsState()` returns a `State<T>` that only recomposes the Composable when the value actually changes, not when the Flow emits the same value.

---

## Theming & Colors

Global colors defined in `GlobalProperties.kt`:

```kotlin
val primaryColor = Color(0xFF...)
val backgroundColor = Color(0xFF...)
val cardColor = Color(0xFF...)
val borderColor = Color(0xFF...)
val dawsonGold = Color(0xFF...)
val lightGreenColor = Color(0xFF...)
val dangerColor = Color(0xFF...)
```

Used throughout composables for consistent theming. No theme system yet (Compose Material3 theming could be added).

---

## Responsive Layout

BeakShield is designed for desktop first (wide screens), with some mobile support:

- **Main content**: `Box` with `fillMaxSize()` for full-height responsiveness
- **Sidebars**: Fixed widths (e.g., 250.dp for ChatsScreen sidebar)
- **Scrollable areas**: `verticalScroll()` with `LazyColumn` for large lists
- **Padding/spacing**: Using `.dp` units (device-independent pixels) for consistent spacing across platforms

**Not yet implemented**: Adaptive layouts for very small screens (tablets/phones would benefit from further optimization).

---

## Text & Typography

Composables use Material3 Text styles (optional) or manual sizing:

```kotlin
Text(
    "Hello",
    fontSize = 16.sp,
    fontWeight = FontWeight.Bold,
    textAlign = TextAlign.Center
)
```

**Font family:** Currently using system defaults; consider adding custom fonts for branding.

---

## Error Handling & User Feedback

### Alert Notifications

The `AlertPopupView` shows alerts/errors:

```kotlin
@Composable
fun ChatView() {
    val notifications by BeakShieldApp.notifications.activeAlerts.collectAsState()
    
    Box {
        // ... main content
        
        // Alert popups
        notifications.forEach { alert ->
            AlertPopupView(alert)
        }
    }
}
```

### Connection Status Indicator

Shown in the sidebar:

```kotlin
val connState by BeakShieldApp.dawson.connectionState.collectAsState()

Icon(
    painter = painterResource(...),
    tint = connState.color,  // Green = connected, Red = error
    contentDescription = connState.message
)
```

---

## Accessibility

Composables should include:
- `contentDescription` on icons and images
- Semantic grouping with `Box`, `Column`, `Row`
- Adequate touch target sizes (48.dp minimum for mobile)

**Currently minimal**: Beakshield has basic accessibility but could benefit from more semantic annotations.

---

## See Also

- [Architecture Overview](../architecture/overview.md) — MVVM and state management patterns
- [Data Models](../data-models/overview.md) — Entity structures displayed by screens
- [Workflows](../workflows/overview.md) — User flows and state transitions
- [Operations & Setup](../operations/overview.md) — Running and debugging on each platform

---

*Last Updated: Generated by OpenWiki*
