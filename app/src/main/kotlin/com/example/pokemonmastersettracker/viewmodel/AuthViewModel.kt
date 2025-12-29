package com.example.pokemonmastersettracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokemonmastersettracker.data.models.User
import com.example.pokemonmastersettracker.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val currentUser: User? = null,
    val loading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    fun register(email: String, username: String) {
        viewModelScope.launch {
            _authUiState.value = AuthUiState(loading = true)
            try {
                val user = repository.createUser(email, username)
                _authUiState.value = AuthUiState(
                    isLoggedIn = true,
                    currentUser = user
                )
            } catch (e: Exception) {
                _authUiState.value = AuthUiState(error = e.message ?: "Registration failed")
            }
        }
    }

    fun login(email: String) {
        viewModelScope.launch {
            _authUiState.value = AuthUiState(loading = true)
            try {
                val user = repository.getUserByEmail(email)
                if (user != null) {
                    _authUiState.value = AuthUiState(
                        isLoggedIn = true,
                        currentUser = user
                    )
                } else {
                    _authUiState.value = AuthUiState(error = "User not found")
                }
            } catch (e: Exception) {
                _authUiState.value = AuthUiState(error = e.message ?: "Login failed")
            }
        }
    }

    fun logout() {
        _authUiState.value = AuthUiState(isLoggedIn = false)
    }

    fun clearError() {
        _authUiState.value = _authUiState.value.copy(error = null)
    }
}
