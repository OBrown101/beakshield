---
type: Reference
title: Key Workflows
description: User flows for chatting, provider configuration, permission management, state sync, and error handling with detailed state transitions.
---

# Key Workflows

This page documents the major user interactions and state flows in BeakShield: sending messages, configuring providers, managing permissions, syncing state, and handling errors.

---

## Flow: Send a Chat Message

User types a message and presses "Send". Here's what happens:

### Sequence

```
1. User types "Hello" in ChatInputField and presses Enter

2. ChatView calls:
   chatsScreenViewModel.sendMessage("Hello")

3. ChatsScreenViewModel calls:
   BeakShieldApp.dawson.sendMessage(chatUUID, "Hello")

4. Dawson.sendMessage() creates a USER_DATA packet:
   {
     "type": "USER_DATA",
     "payload": {
       "messageContent": "Hello",
       "chatUUID": chatUUID,
       "sourceUUID": currentUserUUID,
       "destinationUUID": agentUUID,
       // ... more metadata
     }
   }

5. WebSocketClient sends packet over WebSocket to server

6. Server processes message, routes to agent, agent responds

7. Server broadcasts USER_DATA response:
   {
     "type": "USER_DATA",
     "payload": {
       "uuid": "msg-uuid-1",
       "messageContent": "Hello",
       "sourceUUID": currentUserUUID,
       "destinationUUID": agentUUID,
       "timestamp": 1234567890
     }
   }

8. WebSocketClient receives, deserializes, emits to incomingPackets SharedFlow

9. Dawson.incomingPackets.collect() is notified

10. Dawson routes to handleUserData(userData):
    - Creates Message object
    - Finds Chat by chatUUID
    - Calls chat.addPendingMessage(msg)
    - Updates Chat's _messages StateFlow

11. ChatsScreenViewModel observes Chat.messages via:
    val messages = currentChat.flatMapLatest { it.messages }
                       .stateIn(scope, SharingStarted.Eagerly, emptyList())

12. messages StateFlow emits new list

13. ChatView observes messages via:
    val messages by viewModel.messages.collectAsState()

14. Composable re-renders LazyColumn with new ChatBubble for "Hello"
```

### State Diagram

```
[User typing] → [ChatInputField filled] → [User presses Send]
                                             ↓
                                    [ViewModel.sendMessage()]
                                             ↓
                                    [Dawson.sendMessage()]
                                             ↓
                                    [WebSocket sends packet]
                                             ↓
                                    [Server processes]
                                             ↓
                                    [Server responds]
                                             ↓
                                    [WebSocket receives]
                                             ↓
                                    [Dawson.handleUserData()]
                                             ↓
                                    [Chat._messages StateFlow updated]
                                             ↓
                                    [ViewModel.messages recomputes]
                                             ↓
                                    [Composable re-renders]
                                             ↓
                                    [Message bubble appears]
```

### Key Points

- **Optimistic UI**: Client immediately adds message to `Chat._messages`; no wait for server confirmation
- **Delivery tracking**: Server sends delivery confirmation; client marks message `delivered = true`
- **Streaming**: For multi-chunk responses, each chunk triggers a re-render; final chunk marks `numChunks`
- **Retry**: If user presses Retry button, `sendMessage()` is called again with same content

---

## Flow: Configure a New Provider

User wants to add OpenAI and set it as default. Here's what happens:

### Sequence

