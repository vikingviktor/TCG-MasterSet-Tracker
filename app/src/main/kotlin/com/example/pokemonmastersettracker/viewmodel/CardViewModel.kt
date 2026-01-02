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

data class CardUiState(
    val pokemonList: List<Pokemon> = emptyList(), // Pokemon from local database
    val cards: List<Card> = emptyList(),
    val allCards: List<Card> = emptyList(), // Store all cards from search
    val loading: Boolean = false,
    val error: String? = null,
    val selectedPokemonName: String? = null,
    val currentPage: Int = 1,
    val hasMorePages: Boolean = false,
    val pageSize: Int = 25
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
    private val defaultUserId = "default_user"
    
    init {
        // Seed Pokemon database on first launch
        viewModelScope.launch {
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
            _cardUiState.value = _cardUiState.value.copy(loading = true, selectedPokemonName = pokemonName)
            try {
                android.util.Log.d("CardViewModel", "Loading cards for: $pokemonName (languages: $languages, page: $page)")
                
                // Fetch cards for all selected languages
                val allCards = mutableListOf<Card>()
                for (language in languages) {
                    val cards = repository.searchPokemonCardsWithPagination(pokemonName, language, page, pageSize)
                    allCards.addAll(cards)
                }
                
                android.util.Log.d("CardViewModel", "Loaded ${allCards.size} cards")
                
                // Save the first card's image to Pokemon entity for future searches
                allCards.firstOrNull()?.image?.small?.let { imageUrl ->
                    repository.updatePokemonImage(pokemonName, imageUrl)
                    android.util.Log.d("CardViewModel", "Saved image for $pokemonName")
                }
                
                val hasMore = allCards.size >= pageSize * languages.size
                
                _cardUiState.value = _cardUiState.value.copy(
                    cards = allCards,
                    allCards = allCards,
                    loading = false,
                    selectedPokemonName = pokemonName,
                    currentPage = page,
                    hasMorePages = hasMore,
                    pageSize = pageSize
                )
            } catch (e: Exception) {
                android.util.Log.e("CardViewModel", "Error loading cards: ${e.message}", e)
                _cardUiState.value = _cardUiState.value.copy(
                    error = e.message ?: "Unknown error",
                    loading = false
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
        return repository.isCardOwned(defaultUserId, cardId)
    }
    
    fun toggleCardOwnership(cardId: String, currentlyOwned: Boolean) {
        viewModelScope.launch {
            try {
                if (currentlyOwned) {
                    repository.markCardAsMissing(defaultUserId, cardId)
                } else {
                    repository.markCardAsOwned(defaultUserId, cardId)
                }
            } catch (e: Exception) {
                android.util.Log.e("CardViewModel", "Error toggling ownership: ${e.message}", e)
            }
        }
    }
    
    // Wishlist Management
    
    suspend fun isInWishlist(cardId: String): Boolean {
        return repository.isInWishlist(defaultUserId, cardId)
    }
    
    fun toggleWishlist(cardId: String, currentlyInWishlist: Boolean) {
        viewModelScope.launch {
            try {
                if (currentlyInWishlist) {
                    repository.removeFromWishlist(defaultUserId, cardId)
                } else {
                    repository.addToWishlist(defaultUserId, cardId)
                }
            } catch (e: Exception) {
                android.util.Log.e("CardViewModel", "Error toggling wishlist: ${e.message}", e)
            }
        }
    }
}
