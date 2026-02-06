package com.example.pokemonmastersettracker.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokemonmastersettracker.data.models.Card
import com.example.pokemonmastersettracker.data.models.User
import com.example.pokemonmastersettracker.data.models.UserCard
import com.example.pokemonmastersettracker.data.models.CardCondition
import com.example.pokemonmastersettracker.data.repository.PokemonRepository
import com.example.pokemonmastersettracker.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserCollectionUiState(
    val userCards: List<UserCard> = emptyList(),
    val userCardsWithDetails: List<Pair<UserCard, Card?>> = emptyList(),
    val ownedCount: Int = 0,
    val totalCount: Int = 0,
    val completionPercentage: Float = 0f,
    val totalValue: Double = 0.0,
    val loading: Boolean = false,
    val error: String? = null,
    val countVariants: Boolean = false // Whether to count variants in totals
)

data class WishlistUiState(
    val wishlistCards: List<Card> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class UserCollectionViewModel @Inject constructor(
    private val repository: PokemonRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val userPreferences = UserPreferences(context)
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
            
            combine(
                repository.getUserCards(userId),
                userPreferences.countVariantsInCollection
            ) { userCards, countVariants ->
                Pair(userCards, countVariants)
            }.collect { (userCards, countVariants) ->
                // Get card details for all user cards
                val allCardsWithDetails = repository.getUserCardsWithDetails(userId)
                
                // Filter to only show owned cards in the collection list
                val ownedCardsWithDetails = allCardsWithDetails.filter { it.first.isOwned }
                
                val ownedCount: Int
                val totalCount: Int
                
                if (countVariants) {
                    // Count all variant entries
                    ownedCount = userCards.filter { it.isOwned }.size
                    // Total = all possible variants for all cards in master set
                    totalCount = repository.getTotalVariantsCountForFavoritePokemon(userId)
                } else {
                    // Count unique cards only (ignore variants)
                    ownedCount = userCards.filter { it.isOwned }
                        .distinctBy { it.cardId }
                        .size
                    // Total = unique cards in master set
                    totalCount = repository.getTotalCardsCountForFavoritePokemon(userId)
                }
                
                val completionPercentage = if (totalCount > 0) {
                    (ownedCount.toFloat() / totalCount) * 100
                } else {
                    0f
                }
                
                // Calculate total collection value based on average Cardmarket prices
                val totalValue = ownedCardsWithDetails.sumOf { (_, card) ->
                    card?.cardmarket?.avg ?: 0.0
                }

                _collectionUiState.value = UserCollectionUiState(
                    userCards = userCards,
                    userCardsWithDetails = ownedCardsWithDetails,
                    ownedCount = ownedCount,
                    totalCount = totalCount,
                    completionPercentage = completionPercentage,
                    totalValue = totalValue,
                    loading = false,
                    countVariants = countVariants
                )
            }
        }
    }
    
    fun toggleVariantCounting() {
        viewModelScope.launch {
            val currentSetting = _collectionUiState.value.countVariants
            userPreferences.setCountVariantsInCollection(!currentSetting)
            // Collection will auto-reload via flow
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
        variant: String? = null,
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
                    variant = variant,
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
