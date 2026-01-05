package com.example.pokemonmastersettracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokemonmastersettracker.data.models.Card
import com.example.pokemonmastersettracker.data.models.User
import com.example.pokemonmastersettracker.data.models.UserCard
import com.example.pokemonmastersettracker.data.models.CardCondition
import com.example.pokemonmastersettracker.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserCollectionUiState(
    val userCards: List<UserCard> = emptyList(),
    val userCardsWithDetails: List<Pair<UserCard, Card?>> = emptyList(),
    val ownedCount: Int = 0,
    val totalCount: Int = 0,
    val completionPercentage: Float = 0f,
    val loading: Boolean = false,
    val error: String? = null
)

data class WishlistUiState(
    val wishlistCards: List<Card> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class UserCollectionViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    private val _collectionUiState = MutableStateFlow(UserCollectionUiState())
    val collectionUiState: StateFlow<UserCollectionUiState> = _collectionUiState.asStateFlow()

    private val _wishlistUiState = MutableStateFlow(WishlistUiState())
    val wishlistUiState: StateFlow<WishlistUiState> = _wishlistUiState.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)

    fun setCurrentUser(userId: String) {
        _currentUserId.value = userId
        loadUserCollection(userId)
    }

    private fun loadUserCollection(userId: String) {
        viewModelScope.launch {
            _collectionUiState.value = _collectionUiState.value.copy(loading = true)
            
            repository.getUserCards(userId).collect { userCards ->
                val ownedCards = userCards.filter { it.isOwned }.size
                
                // Get card details for all user cards
                val allCardsWithDetails = repository.getUserCardsWithDetails(userId)
                
                // Filter to only show owned cards in the collection list
                val ownedCardsWithDetails = allCardsWithDetails.filter { it.first.isOwned }
                
                // Get total count of cards for all favorite Pokemon
                val totalCardsForFavorites = repository.getTotalCardsCountForFavoritePokemon(userId)
                
                val completionPercentage = if (totalCardsForFavorites > 0) {
                    (ownedCards.toFloat() / totalCardsForFavorites) * 100
                } else {
                    0f
                }

                _collectionUiState.value = UserCollectionUiState(
                    userCards = userCards,
                    userCardsWithDetails = ownedCardsWithDetails,
                    ownedCount = ownedCards,
                    totalCount = totalCardsForFavorites,
                    completionPercentage = completionPercentage,
                    loading = false
                )
            }
        }
    }

    fun loadWishlist(userId: String) {
        viewModelScope.launch {
            _wishlistUiState.value = _wishlistUiState.value.copy(loading = true)
            try {
                val wishlistCards = repository.getWishlistCardsWithDetails(userId)
                _wishlistUiState.value = WishlistUiState(
                    wishlistCards = wishlistCards,
                    loading = false
                )
            } catch (e: Exception) {
                _wishlistUiState.value = WishlistUiState(
                    loading = false,
                    error = e.message
                )
            }
        }
    }

    fun markCardAsOwned(cardId: String, condition: CardCondition = CardCondition.NEAR_MINT) {
        viewModelScope.launch {
            _currentUserId.value?.let { userId ->
                repository.markCardAsOwned(userId, cardId, condition)
            }
        }
    }

    fun markCardAsMissing(cardId: String) {
        viewModelScope.launch {
            _currentUserId.value?.let { userId ->
                repository.markCardAsMissing(userId, cardId)
            }
        }
    }

    fun addCardToCollection(
        cardId: String,
        isOwned: Boolean,
        condition: CardCondition = CardCondition.NEAR_MINT,
        isGraded: Boolean = false,
        gradingCompany: String? = null,
        grade: String? = null,
        purchasePrice: Double? = null
    ) {
        viewModelScope.launch {
            _currentUserId.value?.let { userId ->
                repository.addCardToCollection(
                    userId = userId,
                    cardId = cardId,
                    isOwned = isOwned,
                    condition = condition,
                    isGraded = isGraded,
                    gradingCompany = gradingCompany,
                    grade = grade,
                    purchasePrice = purchasePrice
                )
            }
        }
    }

    fun updateCardDetails(userCard: UserCard) {
        viewModelScope.launch {
            repository.updateUserCard(userCard)
        }
    }
}
