package com.example.pokemonmastersettracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokemonmastersettracker.data.models.Card
import com.example.pokemonmastersettracker.data.models.CardCondition
import com.example.pokemonmastersettracker.data.models.Pokemon
import com.example.pokemonmastersettracker.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val pageSize: Int = 25,
    val lastQuery: String? = null, // For debugging - shows the actual query used
    val debugInfo: String? = null, // For debugging - shows diagnostic information
    val sortOption: CardSortOption = CardSortOption.NONE
)

@HiltViewModel
class CardViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    private val _cardUiState = MutableStateFlow(CardUiState())
    val cardUiState: StateFlow<CardUiState> = _cardUiState.asStateFlow()

    private val _selectedCard = MutableStateFlow<Card?>(null)
    val selectedCard: StateFlow<Card?> = _selectedCard.asStateFlow()
    
    // Cache recent searches to avoid duplicate API calls
    private val searchCache = mutableMapOf<String, List<Card>>()
    
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
                repository.toggleFavorite(pokemonName)
                // Refresh the current search results
                val currentState = _cardUiState.value
                if (currentState.pokemonList.isNotEmpty()) {
                    val updated = currentState.pokemonList.map { pokemon ->
                        if (pokemon.name == pokemonName) {
                            pokemon.copy(isFavorite = !pokemon.isFavorite)
                        } else pokemon
                    }
                    _cardUiState.value = currentState.copy(pokemonList = updated)
                }
            } catch (e: Exception) {
                android.util.Log.e("CardViewModel", "Toggle favorite error: ${e.message}")
            }
        }
    }

    // MODIFIED: Now loads cards from API when Pokemon is clicked with multi-language support
    fun selectPokemonCards(pokemonName: String, languages: Set<String> = setOf("en"), page: Int = 1, pageSize: Int = 25) {
        viewModelScope.launch {
            _cardUiState.value = _cardUiState.value.copy(
                loading = true, 
                selectedPokemonName = pokemonName,
                lastQuery = "Loading...",
                debugInfo = "Searching: $pokemonName | Page: $page | PageSize: $pageSize"
            )
            try {
                android.util.Log.d("CardViewModel", "Loading cards for: $pokemonName (page: $page, pageSize: $pageSize)")
                
                // The Pokemon TCG API returns ALL cards (English, Japanese, etc.) in one query
                // pageSize controls how many cards per page
                // page controls which page of results to fetch
                val cards = repository.searchPokemonCardsWithPagination(
                    pokemonName, 
                    "en", // Language param is not used by API, but required by signature
                    page, 
                    pageSize
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
            _cardUiState.value = CardUiState(loading = true)
            try {
                val favorites = repository.getFavoritePokemon()
                android.util.Log.d("CardViewModel", "Loaded ${favorites.size} favorite Pokemon")
                _cardUiState.value = CardUiState(pokemonList = favorites)
            } catch (e: Exception) {
                android.util.Log.e("CardViewModel", "Error loading favorites: ${e.message}", e)
                _cardUiState.value = CardUiState(error = e.message ?: "Unknown error")
            }
        }
    }
    
    // Collection Management
    
    suspend fun isCardOwned(cardId: String): Boolean {
        val result = repository.isCardOwned(defaultUserId, cardId)
        android.util.Log.d("CardViewModel", "Check owned: cardId=$cardId, result=$result")
        return result
    }
    
    fun toggleCardOwnership(cardId: String, currentlyOwned: Boolean) {
        viewModelScope.launch {
            try {
                android.util.Log.d("CardViewModel", "Toggle ownership: cardId=$cardId, currentlyOwned=$currentlyOwned, userId=$defaultUserId")
                if (currentlyOwned) {
                    repository.markCardAsMissing(defaultUserId, cardId)
                    android.util.Log.d("CardViewModel", "✓ Removed from collection: $cardId")
                } else {
                    repository.markCardAsOwned(defaultUserId, cardId)
                    android.util.Log.d("CardViewModel", "✓ Added to collection: $cardId")
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
}
