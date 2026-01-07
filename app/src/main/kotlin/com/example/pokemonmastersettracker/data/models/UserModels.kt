package com.example.pokemonmastersettracker.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val email: String,
    val username: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_cards")
data class UserCard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val cardId: String,
    val isOwned: Boolean = false,
    val condition: CardCondition = CardCondition.NEAR_MINT,
    val isGraded: Boolean = false,
    val gradingCompany: String? = null,
    val grade: String? = null,
    val purchasePrice: Double? = null,
    val currentPrice: Double? = null,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_pokemon")
data class FavoritePokemon(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val pokemonName: String,
    val addedAt: Long = System.currentTimeMillis(),
    val totalCards: Int = 0 // Cached total card count for this Pokemon
)

@Entity(tableName = "wishlist_cards")
data class WishlistCard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val cardId: String,
    val addedAt: Long = System.currentTimeMillis()
)

enum class CardCondition {
    MINT,
    NEAR_MINT,
    EXCELLENT,
    GOOD,
    LIGHT_PLAYED,
    PLAYED,
    POOR
}

data class PokemonSetCompletion(
    val pokemonName: String,
    val totalCards: Int,
    val ownedCards: Int,
    val completionPercentage: Float = (ownedCards.toFloat() / totalCards.toFloat()) * 100
)
