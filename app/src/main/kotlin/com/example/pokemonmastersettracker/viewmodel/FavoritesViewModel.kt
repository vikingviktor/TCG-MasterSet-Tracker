package com.example.pokemonmastersettracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokemonmastersettracker.data.models.FavoritePokemon
import com.example.pokemonmastersettracker.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val favorites: List<FavoritePokemon> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    private val _favoritesUiState = MutableStateFlow(FavoritesUiState())
    val favoritesUiState: StateFlow<FavoritesUiState> = _favoritesUiState.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)

    fun setCurrentUser(userId: String) {
        _currentUserId.value = userId
        loadFavorites(userId)
    }

    private fun loadFavorites(userId: String) {
        viewModelScope.launch {
            repository.getUserFavoritePokemon(userId).collect { favorites ->
                _favoritesUiState.value = FavoritesUiState(favorites = favorites)
            }
        }
    }

    fun addFavoritePokemon(pokemonName: String) {
        viewModelScope.launch {
            _currentUserId.value?.let { userId ->
                repository.addFavoritePokemon(userId, pokemonName)
            }
        }
    }

    fun removeFavoritePokemon(pokemonName: String) {
        viewModelScope.launch {
            _currentUserId.value?.let { userId ->
                repository.removeFavoritePokemon(userId, pokemonName)
            }
        }
    }

    suspend fun isFavoritePokemon(pokemonName: String): Boolean {
        return _currentUserId.value?.let { userId ->
            repository.isFavoritePokemon(userId, pokemonName)
        } ?: false
    }
}