```
1. User navigates to SystemScreen

2. SystemScreenViewModel observes:
   val providers = dawson.activeProviders.collectAsState()

3. User clicks "Add Provider" → ProviderConfigView opens dialog

4. User enters API key, selects OpenAI

5. User clicks "Save"

6. ProviderConfigView calls:
   systemScreenViewModel.addProvider(ProviderType.OPENAI, apiKey)

7. SystemScreenViewModel calls:
   BeakShieldApp.dawson.updateProvider(provider)

8. Dawson.updateProvider() creates CONFIG_DATA packet:
   {
     "type": "CONFIG_DATA",
     "payload": {
       "action": "UPDATE_PROVIDER",
       "provider": {
         "type": "OPENAI",
         "apiKey": "sk-...",
         "availableModels": [],
         "defaultModelID": ""
       }
     }
   }

9. WebSocketClient sends to server

10. Server validates API key, fetches available models from OpenAI API

11. Server broadcasts CONFIG_DATA response:
    {
      "type": "CONFIG_DATA",
      "payload": {
        "action": "PROVIDER_UPDATED",
        "provider": {
          "type": "OPENAI",
          "apiKey": "sk-...", (redacted or not sent back)
          "availableModels": [
            { "id": "gpt-4", "contextWindow": 8192, ... },
            { "id": "gpt-4-turbo", "contextWindow": 128000, ... },
            // ...
          ],
          "defaultModelID": ""
        }
      }
    }

12. WebSocketClient receives, Dawson routes to handleConfigData()

13. handleConfigData() updates Dawson._activeProviders StateFlow

14. SystemScreenViewModel observes via:
    val providers = dawson.activeProviders.collectAsState()

15. ProviderTableView re-renders with new provider in list

16. User selects "Set as Default" for gpt-4-turbo

17. User clicks "Save"

18. ViewModel calls:
    dawson.updateProvider(provider.copy(defaultModelID = "gpt-4-turbo"))

19. Server updates, broadcasts back

20. UI updates showing gpt-4-turbo as default
```

### State Diagram

```
[SystemScreen opens]
         ↓
[User clicks "Add Provider"]
         ↓
[ProviderConfigView dialog opens]
         ↓
[User enters API key, selects OpenAI]
         ↓
[User clicks "Save"]
         ↓
[ViewModel.addProvider()]
         ↓
[Dawson.updateProvider()]
         ↓
[WebSocket sends CONFIG_DATA]
         ↓
[Server validates, fetches models]
         ↓
[Server responds with models list]
         ↓
[Dawson.handleConfigData()]
         ↓
[_activeProviders StateFlow updated]
         ↓
[ProviderTableView re-renders with new provider]
         ↓
[User selects default model]
         ↓
[ViewModel calls updateProvider() again]
         ↓
[Server updates, broadcasts back]
         ↓
[UI shows default model selection]
```

### Key Points

