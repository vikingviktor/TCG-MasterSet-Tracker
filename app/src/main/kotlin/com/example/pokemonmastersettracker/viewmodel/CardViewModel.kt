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

data class CardUiState(
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

    fun searchPokemonCards(pokemonName: String) {
        viewModelScope.launch {
            _cardUiState.value = CardUiState(loading = true)
            try {
                android.util.Log.d("CardViewModel", "Searching for: $pokemonName")
                val cards = repository.searchPokemonCards(pokemonName)
                android.util.Log.d("CardViewModel", "Got ${cards.size} cards")
                _cardUiState.value = CardUiState(cards = cards, selectedPokemonName = null)
            } catch (e: Exception) {
                android.util.Log.e("CardViewModel", "Search error: ${e.message}", e)
                _cardUiState.value = CardUiState(error = "Error: ${e.message ?: "Unknown error"}")
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
