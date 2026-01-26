# Work Time Tracker - AI Development Guide

## Project Overview

Android work time tracking app using Kotlin, Jetpack Compose, Hilt DI, Room, Retrofit, and DataStore. Features include authentication with token refresh, profile management, offline caching, and network connectivity monitoring.

## Architecture & Structure

### Feature-Based Organization

Code is organized by feature modules under `features/`:

- `auth/` - Login/register with JWT token management
- `profile/` - User profile viewing and editing
- `home/` - Main screen and theme management

Each feature follows a layered structure:

```
features/<feature>/
  ├── data/
  │   ├── api/         # Retrofit API interfaces
  │   ├── model/       # DTOs and domain models
  │   └── repository/  # Data layer orchestration
  ├── di/              # Feature-specific Hilt modules
  └── presentation/
      ├── <screen>/    # Screen-specific ViewModels and States
      └── <Screen>.kt  # Composable UI screens
```

### Core Infrastructure (`core/`)

- `di/` - App-wide DI modules (NetworkModule, DatabaseModule, DataStoreModule)
- `database/` - Room database and DAOs for offline caching
- `network/` - OkHttp interceptors (Auth, TokenRefresh, ContentType)
- `utils/` - NetworkConnectivityObserver and Constants

### Navigation Pattern

- Type-safe navigation using sealed `Screen` class with route strings
- Two-level navigation: top-level auth flow, bottom navigation within app
- `MainScaffold` hosts bottom nav with Home/Test/Profile tabs
- Navigation state managed via `NavController` passed through composables

## Dependency Injection (Hilt)

### Module Locations

- **App-wide**: `core/di/` (NetworkModule, DatabaseModule, DataStoreModule)
- **Feature-specific**: `features/<feature>/di/` (AuthModule, ProfileModule)
- Application class: `app/App.kt` annotated with `@HiltAndroidApp`

### Key Patterns

- `@Singleton` for repositories and network clients
- `@Named` qualifiers for domain variants (local/remote) and image URLs
- ViewModels use `@HiltViewModel` with constructor injection
- Activities/screens use `@AndroidEntryPoint` annotation

## Network & Authentication

### API Configuration

Base URLs configured in NetworkModule:

- Local: `http://192.168.0.253:8000`
- Remote: `https://worktimetrack.dev`
- Switch via `@Named("active_domain")` binding in NetworkModule

### Interceptor Chain (order matters)

1. `AuthInterceptor` - Adds Bearer token to requests
2. `TokenRefreshInterceptor` - Auto-refreshes tokens on 401, uses mutex to prevent concurrent refreshes
3. `ContentTypeInterceptor` - Ensures proper Content-Type headers
4. `HttpLoggingInterceptor` - Logs requests/responses in debug

### Token Management

- Tokens stored in DataStore (`auth_token` key)
- Access via `AuthRepository.authToken` Flow or `getToken()` suspend function
- Refresh logic in `TokenRefreshInterceptor` with separate OkHttp client to avoid recursion
- Auth state drives MainActivity's initial routing (null → loading, true → main, false → login)

## Data Persistence

### DataStore Usage

Two separate DataStore instances:

- Auth tokens: Injected via Hilt in `core/di/DataStoreModule`
- User preferences (theme): Created in `UserPreferencesRepository` via `preferencesDataStore` delegate

### Room Database

- `AppDatabase` with `CachedUserEntity` for offline user profile caching
- DAOs injected via `DatabaseModule`
- Used for offline-first architecture (check cache, fallback to network)

## State Management

### ViewModel Pattern

- State classes are data classes (e.g., `ProfileState`, `LoginState`)
- Expose UI state as `StateFlow<State>`
- Use sealed `ApiResponse<T>` for network results (Success/Error/Loading)
- Example: `ProfileViewModel` manages profile data, edit mode, logout state

### Compose State

- ViewModels obtained via `hiltViewModel()` in composables
- `collectAsState()` for Flow → Compose State conversion
- Theme preference handled by `ThemeViewModel` observing DataStore

## UI Conventions

### Compose Best Practices

- Screens are top-level `@Composable` functions
- Navigation callbacks passed as lambda parameters (e.g., `onNavigateToLogin`)
- Material3 design system with dynamic color scheme
- Extended Material Icons used throughout (`androidx.compose.material.icons.extended`)

### Network Status UI

- `ConnectivityViewModel` observes network state via `NetworkConnectivityObserver`
- Offline banner shown in `MainScaffold` when disconnected
- Implement offline-first patterns by checking cache before network calls

## Build & Development

### Gradle Configuration

- Version catalog in `gradle/libs.versions.toml` for centralized dependency management
- Compose Compiler plugin enabled via `kotlin-compose` plugin
- KSP used for Hilt and Room annotation processing
- Min SDK 31, Target SDK 36

### Development Environment

- **All builds run through Android Studio UI** - DO NOT generate Gradle commands
- No Java installed locally - all Gradle operations via Android Studio
- Testing done manually on physical device - DO NOT write automated tests
- DO NOT create documentation files (.md, README, docs/)

### Code Generation

- Hilt generates DI code in `build/generated/hilt/`
- Room generates DAOs in `build/generated/ksp/`
- Rebuild project if DI/Room issues occur: Build → Rebuild Project in Android Studio

## AI Agent Restrictions

**NEVER generate these:**

- Gradle commands: `./gradlew`, `gradle build`, `assemble`, `test`, `lint`, `installDebug`
- Gradle sync commands or build scripts
- Test files (unit tests, instrumented tests)
- Documentation files (.md files, README.md, docs/ folder)
- Build or CI/CD configurations

**Instead:**

- Code only - features, bug fixes, refactoring
- All builds/tests handled by developer through Android Studio UI

## Common Patterns

### API Repository Pattern

```kotlin
suspend fun getData(): ApiResponse<Data> = apiCall {
    // Try cache first
    val cached = dao.get()
    if (cached != null) return@apiCall ApiResponse.Success(cached, fromCache = true)

    // Fetch from network
    val response = api.getData()
    dao.insert(response)
    response
}
```

### Navigation with State Clearing

```kotlin
navController.navigate(Screen.Main.route) {
    popUpTo(Screen.Login.route) { inclusive = true }
}
```

### DataStore Operations

```kotlin
// Read
val flow = dataStore.data.map { it[KEY] }

// Write
dataStore.edit { prefs -> prefs[KEY] = value }
```

## Known Issues

- Network security config commented out in AndroidManifest (cleartextTraffic enabled)
- Some screens (Chat, ChatDetail) have empty implementations
- Test screen exists but purpose unclear
