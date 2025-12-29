# Project File Structure

## Complete Directory Tree

```
TCG-MasterSet-Tracker/
├── .gitignore
├── README.md
├── QUICK_START.md
├── SETUP_CHECKLIST.md
├── API_DOCUMENTATION.md
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
│
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   │
│   └── src/main/
│       ├── AndroidManifest.xml
│       │
│       ├── kotlin/com/example/pokemonmastersettracker/
│       │   ├── MainActivity.kt
│       │   ├── PokemonTrackerApp.kt
│       │   │
│       │   ├── data/
│       │   │   ├── api/
│       │   │   │   └── PokemonTCGApi.kt
│       │   │   │
│       │   │   ├── database/
│       │   │   │   ├── PokemonTrackerDatabase.kt
│       │   │   │   └── Daos.kt
│       │   │   │
│       │   │   ├── models/
│       │   │   │   ├── CardModels.kt
│       │   │   │   └── UserModels.kt
│       │   │   │
│       │   │   └── repository/
│       │   │       └── PokemonRepository.kt
│       │   │
│       │   ├── di/
│       │   │   └── AppModule.kt
│       │   │
│       │   ├── ui/
│       │   │   ├── screens/
│       │   │   │   ├── HomeScreen.kt
│       │   │   │   ├── AuthScreens.kt
│       │   │   │   ├── CollectionScreen.kt
│       │   │   │   └── FavoritesScreen.kt
│       │   │   │
│       │   │   ├── components/
│       │   │   │   └── CardComponents.kt
│       │   │   │
│       │   │   └── theme/
│       │   │       └── Color.kt
│       │   │
│       │   ├── viewmodel/
│       │   │   ├── AuthViewModel.kt
│       │   │   ├── CardViewModel.kt
│       │   │   ├── UserCollectionViewModel.kt
│       │   │   └── FavoritesViewModel.kt
│       │   │
│       │   └── utils/
│       │       ├── TypeConverters.kt
│       │       ├── Utilities.kt
│       │       └── MockData.kt
│       │
│       └── res/
│           ├── values/
│           │   ├── strings.xml
│           │   ├── colors.xml
│           │   └── styles.xml
│           │
│           └── mipmap-hdpi/
│               └── (launcher icons - to be added)
```

## File Counts

- **Configuration Files:** 5
- **Kotlin Source Files:** 23
- **XML Resource Files:** 4
- **Documentation Files:** 4
- **Build Scripts:** 3
- **Total Files:** 39

## Core Modules Breakdown

### Data Layer (6 files)
1. `api/PokemonTCGApi.kt` - REST API interface
2. `models/CardModels.kt` - Card data classes
3. `models/UserModels.kt` - User data classes
4. `database/PokemonTrackerDatabase.kt` - Room database
5. `database/Daos.kt` - Data access objects
6. `repository/PokemonRepository.kt` - Repository pattern

### Presentation Layer (11 files)
1. `MainActivity.kt` - Entry point
2. `PokemonTrackerApp.kt` - Application class
3. `ui/screens/HomeScreen.kt` - Card search
4. `ui/screens/AuthScreens.kt` - Login/Register
5. `ui/screens/CollectionScreen.kt` - Collection view
6. `ui/screens/FavoritesScreen.kt` - Favorites view
7. `ui/components/CardComponents.kt` - UI components
8. `ui/theme/Color.kt` - Color definitions
9. `viewmodel/AuthViewModel.kt` - Auth state
10. `viewmodel/CardViewModel.kt` - Card state
11. `viewmodel/UserCollectionViewModel.kt` - Collection state
12. `viewmodel/FavoritesViewModel.kt` - Favorites state

### Utilities & DI (4 files)
1. `di/AppModule.kt` - Dependency injection
2. `utils/TypeConverters.kt` - Room type converters
3. `utils/Utilities.kt` - Helper functions
4. `utils/MockData.kt` - Test data

### Resources (4 files)
1. `AndroidManifest.xml` - App manifest
2. `values/strings.xml` - String resources
3. `values/colors.xml` - Color definitions
4. `values/styles.xml` - Style definitions

### Configuration (5 files)
1. `build.gradle.kts` - Root build
2. `app/build.gradle.kts` - App build
3. `settings.gradle.kts` - Settings
4. `gradle.properties` - Gradle properties
5. `proguard-rules.pro` - Obfuscation rules

### Documentation (4 files)
1. `README.md` - Project overview
2. `QUICK_START.md` - Architecture guide
3. `SETUP_CHECKLIST.md` - Setup progress
4. `API_DOCUMENTATION.md` - API reference

## Package Organization

