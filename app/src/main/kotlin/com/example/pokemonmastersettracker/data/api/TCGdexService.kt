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
            Log.d("TCGdexService", "Searching for $pokemonName in language: $language")
            
            // Search for cards via REST API
            val response = api.searchCards(language, pokemonName)
            
            Log.d("TCGdexService", "Found ${response.size} cards for $pokemonName")
            
            // Convert TCGdex cards to our Card model
            response.mapNotNull { tcgdexCard ->
                convertTCGdexCard(tcgdexCard, language)
            }
        } catch (e: Exception) {
            Log.e("TCGdexService", "Error searching cards: ${e.message}", e)
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
}