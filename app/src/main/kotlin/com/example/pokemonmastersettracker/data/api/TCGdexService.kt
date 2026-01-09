package com.example.pokemonmastersettracker.data.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.example.pokemonmastersettracker.data.models.Card
import com.example.pokemonmastersettracker.data.models.CardSet
import com.example.pokemonmastersettracker.data.models.CardImage

/**
 * TCGdex REST API interface
 * API Documentation: https://tcgdex.dev/rest
 */
interface TCGdexApi {
    @GET("{language}/cards")
    suspend fun searchCards(
        @Path("language") language: String,
        @Query("name") name: String
    ): List<TCGdexCardResponse>
    
    // Get all cards for a language
    @GET("{language}/cards")
    suspend fun getAllCards(
        @Path("language") language: String
    ): List<TCGdexCardResponse>
}

/**
 * Response model from TCGdex API
 */
data class TCGdexCardResponse(
    val id: String,
    val localId: String?,
    val name: String,
    val image: String?,
    val category: String?,
    val hp: String?,
    val types: List<String>?,
    val evolveFrom: String?,
    val level: String?,
    val dexId: List<Int>?,
    val rarity: String?,
    val illustrator: String?,
    val set: TCGdexSetInfo?
)

data class TCGdexSetInfo(
    val id: String,
    val name: String,
    val releaseDate: String?
)

/**
 * Service wrapper for TCGdex REST API
 * Used specifically for fetching Japanese cards in Favorites section
 */
class TCGdexService {
    