```
com.example.pokemonmastersettracker/
├── data/
│   ├── api/              (REST client)
│   ├── database/         (Room entities & DAOs)
│   ├── models/           (Data classes)
│   └── repository/       (Data abstraction)
├── di/                   (Hilt injection)
├── ui/
│   ├── components/       (Reusable UI)
│   ├── screens/          (Full screens)
│   └── theme/            (Design system)
├── viewmodel/            (MVVM state)
└── utils/                (Helpers & utilities)
```

## Key Features Per File

### MainActivity.kt
- Activity setup with Compose
- Navigation bar implementation
- Screen routing logic

### PokemonTCGApi.kt
- Retrofit endpoints
- Card search, get, set listing
- Query parameter definitions

### PokemonRepository.kt
- API calls with error handling
- Room database caching
- Business logic orchestration

### HomeScreen.kt
- Card search UI
- Language selection
- Results display grid

### AuthScreens.kt
- Login screen
- Register screen
- Form validation

### CardViewModel.kt
- Card search state
- Loading/error handling
- Card selection

### PokemonTrackerDatabase.kt
- Room database setup
- DAO references
- Singleton pattern

### AppModule.kt
- Hilt dependency setup
- Retrofit configuration
- Database provider
- Repository injection

## Dependencies Summary

### Networking
- Retrofit 2.9.0
- OkHttp 4.11.0 with logging

### Database
- Room 2.6.1
- Kotlin coroutines integration

### UI Framework
- Jetpack Compose 1.5.4
- Material 3 components
- Compose Material Icons

### State Management
- Kotlin Coroutines 1.7.3
- StateFlow for reactive UI

### Dependency Injection
- Hilt 2.48
- Hilt Navigation Compose 1.1.0

### Image Loading
- Coil 2.5.0 with Compose integration

### Other
- Gson 2.10.1
- Firebase Auth (optional)
- Android Lifecycle components

## File Size Estimates

- **Source Code:** ~8-10 KB
- **XML Resources:** ~3 KB
- **Configuration:** ~5 KB
- **Total:** ~20 KB (uncompressed)

## What Each File Does

### Data Models (`models/`)
- Define data structures matching API responses
- Room entity annotations
- Type serialization annotations

### API Client (`api/`)
- Retrofit interface with all endpoints
- Query parameter definitions
- Request/response mapping

### Database (`database/`)
- Room database configuration
- Data Access Objects (DAOs)
- Entity relationships
- Migrations setup

### Repository (`repository/`)
- Abstracts data sources (API + DB)
- Implements caching strategy
- Error handling
- Business logic

### ViewModels (`viewmodel/`)
- Manage UI state with StateFlow
- Handle user interactions
- Orchestrate repository calls
- Lifecycle aware

### Screens (`ui/screens/`)
- Composable functions for full screens
- Layout and navigation
- User input handling
- State collection

### Components (`ui/components/`)
- Reusable UI elements
- Card display components
- Detail views
- Dialogs

### Theme (`ui/theme/`)
- Color palette
- Typography (if added)
- Design system constants

### DI Module (`di/`)
- Hilt configuration
- Provider functions
- Object creation and injection
- Singleton setup

### Utilities (`utils/`)
- Helper functions
- Type converters for Room
- Formatting functions
- Test/mock data

## Next File Additions

As you develop further, you'll likely add:

```
├── ui/
│   ├── dialogs/
│   │   ├── CardConditionDialog.kt
│   │   ├── GradingDialog.kt
│   │   └── FilterDialog.kt
│   ├── navigation/
│   │   └── Navigation.kt
│   └── modifier/
│       └── Extensions.kt
├── viewmodel/
│   └── SearchViewModel.kt
├── worker/
│   └── SyncWorker.kt
├── broadcast/
│   └── NetworkReceiver.kt
└── test/
    ├── viewmodel/
    ├── repository/
    └── ui/
```

## Dependencies File Paths

- **Gradle:** `build.gradle.kts` (root) and `app/build.gradle.kts`
- **Manifest:** `app/src/main/AndroidManifest.xml`
- **Resources:** `app/src/main/res/`
- **Source:** `app/src/main/kotlin/`

## How to Find Things

**Looking for...** | **Find in...**
---|---
API endpoints | `data/api/PokemonTCGApi.kt`
Card data structures | `data/models/CardModels.kt`
User authentication logic | `viewmodel/AuthViewModel.kt`
Database schema | `data/database/PokemonTrackerDatabase.kt`
UI components | `ui/components/CardComponents.kt`
Screens | `ui/screens/*.kt`
DI setup | `di/AppModule.kt`
Colors/Theme | `ui/theme/Color.kt`
Utilities | `utils/Utilities.kt`
App entry point | `MainActivity.kt`
