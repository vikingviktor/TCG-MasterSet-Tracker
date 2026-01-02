package com.example.pokemonmastersettracker.data.repository

import com.example.pokemonmastersettracker.data.api.PokemonTCGApi
import com.example.pokemonmastersettracker.data.database.CardDao
import com.example.pokemonmastersettracker.data.database.UserCardDao
import com.example.pokemonmastersettracker.data.database.FavoritePokemonDao
import com.example.pokemonmastersettracker.data.database.UserDao
import com.example.pokemonmastersettracker.data.database.PokemonDao
import com.example.pokemonmastersettracker.data.models.Card
import com.example.pokemonmastersettracker.data.models.UserCard
import com.example.pokemonmastersettracker.data.models.FavoritePokemon
import com.example.pokemonmastersettracker.data.models.User
import com.example.pokemonmastersettracker.data.models.Pokemon
import com.example.pokemonmastersettracker.data.models.CardCondition
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PokemonRepository @Inject constructor(
    private val api: PokemonTCGApi,
    private val cardDao: CardDao,
    private val userCardDao: UserCardDao,
    private val favoritePokemonDao: FavoritePokemonDao,
    private val userDao: UserDao,
    private val pokemonDao: PokemonDao
) {
    
    // Pokemon Operations
    
    suspend fun searchPokemonLocal(query: String): List<Pokemon> {
        return pokemonDao.searchPokemon(query)
    }
    
    suspend fun getAllPokemon(): List<Pokemon> {
        return pokemonDao.getAllPokemon()
    }
    
    suspend fun getFavoritePokemon(): List<Pokemon> {
        return pokemonDao.getFavoritePokemon()
    }
    
    suspend fun toggleFavorite(pokemonName: String) {
        val pokemon = pokemonDao.searchPokemon(pokemonName).firstOrNull()
        pokemon?.let {
            pokemonDao.updateFavoriteStatus(pokemonName, !it.isFavorite)
        }
    }
    
    suspend fun seedPopularPokemon() {
        val count = pokemonDao.getPokemonCount()
        if (count == 0) {
            android.util.Log.d("PokemonRepository", "Seeding popular Pokemon...")
            val popularPokemon = getPopularPokemonList()
            pokemonDao.insertPokemons(popularPokemon)
            android.util.Log.d("PokemonRepository", "Seeded ${popularPokemon.size} Pokemon")
        }
    }
    
    // Card Operations
    
    suspend fun searchPokemonCards(pokemonName: String, language: String = "en"): List<Card> {
        return try {
            val startTime = System.currentTimeMillis()
            
            // First, check database for cached results
            val localCards = cardDao.getCardsByPokemonNameSync("%$pokemonName%")
            
            // If we have local results, return them immediately for better UX
            if (localCards.isNotEmpty()) {
                val cacheTime = System.currentTimeMillis() - startTime
                android.util.Log.d("PokemonRepository", "✅ CACHE HIT: ${localCards.size} cards in ${cacheTime}ms for: $pokemonName")
                return localCards
            }
            
            // No cache, fetch from API
            val query = buildCardQuery(pokemonName, language)
            android.util.Log.d("PokemonRepository", "🌐 API REQUEST: Starting search with query: $query")
            val apiStartTime = System.currentTimeMillis()
            
            val response = api.searchCards(query = query, pageSize = 25)
            
            val apiTime = System.currentTimeMillis() - apiStartTime
            android.util.Log.d("PokemonRepository", "⏱️ API RESPONSE: ${response.cards.size} cards in ${apiTime}ms")
            
            val dbStartTime = System.currentTimeMillis()
            cardDao.insertCards(response.cards)
            val dbTime = System.currentTimeMillis() - dbStartTime
            
            val totalTime = System.currentTimeMillis() - startTime
            android.util.Log.d("PokemonRepository", "📊 TIMING: Total=${totalTime}ms (API=${apiTime}ms, DB=${dbTime}ms)")
            
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
        // Use wildcard matching for better performance
        // name:"pikachu*" matches Pikachu, Pikachu-EX, Pikachu V, etc.
        return "name:\"${pokemonName}*\""
    }
    
    private fun getPopularPokemonList(): List<Pokemon> {
        return listOf(
            "Bulbasaur", "Ivysaur", "Venusaur", "Charmander", "Charmeleon", "Charizard",
            "Squirtle", "Wartortle", "Blastoise", "Pikachu", "Raichu", "Mewtwo", "Mew",
            "Chikorita", "Cyndaquil", "Totodile", "Lugia", "Ho-Oh", "Celebi",
            "Treecko", "Torchic", "Mudkip", "Rayquaza", "Kyogre", "Groudon", "Jirachi", "Deoxys",
            "Turtwig", "Chimchar", "Piplup", "Dialga", "Palkia", "Giratina", "Darkrai", "Arceus",
            "Snivy", "Tepig", "Oshawott", "Reshiram", "Zekrom", "Kyurem", "Keldeo", "Genesect",
            "Chespin", "Fennekin", "Froakie", "Xerneas", "Yveltal", "Zygarde", "Diancie",
            "Rowlet", "Litten", "Popplio", "Solgaleo", "Lunala", "Necrozma", "Marshadow",
            "Grookey", "Scorbunny", "Sobble", "Zacian", "Zamazenta", "Eternatus",
            "Sprigatito", "Fuecoco", "Quaxly", "Koraidon", "Miraidon",
            "Eevee", "Vaporeon", "Jolteon", "Flareon", "Espeon", "Umbreon", "Leafeon", "Glaceon", "Sylveon",
            "Snorlax", "Dragonite", "Gengar", "Alakazam", "Machamp", "Gyarados", "Lapras",
            "Articuno", "Zapdos", "Moltres", "Aerodactyl", "Scyther", "Scizor",
            "Tyranitar", "Ampharos", "Heracross", "Houndoom", "Steelix", "Kingdra",
            "Blaziken", "Gardevoir", "Aggron", "Salamence", "Metagross", "Latias", "Latios",
            "Lucario", "Garchomp", "Electivire", "Magmortar", "Togekiss", "Mamoswine", "Gallade",
            "Serperior", "Emboar", "Samurott", "Excadrill", "Zoroark", "Chandelure", "Haxorus", "Volcarona",
            "Greninja", "Talonflame", "Aegislash", "Tyrantrum", "Goodra", "Noivern",
            "Decidueye", "Incineroar", "Primarina", "Lycanroc", "Mimikyu", "Tapu Koko", "Tapu Lele",
            "Rillaboom", "Cinderace", "Inteleon", "Corviknight", "Toxtricity", "Dragapult",
            "Meowscarada", "Skeledirge", "Quaquaval", "Tinkaton", "Gholdengo"
        ).map { Pokemon(name = it) }
    }
}