    private val api: TCGdexApi = Retrofit.Builder()
        .baseUrl("https://api.tcgdex.net/v2/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TCGdexApi::class.java)
    
    /**
     * Search for Pokemon cards by name in specified language
     * @param pokemonName Name of the Pokemon (e.g., "Pikachu")
     * @param language "en" for English or "ja" for Japanese
     * @return List of cards matching the Pokemon name
     */
    suspend fun searchCardsByPokemon(pokemonName: String, language: String = "en"): List<Card> = withContext(Dispatchers.IO) {
        try {
            Log.d("TCGdexService", "🔍 Searching TCGdex for '$pokemonName' in language: $language")
            
            // Get Pokedex number for filtering
            val pokedexNumber = getPokedexNumber(pokemonName)
            
            Log.d("TCGdexService", "📡 Fetching all cards for language: $language")
            val allCards = api.getAllCards(language)
            
            Log.d("TCGdexService", "✓ Got ${allCards.size} total cards")
            
            // Filter cards by dexId matching the Pokemon's Pokedex number
            val filteredCards = allCards.filter { card ->
                card.dexId?.contains(pokedexNumber) == true
            }
            
            Log.d("TCGdexService", "✓ Filtered to ${filteredCards.size} cards for $pokemonName (Dex #$pokedexNumber)")
            if (filteredCards.isNotEmpty()) {
                Log.d("TCGdexService", "📋 First card: ${filteredCards.first().name} (${filteredCards.first().id})")
                Log.d("TCGdexService", "🖼️  Image URL: ${filteredCards.first().image}")
            }
            
            // Convert TCGdex cards to our Card model
            val convertedCards = filteredCards.mapNotNull { tcgdexCard ->
                convertTCGdexCard(tcgdexCard, language)
            }
            
            Log.d("TCGdexService", "✓ Successfully converted ${convertedCards.size} cards")
            convertedCards
        } catch (e: Exception) {
            Log.e("TCGdexService", "❌ Error searching cards: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Convert TCGdex Card response to our app's Card model
     */
    private fun convertTCGdexCard(tcgdexCard: TCGdexCardResponse, language: String): Card? {
        return try {
            Card(
                id = tcgdexCard.id,
                name = tcgdexCard.name,
                supertype = "Pokémon",
                subtypes = listOfNotNull(tcgdexCard.category),
                hp = tcgdexCard.hp,
                types = tcgdexCard.types,
                rarity = tcgdexCard.rarity,
                set = CardSet(
                    id = tcgdexCard.set?.id ?: "",
                    name = tcgdexCard.set?.name ?: "Unknown Set",
                    series = "",
                    total = 0,
                    printedTotal = 0,
                    ptcgoCode = null,
                    releaseDate = tcgdexCard.set?.releaseDate
                ),
                image = CardImage(
                    small = tcgdexCard.image,
                    large = tcgdexCard.image
                ),
                number = tcgdexCard.localId,
                artist = tcgdexCard.illustrator,
                tcgplayer = null
            )
        } catch (e: Exception) {
            Log.e("TCGdexService", "Error converting card: ${e.message}", e)
            null
        }
    }
    
    /**
     * Map Pokemon name to Pokedex number for filtering TCGdex results
     * Returns the Pokedex number as an Int for matching against dexId
     */
    private fun getPokedexNumber(pokemonName: String): Int {
        // Map of Pokemon names to their Pokedex numbers
        val pokedexMap = mapOf(
            "Bulbasaur" to 1, "Ivysaur" to 2, "Venusaur" to 3,
            "Charmander" to 4, "Charmeleon" to 5, "Charizard" to 6,
            "Squirtle" to 7, "Wartortle" to 8, "Blastoise" to 9,
            "Caterpie" to 10, "Metapod" to 11, "Butterfree" to 12,
            "Weedle" to 13, "Kakuna" to 14, "Beedrill" to 15,
            "Pidgey" to 16, "Pidgeotto" to 17, "Pidgeot" to 18,
            "Rattata" to 19, "Raticate" to 20, "Spearow" to 21,
            "Fearow" to 22, "Ekans" to 23, "Arbok" to 24,
            "Pikachu" to 25, "Raichu" to 26, "Sandshrew" to 27,
            "Sandslash" to 28, "Nidoran♀" to 29, "Nidorina" to 30,
            "Nidoqueen" to 31, "Nidoran♂" to 32, "Nidorino" to 33,
            "Nidoking" to 34, "Clefairy" to 35, "Clefable" to 36,
            "Vulpix" to 37, "Ninetales" to 38, "Jigglypuff" to 39,
            "Wigglytuff" to 40, "Zubat" to 41, "Golbat" to 42,
            "Oddish" to 43, "Gloom" to 44, "Vileplume" to 45,
            "Paras" to 46, "Parasect" to 47, "Venonat" to 48,
            "Venomoth" to 49, "Diglett" to 50, "Dugtrio" to 51,
            "Meowth" to 52, "Persian" to 53, "Psyduck" to 54,
            "Golduck" to 55, "Mankey" to 56, "Primeape" to 57,
            "Growlithe" to 58, "Arcanine" to 59, "Poliwag" to 60,
            "Poliwhirl" to 61, "Poliwrath" to 62, "Abra" to 63,
            "Kadabra" to 64, "Alakazam" to 65, "Machop" to 66,
            "Machoke" to 67, "Machamp" to 68, "Bellsprout" to 69,
            "Weepinbell" to 70, "Victreebel" to 71, "Tentacool" to 72,
            "Tentacruel" to 73, "Geodude" to 74, "Graveler" to 75,
            "Golem" to 76, "Ponyta" to 77, "Rapidash" to 78,
            "Slowpoke" to 79, "Slowbro" to 80, "Magnemite" to 81,
            "Magneton" to 82, "Farfetch'd" to 83, "Doduo" to 84,
            "Dodrio" to 85, "Seel" to 86, "Dewgong" to 87,
            "Grimer" to 88, "Muk" to 89, "Shellder" to 90,
            "Cloyster" to 91, "Gastly" to 92, "Haunter" to 93,
            "Gengar" to 94, "Onix" to 95, "Drowzee" to 96,
            "Hypno" to 97, "Krabby" to 98, "Kingler" to 99,
            "Voltorb" to 100, "Electrode" to 101, "Exeggcute" to 102,
            "Exeggutor" to 103, "Cubone" to 104, "Marowak" to 105,
            "Hitmonlee" to 106, "Hitmonchan" to 107, "Lickitung" to 108,
            "Koffing" to 109, "Weezing" to 110, "Rhyhorn" to 111,
            "Rhydon" to 112, "Chansey" to 113, "Tangela" to 114,
            "Kangaskhan" to 115, "Horsea" to 116, "Seadra" to 117,
            "Goldeen" to 118, "Seaking" to 119, "Staryu" to 120,
            "Starmie" to 121, "Mr. Mime" to 122, "Scyther" to 123,
            "Jynx" to 124, "Electabuzz" to 125, "Magmar" to 126,
            "Pinsir" to 127, "Tauros" to 128, "Magikarp" to 129,
            "Gyarados" to 130, "Lapras" to 131, "Ditto" to 132,
            "Eevee" to 133, "Vaporeon" to 134, "Jolteon" to 135,
            "Flareon" to 136, "Porygon" to 137, "Omanyte" to 138,
            "Omastar" to 139, "Kabuto" to 140, "Kabutops" to 141,
            "Aerodactyl" to 142, "Snorlax" to 143, "Articuno" to 144,
            "Zapdos" to 145, "Moltres" to 146, "Dratini" to 147,
            "Dragonair" to 148, "Dragonite" to 149, "Mewtwo" to 150,
            "Mew" to 151
        )
        
        return pokedexMap[pokemonName] ?: 25 // Default to Pikachu if not found
    }
}
