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
        
        // Add to favorites with total count
        favoritePokemonDao.addFavorite(
            FavoritePokemon(
                userId = userId,
                pokemonName = pokemonName,
                totalCards = totalCards
            )
        )
        
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
            "Deoxys" to 386
        )
        
        return pokemonData.map { (name, number) ->
            Pokemon(name = name, nationalPokedexNumber = number)
        }
    }
}
