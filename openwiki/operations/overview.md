---
type: Reference
title: Operations & Setup
description: Building, running, debugging BeakShield on Desktop (JVM), Android, and iOS. Development setup, troubleshooting, and platform-specific guidance.
---

# Operations & Setup

This page covers building, running, and debugging BeakShield across platforms. Each target (Desktop, Android, iOS) has different tools and processes.

---

## Prerequisites

- **JDK 21** (required; configured in `gradle.properties`)
  - Install via sdkman, homebrew, or download from eclipse.temurin.net
  - Verify: `java -version` → should be 21.x
  
- **Kotlin 2.0** (via Gradle; no manual install needed)

- **DAWSON Server** running and accessible
  - Address, port, and auth token ready
  - For local development: run DAWSON on `localhost:8443`
  - See [DAWSON.md](../DAWSON.md) for server setup

- **Git** (for cloning/managing source)

---

## Desktop (JVM) — Primary Development Target

### Building & Running

#### Run from Gradle

```bash
cd /path/to/BeakShield

# Run in dev mode (hot reload enabled)
./gradlew jvmRun

# Watch for Compose changes and reload UI
# (Hot reload plugin will reload composables without rebuild)
```

#### Build JAR

```bash
# Build JAR (includes all dependencies)
./gradlew jvmDistributionReleaseJar

# Output: composeApp/build/compose/jars/BeakShield-X.X.X.jar
# Run: java -jar BeakShield-X.X.X.jar
```

#### Build Native Binary

```bash
# Build OS-specific native binary (macOS, Linux, Windows)
./gradlew jvmDistributionReleaseExe    # Windows: .exe
./gradlew jvmDistributionReleaseDmg    # macOS: .dmg
./gradlew jvmDistributionReleaseAppImage  # Linux: .AppImage
```

### Configuration

Server details are stored in preferences; on first launch:

1. App starts
2. Main screen displayed
3. User navigates to **SystemScreen** (gear icon)
4. Enters server address, port, auth token
5. (Optional) Pastes certificate fingerprint
6. Clicks "Connect"
7. On success, preferences are saved; app reconnects on next launch

**Preferences location:**
- **macOS**: `~/Library/Application Support/BeakShield/`
- **Linux**: `~/.config/BeakShield/` or `~/.local/share/BeakShield/`
- **Windows**: `%APPDATA%/BeakShield/`

### Debugging

#### IDE Setup (IntelliJ IDEA)

1. **Open project**: `File` → `Open` → choose BeakShield root
2. **Configure JDK**: `Project Settings` → `Project` → Select JDK 21
3. **Run configuration**:
   - Gradle task: `jvmRun`
   - Or: `Run` → `Edit Configurations` → Add `Gradle` config

#### IDE Setup (Android Studio)

Android Studio is primarily for Android development but can run desktop too:

1. Open project in Android Studio
2. `Gradle` tab (right side) → `Tasks` → `jvm` → `jvmRun`
3. Or: `Run` → `Run 'jvmRun'`

#### Console Logging

```kotlin
// In any file:
println("Debug message here")
// Appears in: Run window console
```

**For more structured logging, consider adding a logger:**

```kotlin
// Example (not yet in project):
import java.util.logging.Logger
val log = Logger.getLogger("BeakShield")
log.info("Connection state changed")
```

#### Breakpoints

1. Click line number to set breakpoint
2. Run with debugger: `Run` → `Debug 'jvmRun'`
3. Execution pauses at breakpoint; inspect variables

#### Network Debugging

```kotlin
// In WebSocketClient.kt, enable detailed logging:
private val json = Json {
    prettyPrint = true  // Makes JSON output readable
}

// Then in handleIncomingText:
println("RAW: $text")  // Print raw frame
println("PACKET: $packet")  // Print deserialized packet
```

Alternatively, use browser tools or Wireshark to inspect WebSocket frames on port 8443.

---

## Android Development

### Prerequisites

- **Android SDK 33+** (API level; configured in `build.gradle.kts`)
- **Android Studio** (official IDE; includes SDK manager)
- **Android device or emulator** (USB debugging enabled for device)

### Setup

1. **Install Android Studio**
2. **Open BeakShield project** in Android Studio
3. **Install SDKs** via `Tools` → `SDK Manager`:
   - Android API 33+ (Target SDK)
   - Android Build Tools (latest stable)
   - Android Emulator (if emulator-only development)
4. **Create/select device**:
   - Physical device: Connect via USB, enable Developer Mode + USB Debugging
   - Emulator: `Tools` → `AVD Manager` → Create virtual device

### Building

#### Run on Device/Emulator

```bash
# Install and run on connected device/emulator
./gradlew installDebug

# Or from IDE:
# Android Studio → "Run" button (▶) → Select device
```

#### Build APK

```bash
# Build debug APK
./gradlew assembleDebug
# Output: composeApp/build/outputs/apk/debug/composeApp-debug.apk

# Build release APK (requires signing)
./gradlew assembleRelease
# Output: composeApp/build/outputs/apk/release/composeApp-release.apk
```

