package com.example.pokemonmastersettracker.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokemonmastersettracker.data.models.Card
import com.example.pokemonmastersettracker.data.models.CardCondition
import com.example.pokemonmastersettracker.data.models.Pokemon
import com.example.pokemonmastersettracker.data.repository.PokemonRepository
import com.example.pokemonmastersettracker.data.api.TCGdexService
import com.example.pokemonmastersettracker.utils.DatabaseExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CardSortOption {
    NONE,           // Default - no sorting
    SET_NAME,       // Sort by set name (alphabetical)
    PRICE_LOW,      // Sort by price (low to high)
    PRICE_HIGH,     // Sort by price (high to low)
    RARITY,         // Sort by rarity
    CARD_NUMBER     // Sort by card number
}

data class CardUiState(
    val pokemonList: List<Pokemon> = emptyList(), // Pokemon from local database
    val cards: List<Card> = emptyList(),
    val allCards: List<Card> = emptyList(), // Store all cards from search
    val loading: Boolean = false,
    val error: String? = null,
    val selectedPokemonName: String? = null,
    val currentPage: Int = 1,
    val hasMorePages: Boolean = false,
    val pageSize: Int = 250,
    val lastQuery: String? = null, // For debugging - shows the actual query used
    val debugInfo: String? = null, // For debugging - shows diagnostic information
    val sortOption: CardSortOption = CardSortOption.NONE,
    val databaseExported: Boolean = false, // Track if database was exported
    val preFetchComplete: Boolean = false, // Track if pre-fetch completed
    val preFetchProgress: Int = 0, // Current pokemon being fetched (0-56)
    val preFetchTotal: Int = 56, // Total pokemon to fetch (expanded from 31 to 56)
    val preFetchCached: Int = 0, // Already cached
    val preFetchSuccess: Int = 0, // Successfully fetched
    val preFetchFailed: Int = 0, // Failed after retries
    val currentPokemon: String = "", // Name of pokemon currently being fetched
    val showTrackingDialog: String? = null // Pokemon name to show tracking dialog for
)

