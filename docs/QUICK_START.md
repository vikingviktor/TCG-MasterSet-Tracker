# Pokemon Master Set Tracker - Quick Start Guide

## Project Overview

Pokemon Master Set Tracker is a modern Android application built with Kotlin and Jetpack Compose that allows users to track their Pokemon Trading Card Game collections. The app uses the free PokemonTCG.io API to fetch card data and Room database for local caching.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                  Jetpack Compose UI                      │
│         (Screens, Components, Theme Management)          │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│                    ViewModels                             │
│    (State Management, Business Logic Orchestration)      │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│                  Repository Pattern                       │
│          (Data Source Abstraction Layer)                 │
└─────┬──────────────────────────────────┬────────────────┘
      │                                  │
┌─────▼────────────────┐    ┌───────────▼──────────────┐
│   PokemonTCG.io API  │    │  Room Local Database     │
│   (Retrofit Client)  │    │  (Card, User, Favorite) │
└─────────────────────┘    └──────────────────────────┘
```

## Project Structure

### Core Packages

**`data/`** - Data layer
- `api/` - Retrofit API definitions
- `models/` - Data classes (Cards, Users, etc.)
- `database/` - Room DAOs and database
- `repository/` - Repository pattern implementation

**`di/`** - Dependency Injection
- `AppModule.kt` - Hilt modules for dependency injection

**`ui/`** - Presentation layer
- `screens/` - Compose screens
- `components/` - Reusable UI components
- `theme/` - Color, typography, styles

**`viewmodel/`** - State management
- `AuthViewModel.kt` - Authentication state
- `CardViewModel.kt` - Card search and display
- `UserCollectionViewModel.kt` - Collection management
- `FavoritesViewModel.kt` - Favorites management

**`utils/`** - Utilities
- `TypeConverters.kt` - Room type converters for complex objects
- `Utilities.kt` - Helper functions, formatters
- `MockData.kt` - Mock data for testing

## Key Features Implementation

### 1. Authentication Flow
```
User Input (Email/Password)
         ↓
   AuthViewModel
         ↓
  PokemonRepository
         ↓
      UserDao
         ↓
   Room Database
```

### 2. Card Search & Display
```
Search Query
         ↓
   CardViewModel
         ↓
  PokemonRepository
         ↓
   PokemonTCG API
         ↓
   CardDao (Cache)
         ↓
   Compose Screen (with Coil Images)
```

### 3. Collection Management
```
User Marks Card as Owned
         ↓
UserCollectionViewModel
         ↓
PokemonRepository
         ↓
UserCardDao
         ↓
Room Database + UI Update
```

## Database Schema

### Users Table
```
CREATE TABLE users (
    id TEXT PRIMARY KEY,
    email TEXT NOT NULL,
    username TEXT NOT NULL,
    createdAt LONG NOT NULL
)
```

### Cards Table
```
CREATE TABLE cards (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    supertype TEXT NOT NULL,
    subtypes TEXT,
    hp TEXT,
    types TEXT,
    rarity TEXT,
    set TEXT,
    image TEXT,
    number TEXT,
    artist TEXT,
    tcgplayer TEXT
)
```

### UserCards Table
```
CREATE TABLE user_cards (
    id LONG PRIMARY KEY AUTO_INCREMENT,
    userId TEXT NOT NULL,
    cardId TEXT NOT NULL,
    isOwned BOOLEAN NOT NULL,
    condition TEXT NOT NULL,
    isGraded BOOLEAN NOT NULL,
    gradingCompany TEXT,
    grade TEXT,
    purchasePrice REAL,
    currentPrice REAL,
    addedAt LONG NOT NULL,
    FOREIGN KEY(userId) REFERENCES users(id),
    FOREIGN KEY(cardId) REFERENCES cards(id)
)
```

### FavoritePokemon Table
```
CREATE TABLE favorite_pokemon (
    id LONG PRIMARY KEY AUTO_INCREMENT,
    userId TEXT NOT NULL,
    pokemonName TEXT NOT NULL,
    addedAt LONG NOT NULL,
    FOREIGN KEY(userId) REFERENCES users(id)
)
```

## API Integration Details

### Base URL
```
https://api.pokemontcg.io/v2/
```

### Available Endpoints

**Search Cards:**
```
GET /cards?q=name:Pikachu language:en
GET /cards?q=name:Charizard language:ja
```

**Get Card Details:**
```
GET /cards/sv04pt-1
```

**List Sets:**
```
GET /sets?pageSize=250
```

### Example Queries

```kotlin
// Search all Pikachu cards
viewModel.searchPokemonCards("Pikachu", "en")

