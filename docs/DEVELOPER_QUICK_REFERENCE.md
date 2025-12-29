# Developer Quick Reference

## 🚀 Quick Commands

### Build & Run
```bash
# Sync Gradle
Ctrl + Shift + A → "Sync Now"

# Build project
Ctrl + F9

# Run app
Shift + F10

# Clean build
Build → Clean Project
```

### Code Navigation
```bash
Ctrl + N          # Find class
Ctrl + Shift + F  # Find in files
Ctrl + B          # Go to definition
Ctrl + Alt + B    # Find implementations
```

### Editor Shortcuts
```bash
Ctrl + /          # Toggle comment
Ctrl + Alt + L    # Format code
Alt + Enter       # Quick fix/action
Ctrl + Space      # Code completion
```

## 📍 File Locations Quick Guide

### Search for...
```
Authentication    → viewmodel/AuthViewModel.kt
Card search       → viewmodel/CardViewModel.kt
Collection        → viewmodel/UserCollectionViewModel.kt
API endpoints     → data/api/PokemonTCGApi.kt
Database          → data/database/PokemonTrackerDatabase.kt
Home screen       → ui/screens/HomeScreen.kt
Colors            → ui/theme/Color.kt
DI setup          → di/AppModule.kt
```

## 🔧 Common Tasks

### Add a New Screen
```kotlin
// 1. Create file: ui/screens/NewScreen.kt
@Composable
fun NewScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("New Screen")
    }
}

// 2. Add to MainActivity navigation
// 3. Add NavBar item if needed
```

### Add API Endpoint
```kotlin
// 1. Add to PokemonTCGApi.kt
@GET("endpoint")
suspend fun getEndpoint(): ResponseType

// 2. Call from repository
// 3. Cache to database if needed
```

### Add Database Entity
```kotlin
// 1. Create model in data/models/
@Entity(tableName = "table_name")
data class NewEntity(...)

// 2. Create DAO in data/database/Daos.kt
@Dao
interface NewEntityDao { ... }

// 3. Add to PokemonTrackerDatabase.kt
abstract fun newEntityDao(): NewEntityDao
```

### Add ViewModel State
```kotlin
data class NewUiState(
    val data: List<Item> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NewViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewUiState())
    val uiState: StateFlow<NewUiState> = _uiState.asStateFlow()
}
```

## 🎨 Design System

### Colors
```kotlin
PokemonColors.Primary        // #FF5722 (Orange - Pokémon Red)
PokemonColors.PrimaryDark    // #E64A19
PokemonColors.Accent         // #FFEB3B (Yellow)
PokemonColors.FireType       // #FDA113
PokemonColors.WaterType      // #87CEEB
PokemonColors.GrassType      // #78C850
// ... and 15 more type colors
```

### Spacing
```kotlin
8.dp    // Small
12.dp   // Medium
16.dp   // Large
24.dp   // XL
32.dp   // XXL
```

### Text Styles
```kotlin
fontSize = 12.sp      // Small
fontSize = 14.sp      // Body
fontSize = 18.sp      // Subtitle
fontSize = 24.sp      // Title
fontWeight = FontWeight.Bold
```

## 🏗️ Code Structure Examples

### ViewModel Pattern
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun doSomething() {
        viewModelScope.launch {
            _uiState.value = UiState(loading = true)
            try {
                val data = repository.fetchData()
                _uiState.value = UiState(data = data)
            } catch (e: Exception) {
                _uiState.value = UiState(error = e.message)
            }
        }
    }
}
```

### Composable Pattern
```kotlin
@Composable
fun MyScreen(
    viewModel: MyViewModel = hiltViewModel(),
    onNavigate: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column {
        when {
            uiState.loading -> LoadingIndicator()
            uiState.error != null -> ErrorMessage(uiState.error)
            else -> Content(uiState.data)
        }
    }
}
```

### Repository Pattern
```kotlin
class MyRepository @Inject constructor(
    private val api: PokemonTCGApi,
    private val dao: MyDao
) {
    suspend fun fetchData(query: String): List<Item> {
        return try {
            val response = api.search(query)
            dao.insertAll(response.items)
            response.items
        } catch (e: Exception) {
            dao.getAll()  // Return cached
        }
    }
}
```

## 🔌 API Quick Reference

### Search Cards
```kotlin
repository.searchPokemonCards("Pikachu", "en")
// Returns: List<Card>
```

### Get Card Details
```kotlin
repository.getCardById("sv04pt-1")
// Returns: Card?
```

### Mark Card as Owned
```kotlin
repository.markCardAsOwned(userId, cardId, CardCondition.NEAR_MINT)
```

### Add Favorite
```kotlin
repository.addFavoritePokemon(userId, "Pikachu")
```

### Get User Collection
```kotlin
repository.getUserCards(userId)  // Returns: Flow<List<UserCard>>
```

## 💾 Database Quick Reference

### Card Queries
```kotlin
// Get card by ID
cardDao.getCardById(cardId)

// Get cards by name
cardDao.getCardsByPokemonName("Pikachu")

// Get all cards
cardDao.getAllCards()

// Insert cards
cardDao.insertCards(listOf(card1, card2))
```

### User Card Queries
```kotlin
// Add to collection
userCardDao.insertUserCard(userCard)

// Get user's collection
userCardDao.getUserCards(userId)

