package com.example.pokemonmastersettracker.data.repository

import com.example.pokemonmastersettracker.data.api.PokemonTCGApi
import com.example.pokemonmastersettracker.data.database.CardDao
import com.example.pokemonmastersettracker.data.database.UserCardDao
import com.example.pokemonmastersettracker.data.database.FavoritePokemonDao
import com.example.pokemonmastersettracker.data.database.UserDao
import com.example.pokemonmastersettracker.data.models.Card
import com.example.pokemonmastersettracker.data.models.UserCard
import com.example.pokemonmastersettracker.data.models.FavoritePokemon
import com.example.pokemonmastersettracker.data.models.User
import com.example.pokemonmastersettracker.data.models.CardCondition
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PokemonRepository @Inject constructor(
    private val api: PokemonTCGApi,
    private val cardDao: CardDao,
    private val userCardDao: UserCardDao,
    private val favoritePokemonDao: FavoritePokemonDao,
    private val userDao: UserDao
) {
    
    // Card Operations
    
    suspend fun searchPokemonCards(pokemonName: String, language: String = "en"): List<Card> {
        return try {
            // First, check database for cached results
            val localCards = cardDao.getCardsByPokemonNameSync("%$pokemonName%")
            
            // If we have local results, return them immediately for better UX
            if (localCards.isNotEmpty()) {
                android.util.Log.d("PokemonRepository", "Found ${localCards.size} cards in cache for: $pokemonName")
                // Still fetch fresh data in background but return cached first
                return localCards
            }
            
            // No cache, fetch from API
            val query = buildCardQuery(pokemonName, language)
            android.util.Log.d("PokemonRepository", "Searching API with query: $query")
            val response = api.searchCards(query = query, pageSize = 25) // Reduced page size for faster response
            android.util.Log.d("PokemonRepository", "API response: ${response.cards.size} cards found")
            cardDao.insertCards(response.cards)
            response.cards
        } catch (e: Exception) {
            // On error, try to return cached data as fallback
            val cachedCards = cardDao.getCardsByPokemonNameSync("%$pokemonName%")
            if (cachedCards.isNotEmpty()) {
                android.util.Log.w("PokemonRepository", "API failed, using ${cachedCards.size} cached cards")
                return cachedCards
            }
            
            val errorDetails = """
                Error Type: ${e.javaClass.simpleName}
                Message: ${e.message}
                Cause: ${e.cause?.message}
                Stack Trace:
                ${e.stackTraceToString()}
            """.trimIndent()
            android.util.Log.e("PokemonRepository", "Error searching cards:\n$errorDetails")
            throw Exception("${e.javaClass.simpleName}: ${e.message}\nAt: ${e.stackTrace.firstOrNull()}", e)
        }
    }

    suspend fun getCardsByPokemonAndSet(pokemonName: String, setId: String): List<Card> {
        return try {
            val query = "name:$pokemonName set.id:$setId"
            val response = api.searchCards(query = query)
            cardDao.insertCards(response.cards)
            response.cards
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getLocalCardsByPokemonName(pokemonName: String): Flow<List<Card>> {
        return cardDao.getCardsByPokemonName(pokemonName)
    }

    fun getAllLocalCards(): Flow<List<Card>> {
        return cardDao.getAllCards()
    }

    suspend fun getCardById(cardId: String): Card? {
        return try {
            val response = api.getCardById(cardId)
            response.cards.firstOrNull()?.let { card ->
                cardDao.insertCard(card)
                card
            }
        } catch (e: Exception) {
            cardDao.getCardById(cardId)
        }
    }

    // User Card Operations

    suspend fun addCardToCollection(
        userId: String,
        cardId: String,
        isOwned: Boolean = false,
        condition: CardCondition = CardCondition.NEAR_MINT,
        isGraded: Boolean = false,
        gradingCompany: String? = null,
        grade: String? = null,
        purchasePrice: Double? = null
    ) {
        val userCard = UserCard(
            userId = userId,
            cardId = cardId,
            isOwned = isOwned,
            condition = condition,
            isGraded = isGraded,
            gradingCompany = gradingCompany,
            grade = grade,
            purchasePrice = purchasePrice
        )
        userCardDao.insertUserCard(userCard)
    }

    suspend fun updateUserCard(userCard: UserCard) {
        userCardDao.updateUserCard(userCard)
    }

    suspend fun markCardAsOwned(userId: String, cardId: String, condition: CardCondition = CardCondition.NEAR_MINT) {
        val userCard = userCardDao.getUserCard(userId, cardId) ?: run {
            UserCard(userId = userId, cardId = cardId, isOwned = true, condition = condition)
        }
        userCardDao.updateUserCard(userCard.copy(isOwned = true, condition = condition))
    }

    suspend fun markCardAsMissing(userId: String, cardId: String) {
        val userCard = userCardDao.getUserCard(userId, cardId)
        userCard?.let {
            userCardDao.updateUserCard(it.copy(isOwned = false))
        }
    }

    fun getUserCards(userId: String): Flow<List<UserCard>> {
        return userCardDao.getUserCards(userId)
    }

    fun getUserOwnedCards(userId: String): Flow<List<UserCard>> {
        return userCardDao.getUserOwnedCards(userId)
    }

    suspend fun getOwnedCardsCount(userId: String): Int {
        return userCardDao.getOwnedCardsCount(userId)
    }

    // Favorite Pokemon Operations

    suspend fun addFavoritePokemon(userId: String, pokemonName: String) {
        favoritePokemonDao.addFavorite(
            FavoritePokemon(
                userId = userId,
                pokemonName = pokemonName
            )
        )
    }

    suspend fun removeFavoritePokemon(userId: String, pokemonName: String) {
        favoritePokemonDao.removeFavorite(userId, pokemonName)
    }

    fun getUserFavoritePokemon(userId: String): Flow<List<FavoritePokemon>> {
        return favoritePokemonDao.getUserFavorites(userId)
    }

    suspend fun isFavoritePokemon(userId: String, pokemonName: String): Boolean {
        return favoritePokemonDao.isFavorite(userId, pokemonName) > 0
    }

    // User Operations

    suspend fun createUser(email: String, username: String): User {
        val user = User(
            id = java.util.UUID.randomUUID().toString(),
            email = email,
            username = username
        )
        userDao.insertUser(user)
        return user
    }

    suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    // Helper functions

    private fun buildCardQuery(pokemonName: String, language: String): String {
        // Use exact or partial name matching
        return "name:$pokemonName"
    }
}
