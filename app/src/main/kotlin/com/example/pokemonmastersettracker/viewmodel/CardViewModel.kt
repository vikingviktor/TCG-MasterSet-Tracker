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
    val showTrackingDialog: String? = null, // Pokemon name to show tracking dialog for
    val currentLanguage: String = "en" // Track current search language
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

    // Wrapper method for compatibility - now uses TCGdex API
    fun selectPokemonCards(pokemonName: String, languages: Set<String> = setOf("en"), page: Int = 1, pageSize: Int = 250, forceRefresh: Boolean = false) {
        // Use the primary language (TCGdex doesn't support multiple languages in one call)
        val primaryLanguage = languages.firstOrNull() ?: "en"
        loadCardsFromTCGdex(pokemonName, primaryLanguage)
    }
    
    // Pagination methods removed - TCGdex returns all cards at once
    
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

    // getCardsByPokemonAndSet removed - old API method not supported by TCGdex

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
    
    fun addAllMissingCardsToWishlist() {
        viewModelScope.launch {
            try {
                val cards = _cardUiState.value.cards
                val pokemonName = _cardUiState.value.selectedPokemonName
                
                android.util.Log.d("CardViewModel", "Adding all missing cards to wishlist for $pokemonName")
                
                var addedCount = 0
                cards.forEach { card ->
                    val isOwned = isCardOwned(card.id)
                    val isInWishlist = isInWishlist(card.id)
                    
                    // Add to wishlist if not owned and not already in wishlist
                    if (!isOwned && !isInWishlist) {
                        repository.addToWishlist(defaultUserId, card.id)
                        addedCount++
                    }
                }
                
                android.util.Log.d("CardViewModel", "✓ Added $addedCount missing cards to wishlist")
            } catch (e: Exception) {
                android.util.Log.e("CardViewModel", "❌ Error adding missing cards to wishlist: ${e.message}", e)
            }
        }
    }

    // testDirectApiCall removed - old Pokemon TCG API no longer used
    
    /**
     * Load Pokemon cards from TCGdex API with language selection
     * Used in Favorites screen for English/Japanese card support
     */
    fun loadCardsFromTCGdex(pokemonName: String, language: String = "en") {
        viewModelScope.launch {
            android.util.Log.d("CardViewModel", "🌏 Loading cards for: $pokemonName (language: $language)")
            _cardUiState.value = _cardUiState.value.copy(
                loading = true,
                selectedPokemonName = pokemonName,
                showTrackingDialog = null,
                currentLanguage = language
            )
            
            try {
                // Only use cache for English cards to avoid language conflicts
                val cachedCards = if (language == "en") {
                    repository.getCachedCardsForPokemon(pokemonName)
                } else {
                    emptyList()
                }
                
                if (cachedCards.isNotEmpty()) {
                    android.util.Log.d("CardViewModel", "📦 Found ${cachedCards.size} cached English cards for $pokemonName")
                    
                    // Show cached cards immediately
                    _cardUiState.value = _cardUiState.value.copy(
                        cards = cachedCards,
                        allCards = cachedCards,
                        loading = false,
                        error = null
                    )
                    
                    android.util.Log.d("CardViewModel", "✓ Loaded cards from cache instantly")
                } else {
                    // No cache, load from API
                    android.util.Log.d("CardViewModel", "🌐 No cached cards, fetching from TCGdex API...")
                    
                    // Clear existing cards to prevent mixing with previous search
                    _cardUiState.value = _cardUiState.value.copy(
                        cards = emptyList(),
                        allCards = emptyList()
                    )
                    
                    val cards = tcgdexService.searchCardsByPokemon(pokemonName, language)
                    
                    android.util.Log.d("CardViewModel", "✓ TCGdex returned ${cards.size} cards")
                    
                    // Save cards to database for future cache hits (only for English to avoid conflicts)
                    if (cards.isNotEmpty() && language == "en") {
                        repository.saveCards(cards)
                        android.util.Log.d("CardViewModel", "✓ Saved ${cards.size} cards to database cache")
                        
                        // Update Pokemon image if we don't have one
                        cards.firstOrNull()?.image?.small?.let { imageUrl ->
                            repository.updatePokemonImage(pokemonName, imageUrl)
                        }
                    } else if (cards.isNotEmpty() && language != "en") {
                        android.util.Log.d("CardViewModel", "ℹ Skipping cache save for non-English cards")
                    }
                    
                    _cardUiState.value = _cardUiState.value.copy(
                        cards = cards,
                        allCards = cards,
                        loading = false,
                        error = if (cards.isEmpty()) "No cards found" else null
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("CardViewModel", "❌ Error loading cards: ${e.message}", e)
                _cardUiState.value = _cardUiState.value.copy(
                    loading = false,
                    error = "Error loading cards: ${e.message}"
                )
            }
        }
    }
}