#### Build AAB (Google Play)

```bash
# Build Android App Bundle for distribution
./gradlew bundleRelease
# Output: composeApp/build/outputs/bundle/release/composeApp-release.aab
```

### Debugging

#### Android Studio Debugger

1. Set breakpoints
2. Click **Debug** button (instead of Run)
3. Debugger attaches; execution pauses at breakpoints
4. Inspect variables, step through code

#### Logcat

```
Android Studio → Logcat tab (bottom)
Filter by package: "com.beakshield"
```

App logs appear here:
```
I/System.out: Debug message here
E/AndroidRuntime: Exception if app crashes
```

#### File Picker Debugging

Android file picker is native (MediaStore). Test by:

1. Running on device or emulator
2. Navigating to SystemScreen
3. Click "Add Directory"
4. Native picker appears; select a directory
5. Verify in app that path was returned

### Configuration

On Android, preferences are stored in `SharedPreferences`:

```
Data → App com.beakshield → Shared preferences → 
  (default SharedPreferences file)
```

---

## iOS Development

### Prerequisites

- **Xcode** (latest version; includes iOS SDK)
- **iOS device or simulator** (Xcode includes simulator)
- **Cocoapods** (optional; Gradle handles dependencies)

### Setup

1. **Install Xcode** from App Store
2. **Accept Xcode license**: `sudo xcode-select --install` + agree
3. **Open Xcode**:
   ```bash
   cd /path/to/BeakShield/iosApp
   open BeakShield.xcodeproj
   ```
   Or generate from Gradle:
   ```bash
   cd /path/to/BeakShield
   ./gradlew generateXcodeProject
   ```

### Building

#### Run on Simulator

```bash
# From Gradle:
./gradlew iosSimulatorArm64

# Or from Xcode:
# Select target: "BeakShield" (not "composeApp")
# Select scheme: iOS Simulator
# Click Run (▶)
```

#### Run on Device

```bash
# From Gradle (requires signing certificate):
./gradlew iosArm64

# Or from Xcode:
# Connect device via USB
# Select device in scheme selector
# Click Run
```

### Debugging

#### Xcode Debugger

1. Set breakpoints in Xcode editor
2. Build & run
3. Execution pauses at breakpoints
4. Inspect variables, step through

#### Console Output

Xcode → Debug → Console tab

App logs appear here.

#### Network Debugging

Xcode has limited WebSocket inspection. Use proxy tools:
- **Proxyman** (macOS): Free, visual HTTP/WebSocket debugging
- **Charles Proxy**: Paid, comprehensive network inspection

### File Picker Debugging

iOS file picker is native (UIDocument picker).

Test by:
1. Running on simulator or device
2. Navigating to SystemScreen
3. Click "Add Directory"
4. Native picker appears
5. Verify path is returned and saved

---

## Configuration Files & Key Paths

### Build Configuration

```
build.gradle.kts                  # Root build file (plugins, versions)
composeApp/build.gradle.kts       # App-specific build config
settings.gradle.kts               # Gradle settings (subprojects)
gradle.properties                 # Gradle properties (memory, JDK path)
gradle/libs.versions.toml         # Dependency versions (catalog)
```

### Source Organization

```
composeApp/src/
├── commonMain/                  # Shared code (~95%)
│   ├── kotlin/com/beakshield/
│   └── resources/               # Shared assets (icons, etc.)
├── androidMain/                 # Android-specific (~5%)
├── iosMain/                     # iOS-specific
├── jvmMain/                     # Desktop-specific
└── commonTest/                  # Shared tests
```

### Version Management

```gradle
# gradle.properties
APP_VERSION=0.1.2                 # User-facing version

# build.gradle.kts
val appVersionProvider = providers.gradleProperty("APP_VERSION")
```

Bumping version:
1. Update `APP_VERSION` in `gradle.properties`
2. Run build
3. Build info is generated in `BuildInfo.kt` at compile time

### Preferences Storage

```
Android: SharedPreferences
iOS: UserDefaults
Desktop: MultiplatformSettings (file-based)
```

Accessed via `BeakShieldApp.preferences`:
```kotlin
BeakShieldApp.preferences.serverAddress = "localhost"
BeakShieldApp.preferences.serverPort = 8443
// etc.
```

---

## Common Build Tasks

### Clean Build

```bash
./gradlew clean                   # Remove build artifacts
./gradlew build                   # Full rebuild
```

### Incremental Build

```bash
./gradlew jvmRun                  # Build if needed, then run
```

### Check Code Style

```bash
./gradlew detekt                  # Static analysis (if configured)
./gradlew ktlint                  # Kotlin linting (if configured)
```

### Run Tests

```bash
./gradlew commonTest              # Run common tests
./gradlew test                    # All tests
```

**Note**: Test coverage is minimal; most testing is manual.

---

## Troubleshooting

