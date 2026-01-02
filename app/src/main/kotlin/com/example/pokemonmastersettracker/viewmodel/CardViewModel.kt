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

    // MODIFIED: Now loads cards from API when Pokemon is clicked
    fun selectPokemonCards(pokemonName: String) {
        viewModelScope.launch {
            _cardUiState.value = _cardUiState.value.copy(loading = true, selectedPokemonName = pokemonName)
            try {
                android.util.Log.d("CardViewModel", "Loading all cards for: $pokemonName")
                val cards = repository.searchPokemonCards(pokemonName, "en")
                android.util.Log.d("CardViewModel", "Loaded ${cards.size} cards")
                
                _cardUiState.value = _cardUiState.value.copy(
                    cards = cards,
                    allCards = cards,
                    loading = false,
                    selectedPokemonName = pokemonName
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
}
