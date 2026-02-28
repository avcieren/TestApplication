# CLAUDE.md — AI Assistant Guide for TestApplication

This file provides guidance for AI assistants (Claude and others) working on this codebase.

---

## Project Overview

**TestApplication** is an Android mobile application written in Kotlin. It functions as a **device management utility** for Vera smart home devices. The app:

- Fetches a list of devices from a remote API
- Displays them in a scrollable RecyclerView list
- Allows users to view device details
- Supports in-place platform name editing
- Supports device deletion via long-press

---

## Technology Stack

| Category | Technology |
|---|---|
| Language | Kotlin |
| Platform | Android (Min SDK 23 / Target SDK 31) |
| Architecture | MVVM (partial implementation) |
| Build System | Gradle 7.2 with Kotlin DSL |
| HTTP Client | HttpURLConnection (manual) + Retrofit 2 (included, unused) |
| JSON Parsing | org.json (manual) + Gson (included, unused) |
| Async | Kotlin Coroutines 1.5.2 + RxJava 2 (included, unused) |
| Database | Room 2.4.2 (included, unused) |
| Image Loading | Glide 4.9.0 |
| Navigation | AndroidX Navigation 2.4.1 |
| UI | AndroidX, Material Components, ConstraintLayout, RecyclerView |
| Testing | JUnit 4, AndroidX Test, Espresso |

---

## Project Structure

```
TestApplication/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/erenavci/testapplication/
│   │   │   │   ├── model/
│   │   │   │   │   ├── Device.kt           # Data class: single device
│   │   │   │   │   └── Model.kt            # Data class: wrapper with device list
│   │   │   │   ├── service/
│   │   │   │   │   └── DataSource.kt       # Singleton: API fetching & JSON parsing
│   │   │   │   ├── view/
│   │   │   │   │   ├── MainActivity.kt     # Entry point, hosts RecyclerView
│   │   │   │   │   ├── DetailsActivity.kt  # Device detail/edit screen
│   │   │   │   │   └── DeviceRecyclerAdaptor.kt  # RecyclerView adapter
│   │   │   │   └── viewmodel/
│   │   │   │       └── FirstFragmentViewModel.kt  # LiveData ViewModel (unused)
│   │   │   ├── res/
│   │   │   │   ├── layout/                 # XML layouts
│   │   │   │   ├── drawable*/              # Image assets (multi-DPI)
│   │   │   │   ├── values/                 # Colors, strings, themes, dimensions
│   │   │   │   └── navigation/             # Navigation graphs (defined, unused)
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                           # Unit tests (JUnit 4)
│   │   └── androidTest/                    # Instrumented tests (Espresso)
│   ├── build.gradle                        # App-level dependencies and config
│   └── proguard-rules.pro
├── gradle/                                 # Gradle wrapper files
├── build.gradle                            # Root build config
├── settings.gradle                         # Module includes and repo config
├── gradle.properties                       # JVM args, AndroidX flags
├── gradlew / gradlew.bat                   # Gradle wrapper scripts
└── README.md
```

---

## Architecture

The app follows a **partial MVVM** pattern:

```
View (Activity/Adapter) ──► ViewModel (LiveData) ──► Model (data classes)
                                                   ──► Service (DataSource)
```

**Important caveats:**
- `FirstFragmentViewModel` is defined but **not connected to any Activity** — it generates mock data only.
- `MainActivity` calls `DataSource` directly inside a coroutine, bypassing ViewModel.
- Navigation component graphs are defined but **not used** — screens navigate via explicit `Intent`.

---

## Key Files and Their Roles

### `DataSource.kt`
- **Singleton** via Kotlin `companion object`
- Fetches device JSON from `https://veramobile.mios.com/test_android/items.test`
- Parses JSON manually using `org.json`
- Sorts results by `PK_Device`
- Returns `ArrayList<Device>`

### `MainActivity.kt`
- Entry point / launcher activity
- Initializes `RecyclerView` with `LinearLayoutManager`
- Launches a `GlobalScope.launch(Dispatchers.Main)` coroutine to fetch data
- Exposes the adapter via a **static companion method** `getRecyclerViewAdapter()` — this is an anti-pattern; avoid extending it

### `DetailsActivity.kt`
- Launched via `Intent` with extras: `firmware`, `macAddress`, `platform`, `image`, `pkDevice`, `isEditable`
- Shows read-only device fields; platform is editable when `isEditable = true`
- Loads device image asynchronously using `GlobalScope`
- Updates adapter data on back navigation via `TextWatcher`

### `DeviceRecyclerAdaptor.kt`
- Binds `Device` objects to `list_row.xml` views
- Long-click: deletes device from list
- Edit icon click: opens `DetailsActivity` with `isEditable = true`
- Selects image URL based on device platform type
- Uses Glide with placeholder/error drawables

### `Device.kt` / `Model.kt`
- Pure data classes — no logic
- `Device` fields: `Firmware`, `InternalIP`, `LastAliveReported`, `MacAddress`, `PK_Account`, `PK_Device`, `PK_DeviceSubType`, `PK_DeviceType`, `Platform`, `Server_Account`, `Server_Device`, `Server_Event`

