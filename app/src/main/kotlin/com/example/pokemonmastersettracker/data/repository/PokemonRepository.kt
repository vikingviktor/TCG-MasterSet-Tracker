package com.example.pokemonmastersettracker.data.repository

import com.example.pokemonmastersettracker.data.api.PokemonTCGApi
import com.example.pokemonmastersettracker.data.database.CardDao
import com.example.pokemonmastersettracker.data.database.UserCardDao
import com.example.pokemonmastersettracker.data.database.FavoritePokemonDao
import com.example.pokemonmastersettracker.data.database.WishlistCardDao
import com.example.pokemonmastersettracker.data.database.UserDao
import com.example.pokemonmastersettracker.data.database.PokemonDao
import com.example.pokemonmastersettracker.data.models.Card
import com.example.pokemonmastersettracker.data.models.UserCard
import com.example.pokemonmastersettracker.data.models.FavoritePokemon
import com.example.pokemonmastersettracker.data.models.WishlistCard
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
    private val wishlistCardDao: WishlistCardDao,
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
    
    suspend fun updatePokemonImage(pokemonName: String, imageUrl: String) {
        pokemonDao.updatePokemonImage(pokemonName, imageUrl)
    }
    
    suspend fun seedPopularPokemon() {
        val count = pokemonDao.getPokemonCount()
        if (count == 0) {
            android.util.Log.d("PokemonRepository", "Seeding popular Pokemon with Pokedex numbers...")
            val popularPokemon = getPopularPokemonList()
            pokemonDao.insertPokemons(popularPokemon)
            android.util.Log.d("PokemonRepository", "✓ Seeded ${popularPokemon.size} Pokemon (Gen 1-3 with Pokedex #1-386)")
        } else {
            android.util.Log.d("PokemonRepository", "Pokemon already seeded (count: $count)")
        }
    }
    
    // Pre-fetch card data for most popular Pokemon to improve performance
    // Returns Triple(cachedCount, successCount, failedCount)
    // onProgress callback: (current, total, pokemonName, cached, success, failed) -> Unit
    suspend fun preFetchPopularPokemonCards(
        onProgress: ((Int, Int, String, Int, Int, Int) -> Unit)? = null
    ): Triple<Int, Int, Int> {
        val popularPokemon = listOf(
            "Pikachu", "Charizard", "Mewtwo", "Mew", "Eevee",
            "Bulbasaur", "Charmander", "Squirtle", "Gengar", "Dragonite",
            "Gyarados", "Snorlax", "Lucario", "Greninja", "Garchomp",
            "Rayquaza", "Lugia", "Ho-Oh", "Blastoise", "Venusaur",
            "Umbreon", "Espeon", "Jolteon", "Vaporeon", "Flareon",
            "Scyther", "Kabutops", "Nidoking", "Haunter", "Haxorus", "Scizor"
        )
        
        android.util.Log.d("PokemonRepository", "🎯 Starting pre-fetch of ${popularPokemon.size} popular Pokemon with retry logic...")
        android.util.Log.d("PokemonRepository", "💡 This may take a while due to API issues - will retry on failures!")
        var successCount = 0
        var cachedCount = 0
        var failedCount = 0
        
        for ((index, pokemonName) in popularPokemon.withIndex()) {
            // Emit progress update
            onProgress?.invoke(index + 1, popularPokemon.size, pokemonName, cachedCount, successCount, failedCount)
            
            // Check if already cached
            val cached = cardDao.getCardsByPokemonNameSync("%$pokemonName%")
            if (cached.isNotEmpty()) {
                cachedCount++
                android.util.Log.d("PokemonRepository", "  ✓ $pokemonName: Already cached (${cached.size} cards)")
                continue
            }
            
            // Fetch with retry logic
            var retryCount = 0
            val maxRetries = 5
            var success = false
            
            while (retryCount < maxRetries && !success) {
                try {
                    val delayMs = if (retryCount == 0) 2000L else (2000L * (retryCount + 1)) // 2s, 4s, 6s, 8s, 10s
                    if (retryCount > 0) {
                        android.util.Log.d("PokemonRepository", "  🔄 $pokemonName: Retry $retryCount/$maxRetries after ${delayMs}ms delay...")
                        kotlinx.coroutines.delay(delayMs)
                    } else {
                        android.util.Log.d("PokemonRepository", "  📥 Fetching $pokemonName...")
                    }
                    
                    val cards = searchPokemonCardsWithPagination(pokemonName, "en", 1, 250, forceRefresh = true)
                    
                    if (cards.isNotEmpty()) {
                        successCount++
                        success = true
                        android.util.Log.d("PokemonRepository", "  ✅ $pokemonName: SUCCESS! Fetched ${cards.size} cards")
                    } else {
                        android.util.Log.w("PokemonRepository", "  ⚠️ $pokemonName: API returned 0 cards, will retry...")
                        retryCount++
                    }
                } catch (e: Exception) {
                    retryCount++
                    val errorType = when {
                        e.message?.contains("404") == true -> "404 Not Found"
                        e.message?.contains("504") == true -> "504 Gateway Timeout"
                        e.message?.contains("timeout") == true -> "Timeout"
                        else -> e.javaClass.simpleName
                    }
                    
                    if (retryCount < maxRetries) {
                        android.util.Log.w("PokemonRepository", "  ⚠️ $pokemonName: $errorType - will retry ($retryCount/$maxRetries)")
                    } else {
                        android.util.Log.e("PokemonRepository", "  ❌ $pokemonName: FAILED after $maxRetries retries ($errorType)")
                    }
                }
            }
            
            if (!success) {
                failedCount++
            }
        }
        
        android.util.Log.d("PokemonRepository", "📊 Pre-fetch complete: $cachedCount already cached, $successCount fetched, $failedCount failed permanently")
        return Triple(cachedCount, successCount, failedCount)
    }
    
    // Card Operations
    
    suspend fun searchPokemonCardsWithPagination(pokemonName: String, languages: Set<String> = setOf("en", "ja"), page: Int = 1, pageSize: Int = 250, forceRefresh: Boolean = false): List<Card> {
        // For first page, check cache first unless force refresh
        if (page == 1 && !forceRefresh) {
            val cachedCards = cardDao.getCardsByPokemonNameSync("%$pokemonName%")
            if (cachedCards.isNotEmpty()) {
                // Filter cached cards by language
                val filteredCards = filterCardsByLanguage(cachedCards, languages)
                android.util.Log.d("PokemonRepository", "✅ Using ${filteredCards.size}/${cachedCards.size} cached cards for '$pokemonName' (languages: $languages)")
                // Return paginated subset from cache
                val startIndex = 0
                val endIndex = minOf(pageSize, filteredCards.size)
                return filteredCards.subList(startIndex, endIndex)
            }
        }
        
        val query = buildCardQuery(pokemonName)
        return try {
            android.util.Log.d("PokemonRepository", "🌐 API REQUEST: pokemonName='$pokemonName', query='$query', languages=$languages, page=$page, pageSize=$pageSize")
            val apiStartTime = System.currentTimeMillis()
            
            val response = api.searchCards(query = query, pageSize = pageSize, page = page)
            
            val apiTime = System.currentTimeMillis() - apiStartTime
            android.util.Log.d("PokemonRepository", "⏱️ API RESPONSE: ${response.cards.size} cards in ${apiTime}ms")
            android.util.Log.d("PokemonRepository", "📊 Response details: page=${response.page}, pageSize=${response.pageSize}, count=${response.count}, totalCount=${response.totalCount}")
            
            // Filter cards by language before caching
            val filteredCards = filterCardsByLanguage(response.cards, languages)
            android.util.Log.d("PokemonRepository", "🔍 Language filter: ${response.cards.size} -> ${filteredCards.size} cards (languages: $languages)")
            
            cardDao.insertCards(response.cards) // Cache all cards (unfiltered)
            android.util.Log.d("PokemonRepository", "✓ Cards saved to database")
            filteredCards // Return filtered cards
        } catch (e: Exception) {
            android.util.Log.e("PokemonRepository", "❌ API ERROR: ${e.javaClass.simpleName}: ${e.message}")
            
            // ALWAYS try to get cached results on error
            val cachedCards = cardDao.getCardsByPokemonNameSync("%$pokemonName%")
            if (cachedCards.isNotEmpty()) {
                val filteredCards = filterCardsByLanguage(cachedCards, languages)
                android.util.Log.w("PokemonRepository", "⚠️ API failed for '$pokemonName', using ${filteredCards.size}/${cachedCards.size} cached cards (languages: $languages)")
                // Return paginated subset from cache
                val startIndex = (page - 1) * pageSize
                val endIndex = minOf(startIndex + pageSize, filteredCards.size)
                if (startIndex < filteredCards.size) {
                    return filteredCards.subList(startIndex, endIndex)
                }
                return emptyList()
            }
            
            val errorDetails = """
                Pokemon Name: '$pokemonName'
                Query Used: '$query'
                Error Type: ${e.javaClass.simpleName}
                Message: ${e.message}
                Cause: ${e.cause?.message}
            """.trimIndent()
            android.util.Log.e("PokemonRepository", "❌ Error searching cards:\n$errorDetails")
            throw Exception("${e.javaClass.simpleName}: ${e.message}\n\nPokemon: $pokemonName\nQuery: $query", e)
        }
    }
    
    suspend fun searchPokemonCards(pokemonName: String, languages: Set<String> = setOf("en", "ja")): List<Card> {
        return try {
            val startTime = System.currentTimeMillis()
            
            // First, check database for cached results
            val localCards = cardDao.getCardsByPokemonNameSync("%$pokemonName%")
            
            // If we have local results, return them immediately for better UX
            if (localCards.isNotEmpty()) {
                val filteredCards = filterCardsByLanguage(localCards, languages)
                val cacheTime = System.currentTimeMillis() - startTime
                android.util.Log.d("PokemonRepository", "✅ CACHE HIT: ${filteredCards.size}/${localCards.size} cards in ${cacheTime}ms for: $pokemonName (languages: $languages)")
                return filteredCards
            }
            
            // No cache, fetch from API
            val query = buildCardQuery(pokemonName)
            android.util.Log.d("PokemonRepository", "🌐 API REQUEST: Starting search with query: $query")
            val apiStartTime = System.currentTimeMillis()
            
            val response = api.searchCards(query = query, pageSize = 250)
            
            val apiTime = System.currentTimeMillis() - apiStartTime
            android.util.Log.d("PokemonRepository", "⏱️ API RESPONSE: ${response.cards.size} cards in ${apiTime}ms")
            
            val filteredCards = filterCardsByLanguage(response.cards, languages)
            android.util.Log.d("PokemonRepository", "🔍 Language filter: ${response.cards.size} -> ${filteredCards.size} cards (languages: $languages)")
            
            val dbStartTime = System.currentTimeMillis()
            cardDao.insertCards(response.cards) // Cache all cards
            val dbTime = System.currentTimeMillis() - dbStartTime
            
            val totalTime = System.currentTimeMillis() - startTime
            android.util.Log.d("PokemonRepository", "📊 TIMING: Total=${totalTime}ms (API=${apiTime}ms, DB=${dbTime}ms)")
            
            filteredCards // Return filtered cards
        } catch (e: Exception) {
            // On error, try to return cached data as fallback
            val cachedCards = cardDao.getCardsByPokemonNameSync("%$pokemonName%")
            if (cachedCards.isNotEmpty()) {
                val filteredCards = filterCardsByLanguage(cachedCards, languages)
                android.util.Log.w("PokemonRepository", "API failed, using ${filteredCards.size}/${cachedCards.size} cached cards (languages: $languages)")
                return filteredCards
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
        val userCard = userCardDao.getUserCard(userId, cardId)
        if (userCard == null) {
            // Card doesn't exist in collection, insert it as owned
            val newUserCard = UserCard(userId = userId, cardId = cardId, isOwned = true, condition = condition)
            userCardDao.insertUserCard(newUserCard)
        } else {
            // Card exists, update it
            userCardDao.updateUserCard(userCard.copy(isOwned = true, condition = condition))
        }
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
    
    suspend fun isCardOwned(userId: String, cardId: String): Boolean {
        val userCard = userCardDao.getUserCard(userId, cardId)
        return userCard?.isOwned == true
    }

    // Wishlist Operations
    
    suspend fun addToWishlist(userId: String, cardId: String) {
        wishlistCardDao.addToWishlist(
            WishlistCard(
                userId = userId,
                cardId = cardId
            )
        )
    }
    
    suspend fun removeFromWishlist(userId: String, cardId: String) {
        wishlistCardDao.removeFromWishlist(userId, cardId)
    }
    
    fun getUserWishlist(userId: String): Flow<List<WishlistCard>> {
        return wishlistCardDao.getUserWishlist(userId)
    }
    
    suspend fun getUserWishlistSync(userId: String): List<WishlistCard> {
        return wishlistCardDao.getUserWishlistSync(userId)
    }
    
    suspend fun isInWishlist(userId: String, cardId: String): Boolean {
        return wishlistCardDao.isInWishlist(userId, cardId) > 0
    }

    // Favorite Pokemon Operations

    suspend fun addFavoritePokemon(userId: String, pokemonName: String) {
        // First, add the favorite immediately so it shows up in the list
        favoritePokemonDao.addFavorite(
            FavoritePokemon(
                userId = userId,
                pokemonName = pokemonName,
                totalCards = 0 // Start with 0, will update
            )
        )
        
        // Then try to fetch total card count from API in the background
        val totalCards = try {
            // Use the same query building logic for consistency
            val query = buildCardQuery(pokemonName)
            // Use pageSize=1 to minimize data transfer, we only need the totalCount
            val response = api.searchCards(query = query, pageSize = 1)
            val count = response.totalCount // Use the totalCount field from API response
            android.util.Log.d("PokemonRepository", "Fetched total cards for $pokemonName: $count (query: $query)")
            count
        } catch (e: Exception) {
            android.util.Log.w("PokemonRepository", "Could not fetch total cards for $pokemonName (${e.message}), will use default")
            // Fallback: Use 0 so it doesn't show incorrect data
            0
        }
        
        // Update with the actual count
        if (totalCards > 0) {
            favoritePokemonDao.addFavorite(
                FavoritePokemon(
                    userId = userId,
                    pokemonName = pokemonName,
                    totalCards = totalCards
                )
            )
            android.util.Log.d("PokemonRepository", "Updated total cards for $pokemonName: $totalCards")
        }
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

    suspend fun getWishlistCardsWithDetails(userId: String): List<Card> {
        return try {
            val wishlistCardIds = wishlistCardDao.getUserWishlistSync(userId).map { it.cardId }
            wishlistCardIds.mapNotNull { cardId ->
                cardDao.getCardById(cardId)
            }
        } catch (e: Exception) {
            android.util.Log.e("PokemonRepository", "Error getting wishlist cards: ${e.message}")
            emptyList()
        }
    }

    suspend fun getUserCardsWithDetails(userId: String): List<Pair<UserCard, Card?>> {
        return try {
            val userCards = userCardDao.getUserCardsSync(userId)
            userCards.map { userCard ->
                val card = cardDao.getCardById(userCard.cardId)
                userCard to card
            }
        } catch (e: Exception) {
            android.util.Log.e("PokemonRepository", "Error getting user cards with details: ${e.message}")
            emptyList()
        }
    }

    suspend fun getTotalCardsCountForFavoritePokemon(userId: String): Int {
        return try {
            val favoritePokemon = favoritePokemonDao.getUserFavoritesSync(userId)
            
            // Use cached totalCards from database for faster performance
            val totalCount = favoritePokemon.sumOf { it.totalCards }
            
            android.util.Log.d("PokemonRepository", "Total cards for ${favoritePokemon.size} favorite Pokemon (cached): $totalCount")
            totalCount
        } catch (e: Exception) {
            android.util.Log.e("PokemonRepository", "Error getting total cards count: ${e.message}")
            0
        }
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

    private suspend fun buildCardQuery(pokemonName: String): String {
        // Try to get Pokedex number first for multi-language support
        val pokemon = pokemonDao.searchPokemon(pokemonName).firstOrNull()
        
        return if (pokemon?.nationalPokedexNumber != null) {
            // Use Pokedex number range syntax - works for all languages (Pikachu = 25, ピカチュウ = 25)
            // Per API docs: nationalPokedexNumbers:[25 TO 25]
            android.util.Log.d("PokemonRepository", "Using nationalPokedexNumbers:[${pokemon.nationalPokedexNumber} TO ${pokemon.nationalPokedexNumber}] for $pokemonName")
            "nationalPokedexNumbers:[${pokemon.nationalPokedexNumber} TO ${pokemon.nationalPokedexNumber}]"
        } else {
            // Fallback to name search
            val cleanName = pokemonName.trim()
            android.util.Log.d("PokemonRepository", "Using name search: $cleanName (no Pokedex number found)")
            "name:$cleanName"
        }
    }
    
    /**
     * Filter cards by selected languages.
     * English cards: Set is NOT Japanese (no Japanese characters, no "-jp" in set ID)
     * Japanese cards: Set IS Japanese (contains Japanese characters or "-jp" in set ID)
     */
    private fun filterCardsByLanguage(cards: List<Card>, languages: Set<String>): List<Card> {
        if (languages.isEmpty() || languages.containsAll(setOf("en", "ja"))) {
            // Both languages selected or no filter - return all cards
            return cards
        }
        
        return cards.filter { card ->
            when {
                languages.contains("ja") && languages.contains("en") -> true
                languages.contains("ja") -> card.set?.isJapanese == true
                languages.contains("en") -> card.set?.isJapanese != true
                else -> true // Default to showing all if unexpected language value
            }
        }
    }
            "name:$cleanName"
        }
    }
    
    private fun getPopularPokemonList(): List<Pokemon> {
        // Complete Pokedex mapping for all generations
        val pokemonData = mapOf(
            // Gen 1 (Kanto) - #1-151
            "Bulbasaur" to 1, "Ivysaur" to 2, "Venusaur" to 3,
            "Charmander" to 4, "Charmeleon" to 5, "Charizard" to 6,
            "Squirtle" to 7, "Wartortle" to 8, "Blastoise" to 9,
            "Caterpie" to 10, "Metapod" to 11, "Butterfree" to 12,
            "Weedle" to 13, "Kakuna" to 14, "Beedrill" to 15,
            "Pidgey" to 16, "Pidgeotto" to 17, "Pidgeot" to 18,
            "Rattata" to 19, "Raticate" to 20,
            "Spearow" to 21, "Fearow" to 22,
            "Ekans" to 23, "Arbok" to 24,
            "Pikachu" to 25, "Raichu" to 26,
            "Sandshrew" to 27, "Sandslash" to 28,
            "Nidoran♀" to 29, "Nidorina" to 30, "Nidoqueen" to 31,
            "Nidoran♂" to 32, "Nidorino" to 33, "Nidoking" to 34,
            "Clefairy" to 35, "Clefable" to 36,
            "Vulpix" to 37, "Ninetales" to 38,
            "Jigglypuff" to 39, "Wigglytuff" to 40,
            "Zubat" to 41, "Golbat" to 42,
            "Oddish" to 43, "Gloom" to 44, "Vileplume" to 45,
            "Paras" to 46, "Parasect" to 47,
            "Venonat" to 48, "Venomoth" to 49,
            "Diglett" to 50, "Dugtrio" to 51,
            "Meowth" to 52, "Persian" to 53,
            "Psyduck" to 54, "Golduck" to 55,
            "Mankey" to 56, "Primeape" to 57,
            "Growlithe" to 58, "Arcanine" to 59,
            "Poliwag" to 60, "Poliwhirl" to 61, "Poliwrath" to 62,
            "Abra" to 63, "Kadabra" to 64, "Alakazam" to 65,
            "Machop" to 66, "Machoke" to 67, "Machamp" to 68,
            "Bellsprout" to 69, "Weepinbell" to 70, "Victreebel" to 71,
            "Tentacool" to 72, "Tentacruel" to 73,
            "Geodude" to 74, "Graveler" to 75, "Golem" to 76,
            "Ponyta" to 77, "Rapidash" to 78,
            "Slowpoke" to 79, "Slowbro" to 80,
            "Magnemite" to 81, "Magneton" to 82,
            "Farfetch'd" to 83,
            "Doduo" to 84, "Dodrio" to 85,
            "Seel" to 86, "Dewgong" to 87,
            "Grimer" to 88, "Muk" to 89,
            "Shellder" to 90, "Cloyster" to 91,
            "Gastly" to 92, "Haunter" to 93, "Gengar" to 94,
            "Onix" to 95,
            "Drowzee" to 96, "Hypno" to 97,
            "Krabby" to 98, "Kingler" to 99,
            "Voltorb" to 100, "Electrode" to 101,
            "Exeggcute" to 102, "Exeggutor" to 103,
            "Cubone" to 104, "Marowak" to 105,
            "Hitmonlee" to 106, "Hitmonchan" to 107,
            "Lickitung" to 108,
            "Koffing" to 109, "Weezing" to 110,
            "Rhyhorn" to 111, "Rhydon" to 112,
            "Chansey" to 113,
            "Tangela" to 114,
            "Kangaskhan" to 115,
            "Horsea" to 116, "Seadra" to 117,
            "Goldeen" to 118, "Seaking" to 119,
            "Staryu" to 120, "Starmie" to 121,
            "Mr. Mime" to 122,
            "Scyther" to 123,
            "Jynx" to 124,
            "Electabuzz" to 125,
            "Magmar" to 126,
            "Pinsir" to 127,
            "Tauros" to 128,
            "Magikarp" to 129, "Gyarados" to 130,
            "Lapras" to 131,
            "Ditto" to 132,
            "Eevee" to 133, "Vaporeon" to 134, "Jolteon" to 135, "Flareon" to 136,
            "Porygon" to 137,
            "Omanyte" to 138, "Omastar" to 139,
            "Kabuto" to 140, "Kabutops" to 141,
            "Aerodactyl" to 142,
            "Snorlax" to 143,
            "Articuno" to 144, "Zapdos" to 145, "Moltres" to 146,
            "Dratini" to 147, "Dragonair" to 148, "Dragonite" to 149,
            "Mewtwo" to 150,
            "Mew" to 151,
            
            // Gen 2 (Johto) - #152-251
            "Chikorita" to 152, "Bayleef" to 153, "Meganium" to 154,
            "Cyndaquil" to 155, "Quilava" to 156, "Typhlosion" to 157,
            "Totodile" to 158, "Croconaw" to 159, "Feraligatr" to 160,
            "Sentret" to 161, "Furret" to 162,
            "Hoothoot" to 163, "Noctowl" to 164,
            "Ledyba" to 165, "Ledian" to 166,
            "Spinarak" to 167, "Ariados" to 168,
            "Crobat" to 169,
            "Chinchou" to 170, "Lanturn" to 171,
            "Pichu" to 172,
            "Cleffa" to 173,
            "Igglybuff" to 174,
            "Togepi" to 175, "Togetic" to 176,
            "Natu" to 177, "Xatu" to 178,
            "Mareep" to 179, "Flaaffy" to 180, "Ampharos" to 181,
            "Bellossom" to 182,
            "Marill" to 183, "Azumarill" to 184,
            "Sudowoodo" to 185,
            "Politoed" to 186,
            "Hoppip" to 187, "Skiploom" to 188, "Jumpluff" to 189,
            "Aipom" to 190,
            "Sunkern" to 191, "Sunflora" to 192,
            "Yanma" to 193,
            "Wooper" to 194, "Quagsire" to 195,
            "Espeon" to 196,
            "Umbreon" to 197,
            "Murkrow" to 198,
            "Slowking" to 199,
            "Misdreavus" to 200,
            "Unown" to 201,
            "Wobbuffet" to 202,
            "Girafarig" to 203,
            "Pineco" to 204, "Forretress" to 205,
            "Dunsparce" to 206,
            "Gligar" to 207,
            "Steelix" to 208,
            "Snubbull" to 209, "Granbull" to 210,
            "Qwilfish" to 211,
            "Scizor" to 212,
            "Shuckle" to 213,
            "Heracross" to 214,
            "Sneasel" to 215,
            "Teddiursa" to 216, "Ursaring" to 217,
            "Slugma" to 218, "Magcargo" to 219,
            "Swinub" to 220, "Piloswine" to 221,
            "Corsola" to 222,
            "Remoraid" to 223, "Octillery" to 224,
            "Delibird" to 225,
            "Mantine" to 226,
            "Skarmory" to 227,
            "Houndour" to 228, "Houndoom" to 229,
            "Kingdra" to 230,
            "Phanpy" to 231, "Donphan" to 232,
            "Porygon2" to 233,
            "Stantler" to 234,
            "Smeargle" to 235,
            "Tyrogue" to 236,
            "Hitmontop" to 237,
            "Smoochum" to 238,
            "Elekid" to 239,
            "Magby" to 240,
            "Miltank" to 241,
            "Blissey" to 242,
            "Raikou" to 243,
            "Entei" to 244,
            "Suicune" to 245,
            "Larvitar" to 246, "Pupitar" to 247, "Tyranitar" to 248,
            "Lugia" to 249,
            "Ho-Oh" to 250,
            "Celebi" to 251,
            
            // Gen 3 (Hoenn) - #252-386
            "Treecko" to 252, "Grovyle" to 253, "Sceptile" to 254,
            "Torchic" to 255, "Combusken" to 256, "Blaziken" to 257,
            "Mudkip" to 258, "Marshtomp" to 259, "Swampert" to 260,
            "Poochyena" to 261, "Mightyena" to 262,
            "Zigzagoon" to 263, "Linoone" to 264,
            "Wurmple" to 265, "Silcoon" to 266, "Beautifly" to 267,
            "Cascoon" to 268, "Dustox" to 269,
            "Lotad" to 270, "Lombre" to 271, "Ludicolo" to 272,
            "Seedot" to 273, "Nuzleaf" to 274, "Shiftry" to 275,
            "Taillow" to 276, "Swellow" to 277,
            "Wingull" to 278, "Pelipper" to 279,
            "Ralts" to 280, "Kirlia" to 281, "Gardevoir" to 282,
            "Surskit" to 283, "Masquerain" to 284,
            "Shroomish" to 285, "Breloom" to 286,
            "Slakoth" to 287, "Vigoroth" to 288, "Slaking" to 289,
            "Nincada" to 290, "Ninjask" to 291, "Shedinja" to 292,
            "Whismur" to 293, "Loudred" to 294, "Exploud" to 295,
            "Makuhita" to 296, "Hariyama" to 297,
            "Azurill" to 298,
            "Nosepass" to 299,
            "Skitty" to 300, "Delcatty" to 301,
            "Sableye" to 302,
            "Mawile" to 303,
            "Aron" to 304, "Lairon" to 305, "Aggron" to 306,
            "Meditite" to 307, "Medicham" to 308,
            "Electrike" to 309, "Manectric" to 310,
            "Plusle" to 311,
            "Minun" to 312,
            "Volbeat" to 313,
            "Illumise" to 314,
            "Roselia" to 315,
            "Gulpin" to 316, "Swalot" to 317,
            "Carvanha" to 318, "Sharpedo" to 319,
            "Wailmer" to 320, "Wailord" to 321,
            "Numel" to 322, "Camerupt" to 323,
            "Torkoal" to 324,
            "Spoink" to 325, "Grumpig" to 326,
            "Spinda" to 327,
            "Trapinch" to 328, "Vibrava" to 329, "Flygon" to 330,
            "Cacnea" to 331, "Cacturne" to 332,
            "Swablu" to 333, "Altaria" to 334,
            "Zangoose" to 335,
            "Seviper" to 336,
            "Lunatone" to 337,
            "Solrock" to 338,
            "Barboach" to 339, "Whiscash" to 340,
            "Corphish" to 341, "Crawdaunt" to 342,
            "Baltoy" to 343, "Claydol" to 344,
            "Lileep" to 345, "Cradily" to 346,
            "Anorith" to 347, "Armaldo" to 348,
            "Feebas" to 349, "Milotic" to 350,
            "Castform" to 351,
            "Kecleon" to 352,
            "Shuppet" to 353, "Banette" to 354,
            "Duskull" to 355, "Dusclops" to 356,
            "Tropius" to 357,
            "Chimecho" to 358,
            "Absol" to 359,
            "Wynaut" to 360,
            "Snorunt" to 361, "Glalie" to 362,
            "Spheal" to 363, "Sealeo" to 364, "Walrein" to 365,
            "Clamperl" to 366, "Huntail" to 367, "Gorebyss" to 368,
            "Relicanth" to 369,
            "Luvdisc" to 370,
            "Bagon" to 371, "Shelgon" to 372, "Salamence" to 373,
            "Beldum" to 374, "Metang" to 375, "Metagross" to 376,
            "Regirock" to 377,
            "Regice" to 378,
            "Registeel" to 379,
            "Latias" to 380,
            "Latios" to 381,
            "Kyogre" to 382,
            "Groudon" to 383,
            "Rayquaza" to 384,
            "Jirachi" to 385,
            "Deoxys" to 386
        )
        
        return pokemonData.map { (name, number) ->
            Pokemon(name = name, nationalPokedexNumber = number)
        }
    }
}