// Get specific set cards
viewModel.getCardsByPokemonAndSet("Pikachu", "sv04pt")
```

## UI State Management

All screens use `StateFlow` for reactive state:

```kotlin
// Example: CardViewModel
data class CardUiState(
    val cards: List<Card> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

// Collect in Compose
val state by viewModel.cardUiState.collectAsState()
```

## Image Caching with Coil

Images are loaded from URLs stored in the database:

```kotlin
AsyncImage(
    model = card.image?.large,
    contentDescription = card.name,
    contentScale = ContentScale.Crop
)
```

**Coil handles automatically:**
- Memory caching
- Disk caching
- Placeholder management
- Failed image handling

## Dependency Injection with Hilt

### Setup
1. Application class annotated with `@HiltAndroidApp`
2. Activities annotated with `@AndroidEntryPoint`
3. ViewModels annotated with `@HiltViewModel`

### Example Provider
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun providePokemonRepository(...): PokemonRepository {
        return PokemonRepository(...)
    }
}
```

## Running the App

### Prerequisites
- Android Studio Flamingo or later
- Android SDK 24+ (API Level 24)
- Kotlin 1.9.0+

### Steps
1. Clone/open the project
2. Sync Gradle files
3. Connect Android device or start emulator
4. Run the app (Shift + F10)

## Important Notes

### No API Key Required
PokemonTCG.io API is free and public - no authentication needed.

### Offline Support
- Cards are cached in Room database
- Users can view previously searched cards without internet
- New searches require connectivity

### Image URLs
- Card images are stored as URLs in the database
- Full images are downloaded on-demand using Coil
- Coil handles all caching automatically

## Common Development Tasks

### Adding a New Screen
1. Create screen composable in `ui/screens/`
2. Create corresponding ViewModel in `viewmodel/`
3. Add navigation in MainActivity
4. Connect ViewModel in screen using `hiltViewModel()`

### Adding a Database Entity
1. Create data class with `@Entity` annotation in `data/models/`
2. Create DAO in `data/database/Daos.kt`
3. Add DAO to `PokemonTrackerDatabase`
4. Update migrations if needed

### Adding API Endpoints
1. Add suspend function to `PokemonTCGApi`
2. Call from `PokemonRepository`
3. Cache results in Room if needed
4. Expose through repository to ViewModels

## Testing

### Unit Tests
```kotlin
// Test ViewModel state
@Test
fun testCardSearch() {
    // Arrange
    // Act
    viewModel.searchPokemonCards("Pikachu")
    
    // Assert
    assertEquals(expected, viewModel.cardUiState.value)
}
```

### UI Tests
Use Compose testing library:
```kotlin
@Test
fun testCardItem() {
    composeTestRule.setContent {
        CardItem(card = mockCard, onCardClick = {})
    }
    // Assert UI elements
}
```

## Troubleshooting

### API Request Failures
- Check internet connection
- Verify PokemonTCG.io API is accessible
- Check query syntax in API calls
- Review HTTP logs (enabled in AppModule)

### Database Issues
- Clear app data and reinstall
- Check for migration issues
- Verify DAO queries syntax

### UI State Not Updating
- Ensure ViewModels are collected in Compose
- Check for coroutine cancellation
- Verify StateFlow emissions

## Performance Optimization

### Image Loading
- Coil caches images automatically
- Use appropriate image sizes (thumbnails vs full)
- Consider lazy loading in lists

### Database
- Use proper indexes on frequently queried columns
- Implement pagination for large datasets
- Cache frequently accessed data

### Network
- Implement offline-first strategy
- Cache API responses in Room
- Use pagination for large result sets

## Next Steps

1. **Setup Firebase Authentication** - Replace local auth
2. **Implement Card Details Screen** - Show full card info
3. **Add Price Tracking** - Historical price data
4. **Export Collection** - PDF or CSV export
5. **Card Scanning** - Barcode integration
6. **Advanced Filtering** - By type, rarity, set, etc.

## Resources

- [PokemonTCG.io API Docs](https://docs.pokemontcg.io/)
- [Android Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/topic/libraries/architecture/room)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)
- [Coil Image Loading](https://coil-kt.github.io/coil/)
