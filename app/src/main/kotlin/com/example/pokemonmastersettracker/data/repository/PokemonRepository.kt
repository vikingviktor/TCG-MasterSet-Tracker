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
            android.util.Log.d("PokemonRepository", "Seeding popular Pokemon...")
            val popularPokemon = getPopularPokemonList()
            pokemonDao.insertPokemons(popularPokemon)
            android.util.Log.d("PokemonRepository", "Seeded ${popularPokemon.size} Pokemon")
        }
    }
    
    // Card Operations
    
    suspend fun searchPokemonCardsWithPagination(pokemonName: String, language: String = "en", page: Int = 1, pageSize: Int = 25): List<Card> {
        val query = buildCardQuery(pokemonName, language)
        return try {
            android.util.Log.d("PokemonRepository", "🌐 API REQUEST: pokemonName='$pokemonName', query='$query', page=$page, pageSize=$pageSize")
            val apiStartTime = System.currentTimeMillis()
            
            val response = api.searchCards(query = query, pageSize = pageSize, page = page)
            
            val apiTime = System.currentTimeMillis() - apiStartTime
            android.util.Log.d("PokemonRepository", "⏱️ API RESPONSE: ${response.cards.size} cards in ${apiTime}ms")
            
            cardDao.insertCards(response.cards)
            response.cards
        } catch (e: Exception) {
            // Try to get cached results on error
            val cachedCards = cardDao.getCardsByPokemonNameSync("%$pokemonName%")
            if (cachedCards.isNotEmpty()) {
                android.util.Log.w("PokemonRepository", "API failed for '$pokemonName', using ${cachedCards.size} cached cards")
                return cachedCards
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
            val query = "name:${pokemonName}*"
            // Use pageSize=1 to minimize data transfer, we only need the totalCount
            val response = api.searchCards(query = query, pageSize = 1)
            val count = response.totalCount // Use the totalCount field from API response
            android.util.Log.d("PokemonRepository", "Fetched total cards for $pokemonName: $count")
            count
        } catch (e: Exception) {
            android.util.Log.w("PokemonRepository", "Could not fetch total cards for $pokemonName (${e.message}), will use estimated count")
            // Fallback: estimate based on Pokemon popularity (most Pokemon have 50-200 cards)
            100 // Reasonable default
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

    private fun buildCardQuery(pokemonName: String, language: String): String {
        // The Pokemon TCG API doesn't filter by language in the query
        // Instead, it returns cards from all sets (English, Japanese, etc.)
        // Cards are distinguished by their set.id
        val cleanName = pokemonName.trim()
        // FIXED: Removed asterisk (*) wildcard - API returns 404 with it
        // Using exact name match works better with the Pokemon TCG API
        return "name:$cleanName"
    }
    
    private fun getPopularPokemonList(): List<Pokemon> {
        return listOf(
            // Gen 1 (Kanto)
            "Bulbasaur", "Ivysaur", "Venusaur", "Charmander", "Charmeleon", "Charizard",
            "Squirtle", "Wartortle", "Blastoise", "Caterpie", "Metapod", "Butterfree",
            "Weedle", "Kakuna", "Beedrill", "Pidgey", "Pidgeotto", "Pidgeot",
            "Rattata", "Raticate", "Spearow", "Fearow", "Ekans", "Arbok",
            "Pikachu", "Raichu", "Sandshrew", "Sandslash", "Nidoran♀", "Nidorina", "Nidoqueen",
            "Nidoran♂", "Nidorino", "Nidoking", "Clefairy", "Clefable", "Vulpix", "Ninetales",
            "Jigglypuff", "Wigglytuff", "Zubat", "Golbat", "Oddish", "Gloom", "Vileplume",
            "Paras", "Parasect", "Venonat", "Venomoth", "Diglett", "Dugtrio",
            "Meowth", "Persian", "Psyduck", "Golduck", "Mankey", "Primeape",
            "Growlithe", "Arcanine", "Poliwag", "Poliwhirl", "Poliwrath", "Abra", "Kadabra", "Alakazam",
            "Machop", "Machoke", "Machamp", "Bellsprout", "Weepinbell", "Victreebel",
            "Tentacool", "Tentacruel", "Geodude", "Graveler", "Golem", "Ponyta", "Rapidash",
            "Slowpoke", "Slowbro", "Magnemite", "Magneton", "Farfetch'd", "Doduo", "Dodrio",
            "Seel", "Dewgong", "Grimer", "Muk", "Shellder", "Cloyster",
            "Gastly", "Haunter", "Gengar", "Onix", "Drowzee", "Hypno",
            "Krabby", "Kingler", "Voltorb", "Electrode", "Exeggcute", "Exeggutor",
            "Cubone", "Marowak", "Hitmonlee", "Hitmonchan", "Lickitung",
            "Koffing", "Weezing", "Rhyhorn", "Rhydon", "Chansey", "Tangela",
            "Kangaskhan", "Horsea", "Seadra", "Goldeen", "Seaking",
            "Staryu", "Starmie", "Mr. Mime", "Scyther", "Jynx",
            "Electabuzz", "Magmar", "Pinsir", "Tauros", "Magikarp", "Gyarados",
            "Lapras", "Ditto", "Eevee", "Vaporeon", "Jolteon", "Flareon",
            "Porygon", "Omanyte", "Omastar", "Kabuto", "Kabutops",
            "Aerodactyl", "Snorlax", "Articuno", "Zapdos", "Moltres",
            "Dratini", "Dragonair", "Dragonite", "Mewtwo", "Mew",
            // Gen 2 (Johto)
            "Chikorita", "Bayleef", "Meganium", "Cyndaquil", "Quilava", "Typhlosion",
            "Totodile", "Croconaw", "Feraligatr", "Sentret", "Furret",
            "Hoothoot", "Noctowl", "Ledyba", "Ledian", "Spinarak", "Ariados",
            "Crobat", "Chinchou", "Lanturn", "Pichu", "Cleffa", "Igglybuff",
            "Togepi", "Togetic", "Natu", "Xatu", "Mareep", "Flaaffy", "Ampharos",
            "Bellossom", "Marill", "Azumarill", "Sudowoodo", "Politoed",
            "Hoppip", "Skiploom", "Jumpluff", "Aipom", "Sunkern", "Sunflora",
            "Yanma", "Wooper", "Quagsire", "Espeon", "Umbreon",
            "Murkrow", "Slowking", "Misdreavus", "Unown", "Wobbuffet",
            "Girafarig", "Pineco", "Forretress", "Dunsparce", "Gligar",
            "Steelix", "Snubbull", "Granbull", "Qwilfish", "Scizor",
            "Shuckle", "Heracross", "Sneasel", "Teddiursa", "Ursaring",
            "Slugma", "Magcargo", "Swinub", "Piloswine", "Corsola",
            "Remoraid", "Octillery", "Delibird", "Mantine", "Skarmory",
            "Houndour", "Houndoom", "Kingdra", "Phanpy", "Donphan",
            "Porygon2", "Stantler", "Smeargle", "Tyrogue", "Hitmontop",
            "Smoochum", "Elekid", "Magby", "Miltank", "Blissey",
            "Raikou", "Entei", "Suicune", "Larvitar", "Pupitar", "Tyranitar",
            "Lugia", "Ho-Oh", "Celebi",
            // Gen 3 (Hoenn)
            "Treecko", "Grovyle", "Sceptile", "Torchic", "Combusken", "Blaziken",
            "Mudkip", "Marshtomp", "Swampert", "Poochyena", "Mightyena",
            "Zigzagoon", "Linoone", "Wurmple", "Silcoon", "Beautifly",
            "Cascoon", "Dustox", "Lotad", "Lombre", "Ludicolo",
            "Seedot", "Nuzleaf", "Shiftry", "Taillow", "Swellow",
            "Wingull", "Pelipper", "Ralts", "Kirlia", "Gardevoir",
            "Surskit", "Masquerain", "Shroomish", "Breloom", "Slakoth", "Vigoroth", "Slaking",
            "Nincada", "Ninjask", "Shedinja", "Whismur", "Loudred", "Exploud",
            "Makuhita", "Hariyama", "Azurill", "Nosepass", "Skitty", "Delcatty",
            "Sableye", "Mawile", "Aron", "Lairon", "Aggron",
            "Meditite", "Medicham", "Electrike", "Manectric", "Plusle", "Minun",
            "Volbeat", "Illumise", "Roselia", "Gulpin", "Swalot",
            "Carvanha", "Sharpedo", "Wailmer", "Wailord", "Numel", "Camerupt",
            "Torkoal", "Spoink", "Grumpig", "Spinda", "Trapinch", "Vibrava", "Flygon",
            "Cacnea", "Cacturne", "Swablu", "Altaria", "Zangoose", "Seviper",
            "Lunatone", "Solrock", "Barboach", "Whiscash", "Corphish", "Crawdaunt",
            "Baltoy", "Claydol", "Lileep", "Cradily", "Anorith", "Armaldo",
            "Feebas", "Milotic", "Castform", "Kecleon", "Shuppet", "Banette",
            "Duskull", "Dusclops", "Tropius", "Chimecho", "Absol",
            "Wynaut", "Snorunt", "Glalie", "Spheal", "Sealeo", "Walrein",
            "Clamperl", "Huntail", "Gorebyss", "Relicanth",
            "Luvdisc", "Bagon", "Shelgon", "Salamence",
            "Beldum", "Metang", "Metagross", "Regirock", "Regice", "Registeel",
            "Latias", "Latios", "Kyogre", "Groudon", "Rayquaza",
            "Jirachi", "Deoxys",
            // Gen 4-9 starters and legendaries
            "Turtwig", "Grotle", "Torterra", "Chimchar", "Monferno", "Infernape",
            "Piplup", "Prinplup", "Empoleon", "Dialga", "Palkia", "Giratina",
            "Darkrai", "Arceus", "Lucario", "Garchomp", "Togekiss", "Mamoswine",
            "Snivy", "Servine", "Serperior", "Tepig", "Pignite", "Emboar",
            "Oshawott", "Dewott", "Samurott", "Reshiram", "Zekrom", "Kyurem",
            "Chespin", "Quilladin", "Chesnaught", "Fennekin", "Braixen", "Delphox",
            "Froakie", "Frogadier", "Greninja", "Xerneas", "Yveltal", "Zygarde",
            "Rowlet", "Dartrix", "Decidueye", "Litten", "Torracat", "Incineroar",
            "Popplio", "Brionne", "Primarina", "Solgaleo", "Lunala", "Necrozma",
            "Grookey", "Thwackey", "Rillaboom", "Scorbunny", "Raboot", "Cinderace",
            "Sobble", "Drizzile", "Inteleon", "Zacian", "Zamazenta", "Eternatus",
            "Sprigatito", "Floragato", "Meowscarada", "Fuecoco", "Crocalor", "Skeledirge",
            "Quaxly", "Quaxwell", "Quaquaval", "Koraidon", "Miraidon"
        ).map { Pokemon(name = it) }
    }
}