### Build Fails: "JDK 21 not found"

**Solution:**
```bash
# Check gradle.properties
cat gradle.properties | grep org.gradle.java.home

# Update to your JDK 21 path:
# macOS/Linux:
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# Or set in gradle.properties:
org.gradle.java.home=/path/to/jdk-21
```

### App Won't Connect to Server

**Checklist:**
- [ ] Server is running (DAWSON server: `./gradlew run`)
- [ ] Server address is correct (default: `localhost`)
- [ ] Server port is correct (default: `8443`)
- [ ] Auth token is correct (from DAWSON server config)
- [ ] Certificate fingerprint is correct (if using custom cert)
- [ ] Firewall isn't blocking port 8443
- [ ] Network is accessible (ping server before debugging app)

**Debug:**
```kotlin
// In WebSocketClient, add logging:
println("Attempting connection to wss://$address:$port/dawson")

// Check connection state in UI:
SystemScreen → shows red "Error" indicator if failed
// Error message should describe the problem
```

### Hot Reload Not Working (Desktop)

**Solution:**
- Hot reload applies to Composable functions only
- Changes to non-Compose code require full rebuild
- Restart `./gradlew jvmRun` if compilation fails

### Android: App Crashes on Startup

**Possible causes:**
- SDK version mismatch
- Missing permissions
- Dependency conflict

**Debug:**
```bash
# View crash logs
adb logcat | grep "com.beakshield"

# Or in Android Studio Logcat tab
Filter: "com.beakshield"
```

### iOS: Build Fails in Xcode

**Common issues:**
- Cocoapods out of sync: `rm -rf Pods Podfile.lock && pod install`
- Xcode cache: `Cmd+Shift+K` (clean build folder)
- iOS SDK mismatch: Select Xcode's iOS SDK in build settings

---

## Local Development Tips

### Running Locally (All Platforms)

```bash
# Start DAWSON server locally
cd /path/to/dawson-server
./gradlew run                     # DAWSON starts on localhost:8443

# In another terminal, run BeakShield:
cd /path/to/BeakShield
./gradlew jvmRun                  # Desktop app starts

# In UI, go to SystemScreen and enter:
# Address: localhost
# Port: 8443
# Token: (from DAWSON config)
```

### Iterating on UI

```bash
# With hot reload enabled, edit Composables and save
# UI reloads automatically without rebuild

# Edit composables in: composeApp/src/commonMain/kotlin/.../
# Changes appear in running app after saving file
```

### Testing Network Issues

```kotlin
// Add logging to WebSocketClient.kt
println("Sending packet: $packet")

// Or enable Wireshark to inspect WebSocket traffic on port 8443
tcpdump -i lo port 8443 -A
```

### Accessing Server Logs

```bash
# DAWSON server logs (depends on server setup)
# Usually printed to stdout or in ./logs/

# Check server is running:
curl https://localhost:8443/health -k
```

---

## CI/CD (GitHub Actions)

BeakShield uses GitHub Actions for automated builds (not yet configured; future feature).

Planned CI pipeline:
```yaml
# .github/workflows/build.yml
- Build on push
- Run tests
- Build JARs and native binaries
- Upload artifacts
- (Optional) Deploy to releases
```

---

## Performance & Optimization

### Startup Time

- Hot reload: Composables hot-reload, no full rebuild
- Incremental compilation: Gradle caches unchanged files
- Lazy initialization: ViewModels are initialized on-demand

### Runtime Performance

- **Message list**: Uses `LazyColumn` (efficient scrolling)
- **StateFlow**: Caches computed flows with `stateIn()`
- **Recomposition**: Compose avoids recomposing unchanged subtrees

### Optimization Tips

- Use `collectAsState()` only for observed flows (not every flow)
- Avoid creating new lambdas in every recomposition (use `rememberCoroutineScope()`)
- Use `LazyColumn`/`LazyRow` for large lists (already done for messages)

---

## Release Process

### Versioning

Update `gradle.properties`:
```gradle
APP_VERSION=0.2.0   # Bump version
```

### Building Release Artifacts

```bash
# Desktop JAR
./gradlew jvmDistributionReleaseJar

# Android APK/AAB
./gradlew assembleRelease
./gradlew bundleRelease

# iOS (requires signing certificate)
# Build in Xcode with signing identity
```

### Signing (Production)

**Android:**
```bash
# Create keystore (one-time)
keytool -genkey -v -keystore release.keystore -keyalg RSA -keysize 2048 -validity 10000 -alias release

# Sign APK
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore release.keystore app-release-unsigned.apk release
```

**iOS:**
- Requires Apple Developer account
- Configure signing in Xcode
- Use Xcode to sign and upload

---

## See Also

- [Architecture Overview](../architecture/overview.md) — Development concepts
- [Integration & Networking](../integration/overview.md) — Server connectivity
- [DAWSON.md](../DAWSON.md) — Server setup and integration

---

*Last Updated: Generated by OpenWiki*
