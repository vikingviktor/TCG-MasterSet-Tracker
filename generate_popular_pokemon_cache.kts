#!/usr/bin/env kotlin
/**
 * Popular Pokemon Card Cache Generator (Kotlin Version)
 * Fetches card data for popular Pokemon from TCGdex API and generates a local JSON cache file.
 * 
 * Usage:
 *   kotlinc -script generate_popular_pokemon_cache.kts
 * 
 * Or use in Android Studio project
 */

@file:DependsOn("com.squareup.okhttp3:okhttp:4.11.0")
@file:DependsOn("com.google.code.gson:gson:2.10.1")

import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.File
import java.util.concurrent.TimeUnit

// List of most popular Pokemon to cache
val POPULAR_POKEMON = listOf(
    "Pikachu", "Charizard", "Blastoise", "Venusaur", "Dragonite",
    "Lapras", "Gengar", "Alakazam", "Machamp", "Golem",
    "Arcanine", "Exeggutor", "Marowak", "Hitmonlee", "Hitmonchan",
    "Vileplume", "Bellossom", "Wigglytuff", "Golduck", "Kangaskhan",
    "Rhydon", "Magneton", "Farfetchd", "Dodrio", "Electrode",
    "Cloyster", "Kingler", "Haunter", "Gengar", "Arbok",
    "Weezing", "Victreebel", "Muk", "Jynx", "Porygon",
    "Snorlax", "Articuno", "Zapdos", "Moltres", "Ditto",
    "Mewtwo", "Mew", "Gyarados", "Tentacruel", "Primeape",
    "Seaking", "Goldeen", "Staryu", "Starmie", "Slowbro"
)

val TCGDEX_API_BASE = "https://api.tcgdex.net/v2/en/cards"

fun fetchPokemonCards(pokemonName: String, client: OkHttpClient): List<JsonObject>? {
    return try {
        print("Fetching cards for $pokemonName... ")
        val url = "$TCGDEX_API_BASE?name=$pokemonName"
        val request = Request.Builder().url(url).build()
        
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            println("✗ HTTP ${response.code}")
            return null
        }
        
        val gson = Gson()
        val responseBody = response.body?.string() ?: return null
        val cards = gson.fromJson(responseBody, JsonArray::class.java)
        
        println("✓ (${cards.size()} cards found)")
        cards.map { it.asJsonObject }.toList()
    } catch (e: Exception) {
        println("✗ Error: ${e.message}")
        null
    }
}

fun generateCacheFile(outputPath: String? = null): Boolean {
    val path = outputPath ?: "app/src/main/assets/popular_pokemon.json"
    val outputFile = File(path)
    
    // Ensure directory exists
    outputFile.parentFile?.mkdirs()
    
    println("\n${"=".repeat(60)}")
    println("Popular Pokemon Card Cache Generator (Kotlin)")
    println("${"=".repeat(60)}\n")
    println("Target: $outputFile")
    println("Pokemon to cache: ${POPULAR_POKEMON.size}")
    println("\nFetching card data from TCGdex API...\n")
    
    val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    
    val allCards = mutableMapOf<String, List<JsonObject>>()
    var totalCards = 0
    
    return try {
        for (pokemon in POPULAR_POKEMON) {
            val cards = fetchPokemonCards(pokemon, client)
            if (cards != null && cards.isNotEmpty()) {
                allCards[pokemon] = cards
                totalCards += cards.size
            }
        }
        
        if (allCards.isEmpty()) {
            println("\n❌ Failed to fetch any card data. Please check your internet connection.")
            return false
        }
        
        // Build the cache structure
        val gson = GsonBuilder().setPrettyPrinting().create()
        val cacheObject = JsonObject()
        cacheObject.addProperty("version", "1.0")
        cacheObject.addProperty("generated_at", java.time.LocalDate.now().toString())
        cacheObject.addProperty("description", "Pre-cached popular Pokemon card data for instant app startup")
        cacheObject.addProperty("total_pokemon", allCards.size)
        cacheObject.addProperty("total_cards", totalCards)
        
        val pokemonArray = JsonObject()
        for ((pokemon, cards) in allCards) {
            val cardArray = JsonArray()
            for (card in cards) {
                cardArray.add(card)
            }
            pokemonArray.add(pokemon, cardArray)
        }
        cacheObject.add("pokemon", pokemonArray)
        
        // Write to file
        outputFile.writeText(gson.toJson(cacheObject))
        
        println("\n${"=".repeat(60)}")
        println("✓ Cache file generated successfully!")
        println("${"=".repeat(60)}")
        println("Location: ${outputFile.absolutePath}")
        println("File size: ${String.format("%.2f", outputFile.length() / 1024.0)} KB")
        println("Pokemon cached: ${allCards.size}")
        println("Total cards: $totalCards")
        println("Average cards per Pokemon: ${String.format("%.1f", totalCards / allCards.size.toDouble())}")
        println("\nThe app will use this cache on startup for instant results.")
        println("Run this script anytime to refresh the cache with latest data.\n")
        
        true
    } catch (e: Exception) {
        println("\n❌ Error generating cache file: ${e.message}")
        e.printStackTrace()
        false
    }
}

// Run the generator
generateCacheFile()
