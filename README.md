# Pokemon Master Set Tracker

A comprehensive Android application for tracking Pokémon Trading Card Game collections. Users can search for cards across English and Japanese sets, mark cards as owned or missing, track card conditions and grading, view prices, and manage their favorite Pokémon.

## Features

- **Authentication**: Simple local authentication with email-based login/registration
- **Card Search**: Search for Pokémon cards across the PokemonTCG.io API
  - Filter by language (English/Japanese)
  - View detailed card information
- **Collection Management**: 
  - Mark cards as owned or missing
  - Track card condition (Mint, Near Mint, Lightly Played, etc.)
  - Support for graded cards with grading details
  - Store purchase prices and current card values
- **Favorites**: Save favorite Pokémon to quickly access their master sets
- **Completion Tracking**: View collection completion percentage for each Pokémon
- **Image Caching**: Efficient image loading and caching using Coil library
- **Local Database**: Room database for offline access to cached card data

## Tech Stack

### Architecture
- **MVVM** with Clean Architecture principles
- **Jetpack Compose** for UI
- **Kotlin Coroutines** for async operations

### Libraries & Dependencies
- **Retrofit** 2.9.0 - REST API client
- **Room** 2.6.1 - Local database
- **Hilt** 2.48 - Dependency injection
- **Coil** 2.5.0 - Image loading and caching
- **Jetpack Compose** 1.5.4 - UI framework
- **Firebase Auth** - Authentication (optional, can be replaced with local auth)
- **Gson** 2.10.1 - JSON serialization

## Project Structure

```
app/src/main/
├── kotlin/com/example/pokemonmastersettracker/
│   ├── data/
│   │   ├── api/              # API endpoints (Retrofit)
│   │   ├── database/         # Room DAOs and Database
│   │   ├── models/           # Data classes
│   │   └── repository/       # Repository pattern implementation
│   ├── di/                   # Dependency Injection (Hilt modules)
│   ├── ui/
│   │   ├── screens/          # Compose screens
│   │   ├── components/       # Reusable UI components
│   │   └── theme/            # Color and styling
│   ├── viewmodel/            # ViewModels with UI state
│   ├── utils/                # Utility classes and type converters
│   ├── MainActivity.kt       # Main activity
│   └── PokemonTrackerApp.kt  # Application class
├── res/
│   ├── values/
│   │   ├── strings.xml       # String resources
│   │   ├── colors.xml        # Color definitions
│   │   └── styles.xml        # Style definitions
│   └── AndroidManifest.xml   # App manifest
└── build.gradle.kts          # Build configuration
```

## Getting Started

### Prerequisites
- Android Studio 2022.1 or later
- Android SDK 24 (API Level 24) or higher
- Kotlin 1.9.0

### Installation

1. Clone the repository
2. Open the project in Android Studio
3. Let Gradle sync the dependencies
4. Run the app on an emulator or device

### API Key
The app uses the free public PokemonTCG.io API which doesn't require authentication.

## Usage

### Home Screen
1. Search for a Pokémon name
2. Select English or Japanese cards
3. Tap Search to load cards
4. Tap a card to view details

### Collection Management
1. From the card details, add cards to your collection
2. Mark cards as owned and select their condition
3. Add grading information if applicable
4. View your overall collection completion percentage

### Favorites
1. Add favorite Pokémon to quickly access them
2. View all favorites in the Favorites tab
3. Navigate to cards for your favorite Pokémon

## Data Models

### Card
- ID, Name, Type, Rarity
- Card image (small and large)
- HP, Number, Artist
- TCG Player pricing data

### UserCard
- User association
- Card association
- Owned status
- Condition (enum)
- Grading information (company, grade)
- Purchase and current prices

### User
- Email, Username
- Creation timestamp

### FavoritePokemon
- User association
- Pokémon name
- Added timestamp

## Room Database Schema

The app uses Room for local caching with the following entities:
- `cards` - Cached card data
- `user_cards` - User's collection entries
- `users` - User accounts
- `favorite_pokemon` - Favorite Pokémon list

All entities include proper foreign key relationships and cascade delete.

## API Integration

### PokemonTCG.io API Endpoints Used
- `GET /cards` - Search and list cards
- `GET /cards/{id}` - Get card details
- `GET /sets` - List all sets

### Query Examples
```
// Find all Pikachu cards
GET /cards?q=name:Pikachu

// Find English Pikachu cards
GET /cards?q=name:Pikachu language:en

// Find Japanese Pikachu cards
GET /cards?q=name:Pikachu language:ja
```

## State Management

Each screen has a corresponding ViewModel that manages UI state:

- **AuthViewModel** - Authentication state
- **CardViewModel** - Card search and display
- **UserCollectionViewModel** - User's collection data
- **FavoritesViewModel** - Favorite Pokémon management

States are exposed as `StateFlow` for reactive updates.

## Image Loading

The app uses **Coil** for efficient image loading:
- Automatic caching
- URL-based loading (images stored as URLs in database)
- Placeholder support
- Memory and disk caching

## Offline Support

- Room database caches card data locally
- Users can view previously searched cards offline
- New searches require network connectivity

## Future Enhancements

- [ ] Firebase authentication with proper user accounts
- [ ] Card price tracking history
- [ ] Export collection as PDF
- [ ] Community trading features
- [ ] Advanced filtering and sorting
- [ ] Card condition documentation with photos
- [ ] Integration with TCGPlayer API for live pricing
- [ ] Barcode scanning for quick card addition

## Known Limitations

- Currently uses local authentication (no backend)
- Card images stored as URLs (not downloaded locally)
- No real-time price updates from TCGPlayer

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- [PokemonTCG.io](https://pokemontcg.io) for the comprehensive API
- [Jetpack Compose](https://developer.android.com/jetpack/compose) for the modern UI toolkit
- [Coil](https://coil-kt.github.io/coil/) for image loading
=======
# TCG-MasterSet-Tracker
Track your Pokemon TCG card collection with ease
>>>>>>> f45f57942c5162cddf34481037103dc4c6b896b7