@HiltViewModel
class CardViewModel @Inject constructor(
    private val repository: PokemonRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _cardUiState = MutableStateFlow(CardUiState())
    val cardUiState: StateFlow<CardUiState> = _cardUiState.asStateFlow()

    private val _selectedCard = MutableStateFlow<Card?>(null)
    val selectedCard: StateFlow<Card?> = _selectedCard.asStateFlow()
    
    // Cache recent searches to avoid duplicate API calls
    private val searchCache = mutableMapOf<String, List<Card>>()
    
    // TCGdex service for Japanese card support
    private val tcgdexService = TCGdexService()
    
    // Default user ID (in production, this would come from authentication)
    // This matches the userId used in MainActivity: "test-user"
    private val defaultUserId = "test-user"
    
    init {
        // Initialize app on first launch
        viewModelScope.launch {
            try {
                // Ensure test user exists in database
                repository.createUser("test@example.com", "test-user")
                android.util.Log.d("CardViewModel", "✓ Test user created/verified")
            } catch (e: Exception) {
                // User might already exist, that's okay
                android.util.Log.d("CardViewModel", "Test user already exists or error: ${e.message}")
            }
            
            // Seed Pokemon database
            repository.seedPopularPokemon()
            
            // Pre-fetch popular Pokemon cards in background
            // This caches data locally for instant searches
            android.util.Log.d("CardViewModel", "🚀 Starting background pre-fetch of popular Pokemon cards...")
            // Run in background coroutine so it doesn't block app startup
            launch {
                try {
                    val stats = repository.preFetchPopularPokemonCards(
                        onProgress = { current, total, pokemonName, cached, success, failed ->
                            _cardUiState.value = _cardUiState.value.copy(
                                preFetchProgress = current,
                                preFetchTotal = total,
                                currentPokemon = pokemonName,
                                preFetchCached = cached,
                                preFetchSuccess = success,
                                preFetchFailed = failed
                            )
                        }
                    )
                    android.util.Log.d("CardViewModel", "✓ Pre-fetch complete: cached=${stats.first}, fetched=${stats.second}, failed=${stats.third}")
                    
                    // Update UI state
                    _cardUiState.value = _cardUiState.value.copy(preFetchComplete = true)
                    
                    // Only export database if we actually fetched or have cached data
                    val totalData = stats.first + stats.second
                    if (totalData > 0) {
                        // Show notification that pre-fetch is done
                        android.widget.Toast.makeText(
                            context,
                            "✓ Pre-fetch complete! ($totalData Pokemon) Exporting database...",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        
                        // After pre-fetch, export database for bundling with future app versions
                        android.util.Log.d("CardViewModel", "📦 Exporting database for bundling...")
                        val exportPath = DatabaseExporter.exportDatabase(context)
                        if (exportPath != null) {
                            android.util.Log.d("CardViewModel", "✓ Database exported to: $exportPath")
                            android.util.Log.d("CardViewModel", "📋 Next steps:")
                            android.util.Log.d("CardViewModel", "   1. Copy this file from device to: app/src/main/assets/database/")
                            android.util.Log.d("CardViewModel", "   2. Rename to: pokemon_tracker_prepopulated.db")
                            android.util.Log.d("CardViewModel", "   3. Rebuild app - it will use pre-populated data!")
                            
                            // Update UI state
                            _cardUiState.value = _cardUiState.value.copy(databaseExported = true)
                            
                            // Show success notification
                            android.widget.Toast.makeText(
                                context,
                                "✓ Database exported! $totalData Pokemon ready for bundling",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        } else {
                            // Show error notification
                            android.widget.Toast.makeText(
                                context,
                                "⚠️ Database export failed - check permissions",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        android.widget.Toast.makeText(
                            context,
                            "⚠️ Pre-fetch failed - no data cached. API may be having issues.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CardViewModel", "⚠️ Pre-fetch failed: ${e.message}")
                    // Show error notification
                    android.widget.Toast.makeText(
                        context,
                        "⚠️ Pre-fetch failed: ${e.message}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // NEW: Search Pokemon locally (instant results)
    fun searchPokemonLocal(query: String) {
        viewModelScope.launch {
            _cardUiState.value = CardUiState(loading = true)
            try {
                android.util.Log.d("CardViewModel", "Local search for: $query")
                val results = repository.searchPokemonLocal(query)
                android.util.Log.d("CardViewModel", "Found ${results.size} Pokemon locally")
                _cardUiState.value = CardUiState(pokemonList = results)
            } catch (e: Exception) {
                android.util.Log.e("CardViewModel", "Local search error: ${e.message}")
                _cardUiState.value = CardUiState(error = e.message ?: "Unknown error")
            }
        }
    }
    
    // NEW: Toggle favorite status
    fun toggleFavorite(pokemonName: String) {
        viewModelScope.launch {
            try {
                // Check current favorite status
                val pokemon = repository.searchPokemonLocal(pokemonName).firstOrNull()
                val isFavorited = pokemon?.isFavorite == true
                
                if (!isFavorited) {
                    // Favoriting - show tracking dialog
                    _cardUiState.value = _cardUiState.value.copy(showTrackingDialog = pokemonName)
                } else {
                    // Unfavoriting - remove from both pokemon and favorite_pokemon
                    repository.toggleFavorite(pokemonName)
                    repository.removeFavoritePokemon(defaultUserId, pokemonName)
                    
                    // Refresh the current search results
                    val currentState = _cardUiState.value
                    if (currentState.pokemonList.isNotEmpty()) {
                        val updated = currentState.pokemonList.map { p ->
                            if (p.name == pokemonName) p.copy(isFavorite = false) else p
                        }
                        _cardUiState.value = currentState.copy(pokemonList = updated)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CardViewModel", "Toggle favorite error: ${e.message}")
            }
        }
    }
    
    // Handle user's choice on collection tracking
    fun updateFavoriteWithTracking(pokemonName: String, enableTracking: Boolean) {
        viewModelScope.launch {
            try {
                // Always set as favorite in pokemon table
                repository.setFavoriteStatus(pokemonName, true)
                
                if (enableTracking) {
                    // Add to favorite_pokemon for collection tracking
                    repository.addFavoritePokemon(defaultUserId, pokemonName)
                    android.util.Log.d("CardViewModel", "✓ Added $pokemonName with collection tracking")
                } else {
                    android.util.Log.d("CardViewModel", "✓ Favorited $pokemonName without collection tracking")
                }
                
                // Update pokemonList and clear dialog, but preserve selectedPokemonName and cards
                val currentState = _cardUiState.value
                if (currentState.pokemonList.isNotEmpty()) {
                    val updated = currentState.pokemonList.map { p ->
                        if (p.name == pokemonName) p.copy(isFavorite = true) else p
                    }
                    _cardUiState.value = currentState.copy(
                        pokemonList = updated,
                        showTrackingDialog = null
                        // Preserve: selectedPokemonName, cards, allCards, loading, error, etc.
                    )
                } else {
                    _cardUiState.value = currentState.copy(
                        showTrackingDialog = null
                        // Preserve all other state
                    )
                }
                android.util.Log.d("CardViewModel", "State after favorite update - selectedPokemon: ${_cardUiState.value.selectedPokemonName}, cards: ${_cardUiState.value.cards.size}")
            } catch (e: Exception) {
                android.util.Log.e("CardViewModel", "Update favorite error: ${e.message}")
                _cardUiState.value = _cardUiState.value.copy(showTrackingDialog = null)
            }
        }
    }
    
    fun dismissTrackingDialog() {
        android.util.Log.d("CardViewModel", "❌ Tracking dialog dismissed without action")
        _cardUiState.value = _cardUiState.value.copy(showTrackingDialog = null)
    }

    // MODIFIED: Now loads cards from API when Pokemon is clicked (English only, as API doesn't have Japanese card data)
    fun selectPokemonCards(pokemonName: String, languages: Set<String> = setOf("en"), page: Int = 1, pageSize: Int = 250, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            android.util.Log.d("CardViewModel", "🎯 selectPokemonCards called for: $pokemonName")
            _cardUiState.value = _cardUiState.value.copy(
                loading = true, 
                selectedPokemonName = pokemonName,
                lastQuery = "Loading...",
                debugInfo = "Searching: $pokemonName | Page: $page | PageSize: $pageSize | Cache: ${!forceRefresh}",
                showTrackingDialog = null // Clear any dialog when selecting cards
            )
            try {
                android.util.Log.d("CardViewModel", "Loading cards for: $pokemonName (page: $page, pageSize: $pageSize, languages: $languages, forceRefresh: $forceRefresh)")
                
                // The Pokemon TCG API returns ALL cards (English, Japanese, etc.) in one query
                // The repository filters by language after fetching
                // pageSize controls how many cards per page
                // page controls which page of results to fetch
                val cards = repository.searchPokemonCardsWithPagination(
                    pokemonName, 
                    languages, // Pass Set<String> of selected languages
                    page, 
                    pageSize,
                    forceRefresh
                )
                
                android.util.Log.d("CardViewModel", "Loaded ${cards.size} cards for page $page")
                
                // Save the first card's image to Pokemon entity for future searches
                cards.firstOrNull()?.image?.small?.let { imageUrl ->
                    repository.updatePokemonImage(pokemonName, imageUrl)
                    android.util.Log.d("CardViewModel", "Saved image for $pokemonName")
                }
                
                // If we got exactly pageSize cards, there might be more
                val hasMore = cards.size >= pageSize
                
                // If loading more pages (page > 1), append to existing cards
                val allCards = if (page > 1) {
                    _cardUiState.value.allCards + cards
                } else {
                    cards
                }
                
                _cardUiState.value = _cardUiState.value.copy(
                    cards = allCards,
                    allCards = allCards,
                    loading = false,
                    error = null,
                    selectedPokemonName = pokemonName,
                    currentPage = page,
                    hasMorePages = hasMore,
                    pageSize = pageSize,
                    debugInfo = "✓ Loaded ${cards.size} cards for page $page (Total: ${allCards.size})"
                )
            } catch (e: Exception) {
                android.util.Log.e("CardViewModel", "Error loading cards: ${e.message}", e)
                val errorType = when {
                    e.message?.contains("404") == true -> "404 Not Found"
                    e.message?.contains("504") == true -> "504 Timeout"
                    else -> e.javaClass.simpleName
                }
                _cardUiState.value = _cardUiState.value.copy(
                    error = e.message ?: "Unknown error",
                    loading = false,
                    debugInfo = "✗ Failed: $errorType | Pokemon: $pokemonName | Check Logcat for query details"
                )
            }
        }
    }
    
    fun loadMoreCards(languages: Set<String> = setOf("en")) {
        val currentState = _cardUiState.value
        currentState.selectedPokemonName?.let { pokemonName ->
            selectPokemonCards(pokemonName, languages, currentState.currentPage + 1, currentState.pageSize)
        }
    }
    
    fun changePageSize(newPageSize: Int, languages: Set<String> = setOf("en")) {
        val currentState = _cardUiState.value
        currentState.selectedPokemonName?.let { pokemonName ->
            // Reset to page 1 when changing page size
            selectPokemonCards(pokemonName, languages, 1, newPageSize)
        }
    }
    
    fun setSortOption(sortOption: CardSortOption) {
        val currentCards = _cardUiState.value.allCards
        val sortedCards = when (sortOption) {
            CardSortOption.NONE -> currentCards
            CardSortOption.SET_NAME -> currentCards.sortedBy { it.set?.name ?: "" }
            CardSortOption.PRICE_LOW -> currentCards.sortedBy { 
                it.tcgplayer?.prices?.get("normal")?.market ?: it.tcgplayer?.prices?.get("holofoil")?.market ?: Double.MAX_VALUE
            }
            CardSortOption.PRICE_HIGH -> currentCards.sortedByDescending { 
                it.tcgplayer?.prices?.get("normal")?.market ?: it.tcgplayer?.prices?.get("holofoil")?.market ?: 0.0
            }
            CardSortOption.RARITY -> {
                val rarityOrder = mapOf(
                    "Common" to 1,
                    "Uncommon" to 2,
                    "Rare" to 3,
                    "Rare Holo" to 4,
                    "Rare Ultra" to 5,
                    "Rare Secret" to 6
                )
                currentCards.sortedBy { rarityOrder[it.rarity] ?: 999 }
            }
            CardSortOption.CARD_NUMBER -> currentCards.sortedBy { 
                it.number?.toIntOrNull() ?: Int.MAX_VALUE
            }
        }
        
        _cardUiState.value = _cardUiState.value.copy(
            cards = sortedCards,
            allCards = sortedCards,
            sortOption = sortOption
        )
    }

    fun getCardsByPokemonAndSet(pokemonName: String, setId: String) {
        viewModelScope.launch {
            _cardUiState.value = CardUiState(loading = true)
            try {
                val cards = repository.getCardsByPokemonAndSet(pokemonName, setId)
                _cardUiState.value = CardUiState(cards = cards, selectedPokemonName = pokemonName)
            } catch (e: Exception) {
                _cardUiState.value = CardUiState(error = e.message ?: "Unknown error")
            }
        }
    }

    fun selectCard(card: Card) {
        _selectedCard.value = card
    }

    fun clearSelection() {
        _selectedCard.value = null
        _cardUiState.value = CardUiState()
    }
    
    fun loadFavorites() {
        viewModelScope.launch {
            _cardUiState.value = _cardUiState.value.copy(loading = true)
            try {
                // Use single JOIN query Flow - NO database queries inside collect prevents infinite loop
                repository.getUserFavoritePokemonWithDetails(defaultUserId).collect { pokemonList ->
                    android.util.Log.d("CardViewModel", "✓ Loaded ${pokemonList.size} favorite Pokemon with details (single query)")
                    // Preserve selectedPokemonName and cards when updating favorites list
                    _cardUiState.value = _cardUiState.value.copy(
                        pokemonList = pokemonList,
                        loading = false
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("CardViewModel", "❌ Error loading favorites: ${e.message}", e)
                _cardUiState.value = _cardUiState.value.copy(
                    error = e.message ?: "Unknown error",
                    loading = false
                )
            }
        }
    }
    
    // Collection Management
    
    suspend fun isCardOwned(cardId: String): Boolean {
        val result = repository.isCardOwned(defaultUserId, cardId)
        android.util.Log.d("CardViewModel", "Check owned: cardId=$cardId, result=$result")
        return result
    }
    
    fun toggleCardOwnership(cardId: String, currentlyOwned: Boolean, condition: com.example.pokemonmastersettracker.data.models.CardCondition = com.example.pokemonmastersettracker.data.models.CardCondition.NEAR_MINT) {
        viewModelScope.launch {
            try {
                android.util.Log.d("CardViewModel", "Toggle ownership: cardId=$cardId, currentlyOwned=$currentlyOwned, condition=$condition, userId=$defaultUserId")
                if (currentlyOwned) {
                    repository.markCardAsMissing(defaultUserId, cardId)
                    android.util.Log.d("CardViewModel", "✓ Removed from collection: $cardId")
                } else {
                    repository.markCardAsOwned(defaultUserId, cardId, condition)
                    android.util.Log.d("CardViewModel", "✓ Added to collection: $cardId with condition: $condition")
                    
                    // Auto-favorite the Pokemon when adding a card to collection (only if not already favorited)
                    _cardUiState.value.selectedPokemonName?.let { pokemonName ->
                        // Check synchronously to avoid race conditions
                        val alreadyFavorited = repository.isFavoritePokemon(defaultUserId, pokemonName)
                        if (!alreadyFavorited) {
                            android.util.Log.d("CardViewModel", "⚡ Auto-favoriting Pokemon: $pokemonName")
                            repository.addFavoritePokemon(defaultUserId, pokemonName)
                        } else {
                            android.util.Log.d("CardViewModel", "ℹ️ $pokemonName already favorited, skipping")
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CardViewModel", "❌ Error toggling ownership: ${e.message}", e)
            }
        }
    }
    
    // Wishlist Management
    
    suspend fun isInWishlist(cardId: String): Boolean {
        val result = repository.isInWishlist(defaultUserId, cardId)
        android.util.Log.d("CardViewModel", "Check wishlist: cardId=$cardId, result=$result")
        return result
    }
    
    fun toggleWishlist(cardId: String, currentlyInWishlist: Boolean) {
        viewModelScope.launch {
            try {
                android.util.Log.d("CardViewModel", "Toggle wishlist: cardId=$cardId, currentlyInWishlist=$currentlyInWishlist, userId=$defaultUserId")
                if (currentlyInWishlist) {
                    repository.removeFromWishlist(defaultUserId, cardId)
                    android.util.Log.d("CardViewModel", "✓ Removed from wishlist: $cardId")
                } else {
                    repository.addToWishlist(defaultUserId, cardId)
                    android.util.Log.d("CardViewModel", "✓ Added to wishlist: $cardId")
                }
            } catch (e: Exception) {
                android.util.Log.e("CardViewModel", "❌ Error toggling wishlist: ${e.message}", e)
            }
        }
    }

    // Test direct API call with simplest possible query
    suspend fun testDirectApiCall(): String {
        return try {
            val cards = repository.searchPokemonCards("Pikachu")
            "SUCCESS: Retrieved ${cards.size} cards"
        } catch (e: Exception) {
            "ERROR: ${e.message}"
        }
    }
    
    /**
     * Load Pokemon cards from TCGdex API with language selection
     * Used in Favorites screen for English/Japanese card support
     */
    fun loadCardsFromTCGdex(pokemonName: String, language: String = "en") {
        viewModelScope.launch {
            android.util.Log.d("CardViewModel", "🌏 Loading cards from TCGdex: $pokemonName (language: $language)")
            _cardUiState.value = _cardUiState.value.copy(
                loading = true,
                selectedPokemonName = pokemonName,
                showTrackingDialog = null
            )
            
            try {
                val cards = tcgdexService.searchCardsByPokemon(pokemonName, language)
                
                android.util.Log.d("CardViewModel", "✓ TCGdex returned ${cards.size} cards")
                
                _cardUiState.value = _cardUiState.value.copy(
                    cards = cards,
                    allCards = cards,
                    loading = false,
                    error = if (cards.isEmpty()) "No cards found" else null
                )
            } catch (e: Exception) {
                android.util.Log.e("CardViewModel", "❌ Error loading TCGdex cards: ${e.message}", e)
                _cardUiState.value = _cardUiState.value.copy(
                    loading = false,
                    error = "Error loading cards: ${e.message}"
                )
            }
        }
    }
}