---

## Build & Development

### Prerequisites
- Android Studio (Dolphin or later recommended)
- JDK 8+
- Android SDK with API level 31

### Building

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Clean build
./gradlew clean assembleDebug
```

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires connected device or emulator)
./gradlew connectedAndroidTest
```

### Installing on Device

```bash
./gradlew installDebug
```

---

## Code Conventions

### Naming
| Element | Convention | Example |
|---|---|---|
| Packages | reverse domain, lowercase | `com.erenavci.testapplication.view` |
| Classes | PascalCase | `DeviceRecyclerAdaptor` |
| Functions | camelCase | `retrieveDeviceList()` |
| Variables | camelCase | `deviceList` |
| Constants | UPPER_SNAKE_CASE | `DEVICE_LIST_URL` |
| Resources | snake_case | `activity_main.xml`, `text_details_firmware` |

### View Binding
- **`viewBinding` is enabled** in `build.gradle` — use generated binding classes when adding new activities or fragments.
- Existing code uses deprecated `kotlinx.android.synthetic` — do **not** add new synthetic imports; migrate to view binding instead.

### Async / Coroutines
- Current code uses `GlobalScope.launch` — this is acceptable for legacy code but **avoid in new code**.
- Prefer `lifecycleScope.launch` (in Activity/Fragment) or `viewModelScope.launch` (in ViewModel) for new async work.
- Keep network calls off the main thread using `Dispatchers.IO`.

### Image Loading
- Always use **Glide** for image loading.
- Use `RequestOptions` for placeholders and error drawables.
- Do not call `BitmapFactory.decodeStream` on the main thread.

---

## Known Issues / Technical Debt

These are existing patterns to be aware of — fix when touching related code, but don't refactor proactively:

1. **Static adapter access** — `MainActivity.getRecyclerViewAdapter()` couples the adapter to a static reference, making testing and lifecycle management difficult.
2. **`GlobalScope` usage** — coroutines should be scoped to lifecycle to avoid leaks.
3. **Deprecated `kotlinx.android.synthetic`** — should migrate to View Binding.
4. **Synchronous HTTP in coroutine** — `DataSource.retrieveDeviceList()` blocks; wrap with `withContext(Dispatchers.IO)`.
5. **Unused dependencies** — Retrofit, RxJava, and Room are declared but not used; remove or implement.
6. **Unused ViewModel** — `FirstFragmentViewModel` is not wired to any View; either connect it or remove it.
7. **No error handling** — API failures and network errors are not surfaced to the user.
8. **No local persistence** — device edits are lost on app restart despite Room being included.

---

## API

**Endpoint:** `https://veramobile.mios.com/test_android/items.test`
**Method:** HTTP GET
**Auth:** None
**Response:**
```json
{
  "Devices": [
    {
      "Firmware": "1.7.4908",
      "InternalIP": "192.168.1.1",
      "LastAliveReported": "2021-01-01T00:00:00",
      "MacAddress": "AA:BB:CC:DD:EE:FF",
      "PK_Account": 12345,
      "PK_Device": 67890,
      "PK_DeviceSubType": 2,
      "PK_DeviceType": 1,
      "Platform": "MiOS",
      "Server_Account": "...",
      "Server_Device": "...",
      "Server_Event": "..."
    }
  ]
}
```

---

## Manifest & Permissions

- **Package:** `com.erenavci.testapplication`
- **Required permission:** `INTERNET`
- **Activities:**
  - `MainActivity` — launcher, main task
  - `DetailsActivity` — device details / editing
- **Theme:** `Theme.TestApplication` (Material Components)

---

## What Does NOT Exist (Yet)

- CI/CD pipelines (no GitHub Actions, no CircleCI)
- Docker / containerization
- ProGuard / R8 obfuscation (disabled)
- Dependency injection (Hilt/Dagger)
- Local database usage (Room schema not implemented)
- Navigation component usage (graph defined, not wired)
- Meaningful test coverage beyond example placeholders

---

## Git Workflow

- Main branch: `master` / `main`
- Feature branches follow the pattern: `claude/<description>-<id>`
- Commit messages should be descriptive (e.g., `"Add Room entity for Device persistence"`)
- Push with: `git push -u origin <branch-name>`

---

## AI Assistant Guidelines

When making changes to this codebase:

1. **Read before editing** — always read the relevant file before modifying it.
2. **Minimal changes** — only change what is necessary for the task; do not refactor surrounding code.
3. **No new anti-patterns** — do not introduce `GlobalScope`, static references, or synthetic imports.
4. **Test awareness** — if modifying business logic, note that test coverage is minimal; do not break the example tests.
5. **View Binding** — use generated binding classes for any new UI code.
6. **Coroutine scope** — use `lifecycleScope` or `viewModelScope` for new coroutines.
7. **Kotlin idioms** — use data classes, extension functions, and `when` expressions appropriately.
8. **Resource naming** — follow snake_case for all resource IDs and file names.
