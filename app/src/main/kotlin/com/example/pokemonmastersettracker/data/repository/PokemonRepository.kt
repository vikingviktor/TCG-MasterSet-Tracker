package com.example.pokemonmastersettracker.data.repository

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
import com.example.pokemonmastersettracker.data.api.TCGdexService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PokemonRepository @Inject constructor(
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
    
    suspend fun setFavoriteStatus(pokemonName: String, isFavorite: Boolean) {
        pokemonDao.updateFavoriteStatus(pokemonName, isFavorite)
    }
    
    suspend fun updatePokemonImage(pokemonName: String, imageUrl: String) {
        pokemonDao.updatePokemonImage(pokemonName, imageUrl)
    }
    
    suspend fun saveCards(cards: List<Card>) {
        cards.forEach { card ->
            cardDao.insertCard(card)
        }
    }
    
    /**
     * Get cached cards for a Pokemon by name
     * Returns cards from local database
     */
    suspend fun getCachedCardsForPokemon(pokemonName: String): List<Card> {
        return cardDao.getCardsByPokemonNameSync("%$pokemonName%")
    }
    
    suspend fun seedPopularPokemon() {
        val count = pokemonDao.getPokemonCount()
        val expectedCount = 1025 // Total Pokemon Gen 1-9
        if (count < expectedCount) {
            android.util.Log.d("PokemonRepository", "Seeding Pokemon database... (current: $count, expected: $expectedCount)")
            val popularPokemon = getPopularPokemonList()
            pokemonDao.insertPokemons(popularPokemon)
            android.util.Log.d("PokemonRepository", "✓ Seeded ${popularPokemon.size} Pokemon (Gen 1-9, Pokedex #1-1025)")
        } else {
            android.util.Log.d("PokemonRepository", "Pokemon database complete (count: $count)")
        }
    }
    
    // Local card operations
    
    fun getLocalCardsByPokemonName(pokemonName: String): Flow<List<Card>> {
        return cardDao.getCardsByPokemonName(pokemonName)
    }

    fun getAllLocalCards(): Flow<List<Card>> {
        return cardDao.getAllCards()
    }

    suspend fun getCardById(cardId: String): Card? {
        return cardDao.getCardById(cardId)
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
        android.util.Log.d("PokemonRepository", "Adding favorite: $pokemonName for user: $userId")
        
        // Fetch cards from TCGdex to get image and count
        val tcgdexService = TCGdexService()
        val cards = try {
            tcgdexService.searchCardsByPokemon(pokemonName, "en")
        } catch (e: Exception) {
            android.util.Log.w("PokemonRepository", "Failed to fetch TCGdex cards for $pokemonName: ${e.message}")
            emptyList()
        }
        
        val totalCards = cards.size
        val imageUrl = cards.firstOrNull()?.image?.small
        
        // Update Pokemon table with image if we got one
        if (imageUrl != null) {
            updatePokemonImage(pokemonName, imageUrl)
        }
        
        // Add to favorites (IGNORE if exists)
        favoritePokemonDao.addFavorite(
            FavoritePokemon(
                userId = userId,
                pokemonName = pokemonName,
                totalCards = totalCards
            )
        )
        
        // Always update totalCards in case it changed or was 0 before
        favoritePokemonDao.updateTotalCards(userId, pokemonName, totalCards)
        
        android.util.Log.d("PokemonRepository", "✓ Added favorite: $pokemonName (${totalCards} cards, image: ${imageUrl != null})")
    }

    suspend fun removeFavoritePokemon(userId: String, pokemonName: String) {
        favoritePokemonDao.removeFavorite(userId, pokemonName)
    }

    fun getUserFavoritePokemon(userId: String): Flow<List<FavoritePokemon>> {
        return favoritePokemonDao.getUserFavorites(userId)
    }
    
    // Returns Flow that emits fully-populated Pokemon objects
    // JOIN query does ALL the work - no additional queries in map = no infinite loop
    fun getUserFavoritePokemonWithDetails(userId: String): Flow<List<Pokemon>> {
        return favoritePokemonDao.getUserFavoritesWithDetails(userId)
            .map { detailsList ->
                android.util.Log.d("PokemonRepository", "🔄 Transforming ${detailsList.size} favorites from JOIN query")
                detailsList.map { details ->
                    android.util.Log.d("PokemonRepository", "  ✓ ${details.pokemonName}: ${details.ownedCount}/${details.totalCards} cards")
                    Pokemon(
                        name = details.pokemonName,
                        nationalPokedexNumber = details.pokedexNumber,
                        imageUrl = details.imageUrl,
                        isFavorite = true,
                        ownedCount = details.ownedCount,
                        totalCards = details.totalCards
                    )
                }
            }
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
            android.util.Log.d("PokemonRepository", "Found ${favoritePokemon.size} favorite Pokemon for user $userId")
            
            favoritePokemon.forEach { fav ->
                android.util.Log.d("PokemonRepository", "  - ${fav.pokemonName}: ${fav.totalCards} cards")
            }
            
            // Use cached totalCards from database for faster performance
            val totalCount = favoritePokemon.sumOf { it.totalCards }
            
            android.util.Log.d("PokemonRepository", "✓ Total cards for ${favoritePokemon.size} favorite Pokemon: $totalCount")
            totalCount
        } catch (e: Exception) {
            android.util.Log.e("PokemonRepository", "Error getting total cards count: ${e.message}")
            0
        }
    }
    
    suspend fun getOwnedCardsCountForPokemon(userId: String, pokemonName: String): Int {
        return try {
            // Get all cards for this Pokemon from database
            val allCards = cardDao.getCardsByPokemonNameSync("%$pokemonName%")
            android.util.Log.d("PokemonRepository", "Found ${allCards.size} total cards in DB for $pokemonName")
            
            val cardIds = allCards.map { it.id }
            
            // Count how many of these cards the user owns
            val userCards = userCardDao.getUserCardsSync(userId)
            val ownedCount = userCards.count { it.isOwned && it.cardId in cardIds }
            
            android.util.Log.d("PokemonRepository", "✓ Owned cards for $pokemonName: $ownedCount (of ${allCards.size} total in DB)")
            ownedCount
        } catch (e: Exception) {
            android.util.Log.e("PokemonRepository", "Error getting owned cards count for $pokemonName: ${e.message}")
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
            "Deoxys" to 386,
            
            // Gen 4 (Sinnoh) - #387-493
            "Turtwig" to 387, "Grotle" to 388, "Torterra" to 389,
            "Chimchar" to 390, "Monferno" to 391, "Infernape" to 392,
            "Piplup" to 393, "Prinplup" to 394, "Empoleon" to 395,
            "Starly" to 396, "Staravia" to 397, "Staraptor" to 398,
            "Bidoof" to 399, "Bibarel" to 400,
            "Kricketot" to 401, "Kricketune" to 402,
            "Shinx" to 403, "Luxio" to 404, "Luxray" to 405,
            "Budew" to 406,
            "Roserade" to 407,
            "Cranidos" to 408, "Rampardos" to 409,
            "Shieldon" to 410, "Bastiodon" to 411,
            "Burmy" to 412, "Wormadam" to 413, "Mothim" to 414,
            "Combee" to 415, "Vespiquen" to 416,
            "Pachirisu" to 417,
            "Buizel" to 418, "Floatzel" to 419,
            "Cherubi" to 420, "Cherrim" to 421,
            "Shellos" to 422, "Gastrodon" to 423,
            "Ambipom" to 424,
            "Drifloon" to 425, "Drifblim" to 426,
            "Buneary" to 427, "Lopunny" to 428,
            "Mismagius" to 429,
            "Honchkrow" to 430,
            "Glameow" to 431, "Purugly" to 432,
            "Chingling" to 433,
            "Stunky" to 434, "Skuntank" to 435,
            "Bronzor" to 436, "Bronzong" to 437,
            "Bonsly" to 438,
            "Mime Jr." to 439,
            "Happiny" to 440,
            "Chatot" to 441,
            "Spiritomb" to 442,
            "Gible" to 443, "Gabite" to 444, "Garchomp" to 445,
            "Munchlax" to 446,
            "Riolu" to 447, "Lucario" to 448,
            "Hippopotas" to 449, "Hippowdon" to 450,
            "Skorupi" to 451, "Drapion" to 452,
            "Croagunk" to 453, "Toxicroak" to 454,
            "Carnivine" to 455,
            "Finneon" to 456, "Lumineon" to 457,
            "Mantyke" to 458,
            "Snover" to 459, "Abomasnow" to 460,
            "Weavile" to 461,
            "Magnezone" to 462,
            "Lickilicky" to 463,
            "Rhyperior" to 464,
            "Tangrowth" to 465,
            "Electivire" to 466,
            "Magmortar" to 467,
            "Togekiss" to 468,
            "Yanmega" to 469,
            "Leafeon" to 470,
            "Glaceon" to 471,
            "Gliscor" to 472,
            "Mamoswine" to 473,
            "Porygon-Z" to 474,
            "Gallade" to 475,
            "Probopass" to 476,
            "Dusknoir" to 477,
            "Froslass" to 478,
            "Rotom" to 479,
            "Uxie" to 480,
            "Mesprit" to 481,
            "Azelf" to 482,
            "Dialga" to 483,
            "Palkia" to 484,
            "Heatran" to 485,
            "Regigigas" to 486,
            "Giratina" to 487,
            "Cresselia" to 488,
            "Phione" to 489,
            "Manaphy" to 490,
            "Darkrai" to 491,
            "Shaymin" to 492,
            "Arceus" to 493,
            
            // Gen 5 (Unova) - #494-649
            "Victini" to 494,
            "Snivy" to 495, "Servine" to 496, "Serperior" to 497,
            "Tepig" to 498, "Pignite" to 499, "Emboar" to 500,
            "Oshawott" to 501, "Dewott" to 502, "Samurott" to 503,
            "Patrat" to 504, "Watchog" to 505,
            "Lillipup" to 506, "Herdier" to 507, "Stoutland" to 508,
            "Purrloin" to 509, "Liepard" to 510,
            "Pansage" to 511, "Simisage" to 512,
            "Pansear" to 513, "Simisear" to 514,
            "Panpour" to 515, "Simipour" to 516,
            "Munna" to 517, "Musharna" to 518,
            "Pidove" to 519, "Tranquill" to 520, "Unfezant" to 521,
            "Blitzle" to 522, "Zebstrika" to 523,
            "Roggenrola" to 524, "Boldore" to 525, "Gigalith" to 526,
            "Woobat" to 527, "Swoobat" to 528,
            "Drilbur" to 529, "Excadrill" to 530,
            "Audino" to 531,
            "Timburr" to 532, "Gurdurr" to 533, "Conkeldurr" to 534,
            "Tympole" to 535, "Palpitoad" to 536, "Seismitoad" to 537,
            "Throh" to 538,
            "Sawk" to 539,
            "Sewaddle" to 540, "Swadloon" to 541, "Leavanny" to 542,
            "Venipede" to 543, "Whirlipede" to 544, "Scolipede" to 545,
            "Cottonee" to 546, "Whimsicott" to 547,
            "Petilil" to 548, "Lilligant" to 549,
            "Basculin" to 550,
            "Sandile" to 551, "Krokorok" to 552, "Krookodile" to 553,
            "Darumaka" to 554, "Darmanitan" to 555,
            "Maractus" to 556,
            "Dwebble" to 557, "Crustle" to 558,
            "Scraggy" to 559, "Scrafty" to 560,
            "Sigilyph" to 561,
            "Yamask" to 562, "Cofagrigus" to 563,
            "Tirtouga" to 564, "Carracosta" to 565,
            "Archen" to 566, "Archeops" to 567,
            "Trubbish" to 568, "Garbodor" to 569,
            "Zorua" to 570, "Zoroark" to 571,
            "Minccino" to 572, "Cinccino" to 573,
            "Gothita" to 574, "Gothorita" to 575, "Gothitelle" to 576,
            "Solosis" to 577, "Duosion" to 578, "Reuniclus" to 579,
            "Ducklett" to 580, "Swanna" to 581,
            "Vanillite" to 582, "Vanillish" to 583, "Vanilluxe" to 584,
            "Deerling" to 585, "Sawsbuck" to 586,
            "Emolga" to 587,
            "Karrablast" to 588, "Escavalier" to 589,
            "Foongus" to 590, "Amoonguss" to 591,
            "Frillish" to 592, "Jellicent" to 593,
            "Alomomola" to 594,
            "Joltik" to 595, "Galvantula" to 596,
            "Ferroseed" to 597, "Ferrothorn" to 598,
            "Klink" to 599, "Klang" to 600, "Klinklang" to 601,
            "Tynamo" to 602, "Eelektrik" to 603, "Eelektross" to 604,
            "Elgyem" to 605, "Beheeyem" to 606,
            "Litwick" to 607, "Lampent" to 608, "Chandelure" to 609,
            "Axew" to 610, "Fraxure" to 611, "Haxorus" to 612,
            "Cubchoo" to 613, "Beartic" to 614,
            "Cryogonal" to 615,
            "Shelmet" to 616, "Accelgor" to 617,
            "Stunfisk" to 618,
            "Mienfoo" to 619, "Mienshao" to 620,
            "Druddigon" to 621,
            "Golett" to 622, "Golurk" to 623,
            "Pawniard" to 624, "Bisharp" to 625,
            "Bouffalant" to 626,
            "Rufflet" to 627, "Braviary" to 628,
            "Vullaby" to 629, "Mandibuzz" to 630,
            "Heatmor" to 631,
            "Durant" to 632,
            "Deino" to 633, "Zweilous" to 634, "Hydreigon" to 635,
            "Larvesta" to 636, "Volcarona" to 637,
            "Cobalion" to 638,
            "Terrakion" to 639,
            "Virizion" to 640,
            "Tornadus" to 641,
            "Thundurus" to 642,
            "Reshiram" to 643,
            "Zekrom" to 644,
            "Landorus" to 645,
            "Kyurem" to 646,
            "Keldeo" to 647,
            "Meloetta" to 648,
            "Genesect" to 649,
            
            // Gen 6 (Kalos) - #650-721
            "Chespin" to 650, "Quilladin" to 651, "Chesnaught" to 652,
            "Fennekin" to 653, "Braixen" to 654, "Delphox" to 655,
            "Froakie" to 656, "Frogadier" to 657, "Greninja" to 658,
            "Bunnelby" to 659, "Diggersby" to 660,
            "Fletchling" to 661, "Fletchinder" to 662, "Talonflame" to 663,
            "Scatterbug" to 664, "Spewpa" to 665, "Vivillon" to 666,
            "Litleo" to 667, "Pyroar" to 668,
            "Flabébé" to 669, "Floette" to 670, "Florges" to 671,
            "Skiddo" to 672, "Gogoat" to 673,
            "Pancham" to 674, "Pangoro" to 675,
            "Furfrou" to 676,
            "Espurr" to 677, "Meowstic" to 678,
            "Honedge" to 679, "Doublade" to 680, "Aegislash" to 681,
            "Spritzee" to 682, "Aromatisse" to 683,
            "Swirlix" to 684, "Slurpuff" to 685,
            "Inkay" to 686, "Malamar" to 687,
            "Binacle" to 688, "Barbaracle" to 689,
            "Skrelp" to 690, "Dragalge" to 691,
            "Clauncher" to 692, "Clawitzer" to 693,
            "Helioptile" to 694, "Heliolisk" to 695,
            "Tyrunt" to 696, "Tyrantrum" to 697,
            "Amaura" to 698, "Aurorus" to 699,
            "Sylveon" to 700,
            "Hawlucha" to 701,
            "Dedenne" to 702,
            "Carbink" to 703,
            "Goomy" to 704, "Sliggoo" to 705, "Goodra" to 706,
            "Klefki" to 707,
            "Phantump" to 708, "Trevenant" to 709,
            "Pumpkaboo" to 710, "Gourgeist" to 711,
            "Bergmite" to 712, "Avalugg" to 713,
            "Noibat" to 714, "Noivern" to 715,
            "Xerneas" to 716,
            "Yveltal" to 717,
            "Zygarde" to 718,
            "Diancie" to 719,
            "Hoopa" to 720,
            "Volcanion" to 721,
            
            // Gen 7 (Alola) - #722-809
            "Rowlet" to 722, "Dartrix" to 723, "Decidueye" to 724,
            "Litten" to 725, "Torracat" to 726, "Incineroar" to 727,
            "Popplio" to 728, "Brionne" to 729, "Primarina" to 730,
            "Pikipek" to 731, "Trumbeak" to 732, "Toucannon" to 733,
            "Yungoos" to 734, "Gumshoos" to 735,
            "Grubbin" to 736, "Charjabug" to 737, "Vikavolt" to 738,
            "Crabrawler" to 739, "Crabominable" to 740,
            "Oricorio" to 741,
            "Cutiefly" to 742, "Ribombee" to 743,
            "Rockruff" to 744, "Lycanroc" to 745,
            "Wishiwashi" to 746,
            "Mareanie" to 747, "Toxapex" to 748,
            "Mudbray" to 749, "Mudsdale" to 750,
            "Dewpider" to 751, "Araquanid" to 752,
            "Fomantis" to 753, "Lurantis" to 754,
            "Morelull" to 755, "Shiinotic" to 756,
            "Salandit" to 757, "Salazzle" to 758,
            "Stufful" to 759, "Bewear" to 760,
            "Bounsweet" to 761, "Steenee" to 762, "Tsareena" to 763,
            "Comfey" to 764,
            "Oranguru" to 765,
            "Passimian" to 766,
            "Wimpod" to 767, "Golisopod" to 768,
            "Sandygast" to 769, "Palossand" to 770,
            "Pyukumuku" to 771,
            "Type: Null" to 772, "Silvally" to 773,
            "Minior" to 774,
            "Komala" to 775,
            "Turtonator" to 776,
            "Togedemaru" to 777,
            "Mimikyu" to 778,
            "Bruxish" to 779,
            "Drampa" to 780,
            "Dhelmise" to 781,
            "Jangmo-o" to 782, "Hakamo-o" to 783, "Kommo-o" to 784,
            "Tapu Koko" to 785,
            "Tapu Lele" to 786,
            "Tapu Bulu" to 787,
            "Tapu Fini" to 788,
            "Cosmog" to 789, "Cosmoem" to 790, "Solgaleo" to 791,
            "Lunala" to 792,
            "Nihilego" to 793,
            "Buzzwole" to 794,
            "Pheromosa" to 795,
            "Xurkitree" to 796,
            "Celesteela" to 797,
            "Kartana" to 798,
            "Guzzlord" to 799,
            "Necrozma" to 800,
            "Magearna" to 801,
            "Marshadow" to 802,
            "Poipole" to 803, "Naganadel" to 804,
            "Stakataka" to 805,
            "Blacephalon" to 806,
            "Zeraora" to 807,
            "Meltan" to 808, "Melmetal" to 809,
            
            // Gen 8 (Galar) - #810-905
            "Grookey" to 810, "Thwackey" to 811, "Rillaboom" to 812,
            "Scorbunny" to 813, "Raboot" to 814, "Cinderace" to 815,
            "Sobble" to 816, "Drizzile" to 817, "Inteleon" to 818,
            "Skwovet" to 819, "Greedent" to 820,
            "Rookidee" to 821, "Corvisquire" to 822, "Corviknight" to 823,
            "Blipbug" to 824, "Dottler" to 825, "Orbeetle" to 826,
            "Nickit" to 827, "Thievul" to 828,
            "Gossifleur" to 829, "Eldegoss" to 830,
            "Wooloo" to 831, "Dubwool" to 832,
            "Chewtle" to 833, "Drednaw" to 834,
            "Yamper" to 835, "Boltund" to 836,
            "Rolycoly" to 837, "Carkol" to 838, "Coalossal" to 839,
            "Applin" to 840, "Flapple" to 841, "Appletun" to 842,
            "Silicobra" to 843, "Sandaconda" to 844,
            "Cramorant" to 845,
            "Arrokuda" to 846, "Barraskewda" to 847,
            "Toxel" to 848, "Toxtricity" to 849,
            "Sizzlipede" to 850, "Centiskorch" to 851,
            "Clobbopus" to 852, "Grapploct" to 853,
            "Sinistea" to 854, "Polteageist" to 855,
            "Hatenna" to 856, "Hattrem" to 857, "Hatterene" to 858,
            "Impidimp" to 859, "Morgrem" to 860, "Grimmsnarl" to 861,
            "Obstagoon" to 862,
            "Perrserker" to 863,
            "Cursola" to 864,
            "Sirfetch'd" to 865,
            "Mr. Rime" to 866,
            "Runerigus" to 867,
            "Milcery" to 868, "Alcremie" to 869,
            "Falinks" to 870,
            "Pincurchin" to 871,
            "Snom" to 872, "Frosmoth" to 873,
            "Stonjourner" to 874,
            "Eiscue" to 875,
            "Indeedee" to 876,
            "Morpeko" to 877,
            "Cufant" to 878, "Copperajah" to 879,
            "Dracozolt" to 880,
            "Arctozolt" to 881,
            "Dracovish" to 882,
            "Arctovish" to 883,
            "Duraludon" to 884,
            "Dreepy" to 885, "Drakloak" to 886, "Dragapult" to 887,
            "Zacian" to 888,
            "Zamazenta" to 889,
            "Eternatus" to 890,
            "Kubfu" to 891, "Urshifu" to 892,
            "Zarude" to 893,
            "Regieleki" to 894,
            "Regidrago" to 895,
            "Glastrier" to 896,
            "Spectrier" to 897,
            "Calyrex" to 898,
            "Wyrdeer" to 899,
            "Kleavor" to 900,
            "Ursaluna" to 901,
            "Basculegion" to 902,
            "Sneasler" to 903,
            "Overqwil" to 904,
            "Enamorus" to 905,
            
            // Gen 9 (Paldea) - #906-1025
            "Sprigatito" to 906, "Floragato" to 907, "Meowscarada" to 908,
            "Fuecoco" to 909, "Crocalor" to 910, "Skeledirge" to 911,
            "Quaxly" to 912, "Quaxwell" to 913, "Quaquaval" to 914,
            "Lechonk" to 915, "Oinkologne" to 916,
            "Tarountula" to 917, "Spidops" to 918,
            "Nymble" to 919, "Lokix" to 920,
            "Pawmi" to 921, "Pawmo" to 922, "Pawmot" to 923,
            "Tandemaus" to 924, "Maushold" to 925,
            "Fidough" to 926, "Dachsbun" to 927,
            "Smoliv" to 928, "Dolliv" to 929, "Arboliva" to 930,
            "Squawkabilly" to 931,
            "Nacli" to 932, "Naclstack" to 933, "Garganacl" to 934,
            "Charcadet" to 935, "Armarouge" to 936, "Ceruledge" to 937,
            "Tadbulb" to 938, "Bellibolt" to 939,
            "Wattrel" to 940, "Kilowattrel" to 941,
            "Maschiff" to 942, "Mabosstiff" to 943,
            "Shroodle" to 944, "Grafaiai" to 945,
            "Bramblin" to 946, "Brambleghast" to 947,
            "Toedscool" to 948, "Toedscruel" to 949,
            "Klawf" to 950,
            "Capsakid" to 951, "Scovillain" to 952,
            "Rellor" to 953, "Rabsca" to 954,
            "Flittle" to 955, "Espathra" to 956,
            "Tinkatink" to 957, "Tinkatuff" to 958, "Tinkaton" to 959,
            "Wiglett" to 960, "Wugtrio" to 961,
            "Bombirdier" to 962,
            "Finizen" to 963, "Palafin" to 964,
            "Varoom" to 965, "Revavroom" to 966,
            "Cyclizar" to 967,
            "Orthworm" to 968,
            "Glimmet" to 969, "Glimmora" to 970,
            "Greavard" to 971, "Houndstone" to 972,
            "Flamigo" to 973,
            "Cetoddle" to 974, "Cetitan" to 975,
            "Veluza" to 976,
            "Dondozo" to 977,
            "Tatsugiri" to 978,
            "Annihilape" to 979,
            "Clodsire" to 980,
            "Farigiraf" to 981,
            "Dudunsparce" to 982,
            "Kingambit" to 983,
            "Great Tusk" to 984,
            "Scream Tail" to 985,
            "Brute Bonnet" to 986,
            "Flutter Mane" to 987,
            "Slither Wing" to 988,
            "Sandy Shocks" to 989,
            "Iron Treads" to 990,
            "Iron Bundle" to 991,
            "Iron Hands" to 992,
            "Iron Jugulis" to 993,
            "Iron Moth" to 994,
            "Iron Thorns" to 995,
            "Frigibax" to 996, "Arctibax" to 997, "Baxcalibur" to 998,
            "Gimmighoul" to 999, "Gholdengo" to 1000,
            "Wo-Chien" to 1001,
            "Chien-Pao" to 1002,
            "Ting-Lu" to 1003,
            "Chi-Yu" to 1004,
            "Roaring Moon" to 1005,
            "Iron Valiant" to 1006,
            "Koraidon" to 1007,
            "Miraidon" to 1008,
            "Walking Wake" to 1009,
            "Iron Leaves" to 1010,
            "Dipplin" to 1011,
            "Poltchageist" to 1012, "Sinistcha" to 1013,
            "Okidogi" to 1014,
            "Munkidori" to 1015,
            "Fezandipiti" to 1016,
            "Ogerpon" to 1017,
            "Archaludon" to 1018,
            "Hydrapple" to 1019,
            "Gouging Fire" to 1020,
            "Raging Bolt" to 1021,
            "Iron Boulder" to 1022,
            "Iron Crown" to 1023,
            "Terapagos" to 1024,
            "Pecharunt" to 1025
        )
        
        return pokemonData.map { (name, number) ->
            Pokemon(name = name, nationalPokedexNumber = number)
        }
    }
}
