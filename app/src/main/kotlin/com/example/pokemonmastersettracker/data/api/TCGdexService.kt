package com.example.pokemonmastersettracker.data.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.tcgdex.sdk.TCGdex
import net.tcgdex.sdk.model.Card as TCGdexCard
import com.example.pokemonmastersettracker.data.models.Card
import com.example.pokemonmastersettracker.data.models.CardSet

/**
 * Service wrapper for TCGdex API
 * Used specifically for fetching Japanese cards in Favorites section
 */
class TCGdexService {
    
    private val tcgdexEN = TCGdex("en")
    private val tcgdexJA = TCGdex("ja")
    
    /**
     * Search for Pokemon cards by name in specified language
     * @param pokemonName Name of the Pokemon (e.g., "Pikachu")
     * @param language "en" for English or "ja" for Japanese
     * @return List of cards matching the Pokemon name
     */
    suspend fun searchCardsByPokemon(pokemonName: String, language: String = "en"): List<Card> = withContext(Dispatchers.IO) {
        try {
            val tcgdex = if (language == "ja") tcgdexJA else tcgdexEN
            
            Log.d("TCGdexService", "Searching for $pokemonName in language: $language")
            
            // Search for cards
            val cards = tcgdex.findCards(pokemonName)
            
            Log.d("TCGdexService", "Found ${cards.size} cards for $pokemonName")
            
            // Convert TCGdex cards to our Card model
            cards.mapNotNull { tcgdexCard ->
                convertTCGdexCard(tcgdexCard, language)
            }
        } catch (e: Exception) {
            Log.e("TCGdexService", "Error searching cards: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Convert TCGdex Card model to our app's Card model
     */
    private fun convertTCGdexCard(tcgdexCard: TCGdexCard, language: String): Card? {
        return try {
            Card(
                id = tcgdexCard.id ?: return null,
                name = tcgdexCard.name ?: "Unknown",
                supertype = "Pokémon", // TCGdex doesn't have supertype in same format
                subtypes = listOf(tcgdexCard.category ?: ""),
                level = tcgdexCard.level,
                hp = tcgdexCard.hp,
                types = tcgdexCard.types ?: emptyList(),
                evolvesFrom = tcgdexCard.evolveFrom,
                abilities = emptyList(), // TCGdex has different structure
                attacks = emptyList(), // TCGdex has different structure
                weaknesses = emptyList(), // TCGdex has different structure
                resistances = emptyList(), // TCGdex has different structure
                retreatCost = emptyList(), // TCGdex has different structure
                convertedRetreatCost = null,
                set = CardSet(
                    id = tcgdexCard.set?.id ?: "",
                    name = tcgdexCard.set?.name ?: "Unknown Set",
                    series = "",
                    printedTotal = 0,
                    total = 0,
                    legalities = emptyMap(),
                    ptcgoCode = null,
                    releaseDate = tcgdexCard.set?.releaseDate ?: "",
                    updatedAt = "",
                    images = emptyMap()
                ),
                number = tcgdexCard.localId ?: "",
                artist = tcgdexCard.illustrator,
                rarity = tcgdexCard.rarity ?: "",
                flavorText = null,
                nationalPokedexNumbers = listOfNotNull(tcgdexCard.dexId?.firstOrNull()),
                legalities = emptyMap(),
                images = mapOf(
                    "small" to (tcgdexCard.image ?: ""),
                    "large" to (tcgdexCard.image ?: "")
                ),
                tcgplayer = null,
                cardmarket = null,
                language = language
            )
        } catch (e: Exception) {
            Log.e("TCGdexService", "Error converting card: ${e.message}", e)
            null
        }
    }
}