- **Multi-step**: API key + model selection happens in two updates
- **Server-fetched models**: Available models come from server (server calls provider's API)
- **State sync**: After each update, full provider object is sent back to ensure client stays in sync
- **No sensitive data**: API key may not be echoed in response (security best practice)

---

## Flow: Sync Agent State

Dawson periodically polls for state changes. Here's what happens:

### Periodic Sync (Every N Seconds)

```
1. Dawson.init() starts sync timer:
   Timer(delay = 3000, repeat = 3000) {
       fetchSyncState()
   }

2. fetchSyncState() sends SYNC_STATE request:
   {
     "type": "SYNC_STATE",
     "payload": {
       "requestedEntityTypes": ["AGENT", "CHAT"],
       "lastSyncTimestamp": 1234567890
     }
   }

3. Server responds with SYNC_STATE:
   {
     "type": "SYNC_STATE",
     "payload": {
       "agentUUIDs": ["agent-1", "agent-2"],  // Agents to refresh
       "chatUUIDs": ["chat-1"],                // Chats to refresh
       "reason": "agent-state-changed"
     }
   }

4. Dawson.handleSyncState() fetches full entities:
   for each agentUUID in agentUUIDs {
       queryAgent(agentUUID)  // Fetch full agent data
   }

5. Server sends AGENT_DATA for each agent:
   {
     "type": "AGENT_DATA",
     "payload": {
       "uuid": "agent-1",
       "state": "RESPONDING",  // Changed from THINKING
       "model": { ... },
       // ...
     }
   }

6. Dawson.handleAgentData() updates _activeAgents StateFlow

7. ViewModels observing activeAgents recompute

8. Composables re-render (e.g., Agent state indicator changes)
```

### State Diagram

```
[Timer fires (every 3s)]
         ↓
[Dawson.fetchSyncState()]
         ↓
[WebSocket sends SYNC_STATE request]
         ↓
[Server checks for changes]
         ↓
[Server sends SYNC_STATE response with changed UUIDs]
         ↓
[Dawson.handleSyncState()]
         ↓
[For each changed entity UUID, send query]
         ↓
[Server responds with full entity AGENT_DATA/CHAT_DATA/etc.]
         ↓
[Dawson.handleAgentData() etc. updates StateFlow]
         ↓
[ViewModels recompute derived flows]
         ↓
[Composables re-render]
```

### Key Points

- **Event-driven optional**: Currently polling; ideal would be server-push (WebSocket events)
- **Batch fetches**: SYNC_STATE tells client which entities changed; client fetches only those
- **Timestamp tracking**: `lastSyncTimestamp` helps server detect changes since last sync
- **No polling if idle**: Could optimize: pause sync timer when app in background

---

## Flow: Receive User Input Request

Server requests user input (blocking call). Here's the flow:

### Sequence

```
1. Agent is processing and needs user input (e.g., "Do you approve?")

2. Server sends USER_INPUT_REQUEST packet:
   {
     "type": "USER_INPUT_REQUEST",
     "payload": {
       "uuid": "input-req-1",
       "prompt": "Do you approve?",
       "inputType": "YES_NO",  // or TEXT, MULTIPLE_CHOICE
       "options": ["Yes", "No"]
     }
   }

3. WebSocketClient receives, Dawson routes to handleUserInputRequest()

4. Dawson adds to _pendingInputRequests StateFlow

5. ChatView observes:
   val pendingInputs by dawson.pendingInputRequests.collectAsState()

6. If pendingInputs is not empty, InputRequestView appears:
   - Shows prompt: "Do you approve?"
   - Shows buttons: "Yes", "No"

7. User clicks "Yes"

8. InputRequestView calls:
   chatsScreenViewModel.respondToInputRequest("input-req-1", "Yes")

9. ViewModel calls:
   Dawson.sendUserInputResponse(inputRequestUUID, response)

10. Dawson creates USER_INPUT_RESPONSE packet:
    {
      "type": "USER_INPUT_RESPONSE",
      "payload": {
        "requestUUID": "input-req-1",
        "response": "Yes"
      }
    }

11. WebSocketClient sends to server

12. Server unblocks agent, resumes processing with user's response

13. Server sends USER_INPUT_RESPONSE confirmation (optional)

14. Dawson removes from _pendingInputRequests

15. InputRequestView disappears, chat resumes normal flow
```

### State Diagram

```
[Agent needs input]
         ↓
[Server sends USER_INPUT_REQUEST]
         ↓
[WebSocketClient receives]
         ↓
[Dawson.handleUserInputRequest()]
         ↓
[Add to _pendingInputRequests StateFlow]
         ↓
[InputRequestView appears]
         ↓
[User selects response]
         ↓
[User clicks button]
         ↓
[ViewModel.respondToInputRequest()]
         ↓
[Dawson.sendUserInputResponse()]
         ↓
[WebSocket sends USER_INPUT_RESPONSE]
         ↓
[Server unblocks, resumes]
         ↓
[Dawson removes from _pendingInputRequests]
         ↓
[InputRequestView disappears]
```

### Key Points

- **Blocking on server**: While waiting for input, agent is blocked; no other work happens
- **UI dialog**: BeakShield displays the request as a modal/overlay
- **Types**: YES_NO, TEXT, MULTIPLE_CHOICE, etc. supported
- **Timeout handling**: If user doesn't respond within timeout, server may auto-dismiss or error

---

## Flow: Handle Connection Error

WebSocket disconnects or auth fails. Here's what happens:

### Disconnection

```
1. Network drops or server shuts down

2. WebSocketClient.session.incoming loop throws exception

3. WebSocketClient.setConnState(ERROR) with message:
   ServerConnState(state = ERROR, msg = "Connection lost")

4. _connectionState StateFlow is updated

5. Dawson and all ViewModels observe connectionState

6. MainScreen/BaseScreen shows red "Disconnected" indicator

7. All UI is still responsive; no data is cleared yet

8. User is prompted: "Connection lost. Reconnect?" button
```

### Reconnection

```
1. User clicks "Reconnect" or re-enters server details in SystemScreen

2. ViewModel calls:
   Dawson.connect(address, port, authToken, fingerprint)

3. WebSocketClient.connect() attempts new connection

4. If successful:
   setConnState(CONNECTED)
   Restart sync timer
   Fetch all data (SYNC_STATE with empty lastSync)

5. If fails:
   setConnState(ERROR) with new error message
   User can retry or fix server details
```

### Auth Failure

```
1. User provides wrong auth token

2. Server rejects WebSocket upgrade with 401

3. WebSocketClient catches, calls:
   setConnState(ERROR, msg = "Unauthorized")

4. UI shows: "Connection Error: Unauthorized - check auth token"

5. User must correct token in SystemScreen and reconnect
```

### State Diagram

```
[CONNECTED state]
         ↓
[Network error / server down]
         ↓
[ERROR state + error message]
         ↓
[UI shows red "Disconnected"]
         ↓
[User clicks "Reconnect"]
         ↓
[Attempt new connection]
         ↓
[Success: CONNECTED] OR [Failure: ERROR]
```

### Key Points

- **No data loss**: Disconnection doesn't clear local state; UI remains interactive
- **Auto-reconnect**: Could implement (not currently)
- **Sync on reconnect**: After reconnect, full data sync happens
- **User visibility**: Connection state is always visible in top bar

---

## Flow: Message Retry

User's message failed to send. Here's what happens:

### User Retry

```
1. Message sent but delivery never confirmed (network timeout)

2. ChatBubble shows "Retry" button (appears after timeout)

3. User clicks "Retry"

4. ChatBubble calls:
   chatsScreenViewModel.sendMessage(originalContent)

5. ViewModel calls:
   Dawson.sendMessage(chatUUID, originalContent)

6. Process same as normal "Send Message" flow

7. If server receives duplicate (same content, same timestamp):
   Server may deduplicate or return error
   
8. If new message sent successfully:
   New UUID assigned, new delivery tracking

9. UI updates with successful delivery
```

### State Diagram

```
[User sends message]
         ↓
[Timeout: no delivery confirmation]
         ↓
[ChatBubble shows "Retry" button]
         ↓
[User clicks "Retry"]
         ↓
[sendMessage() called again]
         ↓
[WebSocket sends packet]
         ↓
[Server processes]
         ↓
[Success: delivery confirmed] OR [Error: shown to user]
```

### Key Points

- **Delivery tracking**: Message tracked by UUID; timeout triggers retry affordance
- **Idempotency**: Server should handle retries gracefully (deduplicate)
- **User responsibility**: Manual retry; not automatic

---

## Common State Transitions

### Agent State Machine

```
READY ↔ AWAITING_INPUT
  ↓
PROCESSING
  ├→ THINKING     (extended thinking)
  ├→ ACTING       (tool use)
  └→ RESPONDING   (generating response)
  ↓
READY

ERROR → (user retry or manual reset)
```

### Chat State Transitions

```
[Empty] → [User sends first message] → [Agent responds] → [Active chat]
                                            ↓
                                    [Further messages flow freely]
                                            ↓
                                    [User deletes chat] → [Deleted]
```

### Provider Configuration State

```
[Not configured] → [User adds API key] → [Server fetches models] → [Configured]
                                                                         ↓
                                    [User selects default model] → [Ready to use]
```

---

## Error Handling Patterns

### Network Errors

- **WebSocket disconnect**: Show error, allow reconnect
- **Packet send timeout**: Show retry button on message bubble
- **Server rejection**: Display server error message to user

### Validation Errors

- **Invalid API key**: Server returns CONFIG_DATA with error; show error notification
- **Invalid directory path**: Server returns error; suggest valid paths

### State Inconsistencies

- **Stale data**: Use SYNC_STATE periodically to refresh
- **Orphaned entity**: (e.g., chat references deleted agent) — handle gracefully, hide/delete chat

---

## See Also

- [Architecture Overview](../architecture/overview.md) — State management and reactive flow
- [Data Models](../data-models/overview.md) — Entity structure and relationships
- [Integration & Networking](../integration/overview.md) — Packet types and network details

---

*Last Updated: Generated by OpenWiki*