// Get only owned cards
userCardDao.getUserOwnedCards(userId)

// Update card condition
userCardDao.updateUserCard(userCard)
```

## 📡 HTTP Logging

### Enable API Logging
Already configured in `AppModule.kt`. Logs are visible in Logcat:

```
D/OkHttp: --> GET /cards?q=name:Pikachu http/1.1
D/OkHttp: <-- 200 OK
D/OkHttp: response body (JSON)
```

## 🐛 Debug Tips

### Check Database
```kotlin
// In console
adb shell
sqlite3 /data/data/com.example.pokemonmastersettracker/databases/pokemon_tracker_db
> SELECT * FROM cards;
```

### View Logs
```bash
Ctrl + Alt + 6  # Open Logcat
```

### Break on Exception
```
Debug menu → Breakpoints → View Breakpoints
Add exception breakpoint for specific exceptions
```

### State Inspection
```kotlin
// Add to ViewModel for debugging
Log.d("ViewModel", "Current state: ${_uiState.value}")
```

## 🧪 Testing Patterns

### Test ViewModel
```kotlin
@Test
fun testSearch() {
    val vm = CardViewModel(mockRepository)
    vm.searchPokemonCards("Pikachu")
    assertTrue(vm.cardUiState.value.cards.isNotEmpty())
}
```

### Test Composable
```kotlin
@Test
fun testCardItem() {
    composeTestRule.setContent {
        CardItem(card = mockCard, onCardClick = {}, onFavoriteToggle = {})
    }
    composeTestRule.onNodeWithText("Pikachu").assertExists()
}
```

### Test Repository
```kotlin
@Test
fun testSearchCards() = runTest {
    val result = repository.searchPokemonCards("Pikachu")
    assertEquals(10, result.size)
}
```

## 📱 Compose Snippets

### LazyColumn
```kotlin
LazyColumn {
    items(items) { item ->
        ItemCard(item)
    }
}
```

### LazyVerticalGrid
```kotlin
LazyVerticalGrid(columns = GridCells.Fixed(2)) {
    items(items) { item ->
        CardItem(item)
    }
}
```

### Conditional Rendering
```kotlin
when {
    uiState.loading -> LoadingIndicator()
    uiState.error != null -> ErrorDialog(uiState.error)
    uiState.data.isEmpty() -> EmptyState()
    else -> Content(uiState.data)
}
```

### State in Composable
```kotlin
var searchText by remember { mutableStateOf("") }
OutlinedTextField(
    value = searchText,
    onValueChange = { searchText = it }
)
```

## 🚨 Common Errors & Fixes

### "Unresolved reference to X"
```
Fix: Build → Clean Project → Sync Now
```

### "Room cannot find table"
```
Fix: Increment database version and add migration
```

### "Hilt injection failed"
```
Fix: Check @AndroidEntryPoint on Activity/Fragment
     Check @HiltViewModel on ViewModel
     Run Build → Clean Project
```

### "API returns null"
```
Fix: Check query syntax
     Check network connectivity
     Review response in Logcat
     Use Postman to test query
```

### "Image not loading"
```
Fix: Check internet permission in manifest
     Check image URL is valid
     Check Coil dependency version
     Add error handling in AsyncImage
```

## 🔗 Important Links

**In-Project:**
- README.md - Full documentation
- QUICK_START.md - Architecture guide
- API_DOCUMENTATION.md - API reference
- SETUP_CHECKLIST.md - Setup progress

**External:**
- PokemonTCG.io Docs: https://docs.pokemontcg.io/
- Android Compose: https://developer.android.com/jetpack/compose
- Room Database: https://developer.android.com/training/data-storage/room
- Hilt: https://developer.android.com/training/dependency-injection/hilt-android
- Coil: https://coil-kt.github.io/coil/

## ⌨️ IDE Shortcuts (Android Studio)

```
Shift + Shift      # Search everything
Ctrl + Shift + A   # Find action
Ctrl + K           # Commit changes
Ctrl + Alt + F12   # Open file in explorer
Ctrl + L           # Go to line
Ctrl + F           # Find in file
Ctrl + H           # Find & replace
```

## 🎯 Development Workflow

1. **Identify task** → Check docs
2. **Find file** → Use Ctrl + N or search
3. **Edit code** → Follow patterns from similar code
4. **Build** → Ctrl + F9
5. **Test** → Run on device (Shift + F10)
6. **Debug** → Use Logcat and breakpoints
7. **Commit** → Use Git through IDE

## 💡 Pro Tips

1. **Format code** before committing - Ctrl + Alt + L
2. **Check errors** - Alt + F10 or Status bar
3. **Use quick documentation** - Ctrl + Q on any element
4. **Organize imports** - Ctrl + Alt + O
5. **Create from template** - Right-click folder → New
6. **Rename refactor** - Shift + F6 (renames all usages)
7. **Extract method** - Ctrl + Alt + M
8. **Generate code** - Alt + Insert (getters, etc.)

## 📊 Project Statistics

- **Lines of Code:** ~2,000
- **Kotlin Files:** 23
- **Documentation:** 6 guides
- **Dependencies:** 23
- **Screens:** 5
- **ViewModels:** 4
- **Database Tables:** 4
- **API Endpoints:** 6
- **UI Components:** 3

---

**Last Updated:** December 29, 2025  
**Target Android:** API 24+
