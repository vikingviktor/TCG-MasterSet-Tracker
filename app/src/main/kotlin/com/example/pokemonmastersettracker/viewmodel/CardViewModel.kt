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

    fun searchPokemonCards(pokemonName: String, language: String = "en") {
        viewModelScope.launch {
            _cardUiState.value = CardUiState(loading = true)
            try {
                android.util.Log.d("CardViewModel", "Searching for: $pokemonName")
                val cards = repository.searchPokemonCards(pokemonName, language)
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
                
                _cardUiState.value = CardUiState(pokemonList = pokemonList, selectedPokemonName = null)
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
        viewModelScope.launch {
            _cardUiState.value = CardUiState(loading = true, selectedPokemonName = pokemonName)
            try {
                android.util.Log.d("CardViewModel", "Loading cards for: $pokemonName")
                val cards = repository.searchPokemonCards(pokemonName, "en")
                android.util.Log.d("CardViewModel", "Loaded ${cards.size} cards for $pokemonName")
                _cardUiState.value = CardUiState(cards = cards, selectedPokemonName = pokemonName)
            } catch (e: Exception) {
                android.util.Log.e("CardViewModel", "Error loading cards: ${e.message}", e)
                _cardUiState.value = CardUiState(error = e.message ?: "Unknown error")
            }
        }
    }

    fun clearSelection() {
        _cardUiState.value = CardUiState()
    }
                val cards = repository.searchPokemonCards(pokemonName)
                _cardUiState.value = CardUiState(cards = cards, selectedPokemonName = pokemonName)
            } catch (e: Exception) {
                _cardUiState.value = CardUiState(error = e.message ?: "Unknown error", selectedPokemonName = pokemonName)
            }
        }
    }

    fun selectCard(card: Card) {
        _selectedCard.value = card
    }

    fun clearSelection() {
        _selectedCard.value = null
        _cardUiState.value = CardUiState(selectedPokemonName = null)
    }
}
