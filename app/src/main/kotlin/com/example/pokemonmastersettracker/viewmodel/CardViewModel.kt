package com.example.pokemonmastersettracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokemonmastersettracker.data.models.Card
import com.example.pokemonmastersettracker.data.models.CardCondition
import com.example.pokemonmastersettracker.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PokemonSummary(
    val name: String,
    val cardCount: Int,
    val imageUrl: String?
)

data class CardUiState(
    val pokemonList: List<PokemonSummary> = emptyList(),
    val cards: List<Card> = emptyList(),
    val allCards: List<Card> = emptyList(), // Store all cards from search
    val loading: Boolean = false,
    val error: String? = null,
    val selectedPokemonName: String? = null
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

    fun searchPokemonCards(pokemonName: String, language: String = "en") {
        viewModelScope.launch {
            _cardUiState.value = CardUiState(loading = true)
            try {
                // Check cache first
                val cacheKey = "${pokemonName.lowercase()}_$language"
                val cachedCards = searchCache[cacheKey]
                
                val cards = if (cachedCards != null) {
                    android.util.Log.d("CardViewModel", "Using cached results for: $pokemonName")
                    cachedCards
                } else {
                    android.util.Log.d("CardViewModel", "Searching for: $pokemonName")
                    val fetchedCards = repository.searchPokemonCards(pokemonName, language)
                    searchCache[cacheKey] = fetchedCards // Cache the results
                    fetchedCards
                }
                android.util.Log.d("CardViewModel", "Got ${cards.size} cards")
                
                // Group cards by Pokemon name
                val pokemonGroups = cards.groupBy { it.name }
                val pokemonList = pokemonGroups.map { (name, cardList) ->
                    PokemonSummary(
                        name = name,
                        cardCount = cardList.size,
                        imageUrl = cardList.firstOrNull()?.image?.small
                    )
                }.sortedBy { it.name }
                
                // Store all cards and the pokemon list
                _cardUiState.value = CardUiState(
                    pokemonList = pokemonList, 
                    allCards = cards,
                    selectedPokemonName = null
                )
            } catch (e: Exception) {
                val userMessage = when {
                    e.message?.contains("504") == true -> "Pokemon TCG API is slow. Please try again."
                    e.message?.contains("timeout") == true -> "Request timed out. Check your internet connection."
                    e.message?.contains("Unable to resolve host") == true -> "No internet connection."
                    else -> {
                        val errorType = e.javaClass.simpleName
                        val errorLocation = e.stackTrace.firstOrNull()?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
                        "$errorType: ${e.message}\nAt: $errorLocation"
                    }
                }
                android.util.Log.e("CardViewModel", "Search error:\n${e.stackTraceToString()}")
                _cardUiState.value = CardUiState(error = "Error:$userMessage")
            }
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

    fun selectPokemonCards(pokemonName: String) {
        val currentState = _cardUiState.value
        // Filter from already loaded cards instead of making a new API call
        val filteredCards = currentState.allCards.filter { it.name == pokemonName }
        
        android.util.Log.d("CardViewModel", "Selecting cards for: $pokemonName")
        android.util.Log.d("CardViewModel", "Found ${filteredCards.size} cards")
        
        _cardUiState.value = currentState.copy(
            cards = filteredCards,
            selectedPokemonName = pokemonName
        )
    }

    fun selectCard(card: Card) {
        _selectedCard.value = card
    }

    fun clearSelection() {
        _selectedCard.value = null
        _cardUiState.value = CardUiState()
    }
}
