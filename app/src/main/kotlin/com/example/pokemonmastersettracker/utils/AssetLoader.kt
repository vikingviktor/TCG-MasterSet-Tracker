package com.example.pokemonmastersettracker.utils

import android.content.Context
import android.util.Log
import com.example.pokemonmastersettracker.data.models.Card
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utility for loading and parsing cached data from app assets
 */
object AssetLoader {
    private const val TAG = "AssetLoader"
    private const val ASSET_FILE_NAME = "popular_pokemon.json"

    /**
     * Check if the asset file exists
     */
    fun assetFileExists(context: Context): Boolean {
        return try {
            context.assets.open(ASSET_FILE_NAME).use { true }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Asset file '$ASSET_FILE_NAME' NOT FOUND: ${e.message}")
            false
        }
    }

    /**
     * Load popular Pokemon cards from the bundled JSON asset file
     * 
     * @param context Android context for accessing assets
     * @param pokemonName Name of the Pokemon to load cards for
     * @return List of Card objects, or empty list if not found or error
     */
    suspend fun loadPopularPokemonCards(
        context: Context,
        pokemonName: String
    ): List<Card> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "🔍 Attempting to load cached cards for: '$pokemonName'")
            
            // Check if asset file exists first
            if (!assetFileExists(context)) {
                Log.e(TAG, "❌ Asset file not found!")
                return@withContext emptyList()
            }
            
            Log.d(TAG, "✓ Asset file found, opening...")
            val gson = Gson()
            
            // Load the asset file
            val assetManager = context.assets
            Log.d(TAG, "📂 Opening assets...")
            val inputStream = assetManager.open(ASSET_FILE_NAME)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            Log.d(TAG, "✓ Loaded asset file (${jsonString.length} bytes)")
            
            // Parse the JSON structure
            Log.d(TAG, "📊 Parsing JSON structure...")
            val cacheJson = gson.fromJson(jsonString, JsonObject::class.java)
            val pokemonMap = cacheJson.getAsJsonObject("pokemon")
            
            Log.d(TAG, "📋 Available Pokemon in cache: ${pokemonMap.keySet().size} total")
            
            if (pokemonMap.has(pokemonName)) {
                Log.d(TAG, "✓✓✓ FOUND '$pokemonName' in cache! ✓✓✓")
                val cardsArray = pokemonMap.getAsJsonArray(pokemonName)
                val cards = mutableListOf<Card>()
                
                Log.d(TAG, "📦 Parsing ${cardsArray.size()} cards for $pokemonName...")
                for ((index, cardElement) in cardsArray.withIndex()) {
                    try {
                        val cardJson = cardElement.asJsonObject
                        // Convert API response to Card object
                        val card = convertTCGdexToCard(cardJson, gson)
                        if (card != null) {
                            cards.add(card)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "⚠️ Error parsing card #$index from cache: ${e.message}")
                    }
                }
                
                Log.d(TAG, "✅✅✅ Successfully loaded ${cards.size} cached cards for $pokemonName ✅✅✅")
                cards
            } else {
                Log.w(TAG, "❌ Pokemon '$pokemonName' NOT found in cache")
                Log.d(TAG, "First 10 available Pokemon: ${pokemonMap.keySet().take(10)}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌❌❌ CRITICAL ERROR loading popular_pokemon.json: ${e.message}", e)
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Convert TCGdex API JSON response to Card object
     * This mirrors the conversion logic in TCGdexService
     */
    private fun convertTCGdexToCard(cardJson: JsonObject, gson: Gson): Card? {
        return try {
            val name = cardJson.get("name")?.asString ?: return null
            val id = cardJson.get("id")?.asString ?: return null
            
            Card(
                id = id,
                name = name,
                supertype = "Pokémon",
                subtypes = cardJson.get("category")?.asString?.let { listOf(it) },
                hp = cardJson.get("hp")?.asString,
                types = cardJson.getAsJsonArray("types")?.let {
                    it.map { e -> e.asString }
                },
                rarity = cardJson.get("rarity")?.asString,
                set = cardJson.getAsJsonObject("set")?.let { setObj ->
                    com.example.pokemonmastersettracker.data.models.CardSet(
                        id = setObj.get("id")?.asString ?: "",
                        name = setObj.get("name")?.asString,
                        series = "",
                        total = setObj.getAsJsonObject("cardCount")?.get("total")?.asInt,
                        printedTotal = setObj.getAsJsonObject("cardCount")?.get("official")?.asInt,
                        ptcgoCode = null,
                        releaseDate = setObj.get("releaseDate")?.asString
                    )
                },
                image = cardJson.get("image")?.asString?.let { baseUrl ->
                    com.example.pokemonmastersettracker.data.models.CardImage(
                        small = "$baseUrl/low.webp",
                        large = "$baseUrl/high.webp"
                    )
                },
                number = cardJson.get("localId")?.asString,
                artist = cardJson.get("illustrator")?.asString,
                tcgplayer = cardJson.getAsJsonObject("pricing")?.getAsJsonObject("tcgplayer")?.let { pricing ->
                    com.example.pokemonmastersettracker.data.models.TCGPlayerData(
                        url = null,
                        updatedAt = pricing.get("updated")?.asString,
                        prices = mapOf(
                            "normal" to com.example.pokemonmastersettracker.data.models.PriceData(
                                low = pricing.getAsJsonObject("normal")?.get("lowPrice")?.asDouble,
                                mid = pricing.getAsJsonObject("normal")?.get("midPrice")?.asDouble,
                                high = pricing.getAsJsonObject("normal")?.get("highPrice")?.asDouble,
                                market = pricing.getAsJsonObject("normal")?.get("marketPrice")?.asDouble,
                                directLow = pricing.getAsJsonObject("normal")?.get("directLowPrice")?.asDouble
                            ),
                            "reverse" to com.example.pokemonmastersettracker.data.models.PriceData(
                                low = pricing.getAsJsonObject("reverse")?.get("lowPrice")?.asDouble,
                                mid = pricing.getAsJsonObject("reverse")?.get("midPrice")?.asDouble,
                                high = pricing.getAsJsonObject("reverse")?.get("highPrice")?.asDouble,
                                market = pricing.getAsJsonObject("reverse")?.get("marketPrice")?.asDouble,
                                directLow = pricing.getAsJsonObject("reverse")?.get("directLowPrice")?.asDouble
                            )
                        ).filterValues { it.mid != null || it.market != null }
                    )
                },
                cardmarket = cardJson.getAsJsonObject("pricing")?.getAsJsonObject("cardmarket")?.let { pricing ->
                    com.example.pokemonmastersettracker.data.models.CardMarketData(
                        updated = pricing.get("updated")?.asString,
                        unit = pricing.get("unit")?.asString,
                        avg = pricing.get("avg")?.asDouble,
                        low = pricing.get("low")?.asDouble,
                        trend = pricing.get("trend")?.asDouble
                    )
                },
                variants = cardJson.getAsJsonObject("variants")?.let { v ->
                    com.example.pokemonmastersettracker.data.models.CardVariants(
                        firstEdition = v.get("firstEdition")?.asBoolean ?: false,
                        holo = v.get("holo")?.asBoolean ?: false,
                        normal = v.get("normal")?.asBoolean ?: false,
                        reverse = v.get("reverse")?.asBoolean ?: false,
                        wPromo = v.get("wPromo")?.asBoolean ?: false
                    )
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error converting card: ${e.message}", e)
            null
        }
    }
}
