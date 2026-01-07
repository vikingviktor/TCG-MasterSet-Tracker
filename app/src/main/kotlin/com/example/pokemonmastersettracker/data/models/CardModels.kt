package com.example.pokemonmastersettracker.data.models

import com.google.gson.annotations.SerializedName
import androidx.room.Entity
import androidx.room.PrimaryKey

// Pokemon Entity for local search
@Entity(tableName = "pokemon")
data class Pokemon(
    @PrimaryKey
    val name: String,
    val nationalPokedexNumber: Int? = null,  // For multi-language support
    val imageUrl: String? = null,
    val isFavorite: Boolean = false,
    @androidx.room.Ignore
    val ownedCount: Int = 0,  // Not stored in DB, calculated at runtime
    @androidx.room.Ignore
    val totalCards: Int = 0   // Not stored in DB, loaded from FavoritePokemon
) {
    // Secondary constructor for Room (without @Ignore fields)
    constructor(name: String, nationalPokedexNumber: Int?, imageUrl: String?, isFavorite: Boolean) :
        this(name, nationalPokedexNumber, imageUrl, isFavorite, 0, 0)
}

// API Response Models
data class CardResponse(
    @SerializedName("data")
    val cards: List<Card>,
    @SerializedName("page")
    val page: Int = 1,
    @SerializedName("pageSize")
    val pageSize: Int = 250,
    @SerializedName("count")
    val count: Int = 0,
    @SerializedName("totalCount")
    val totalCount: Int = 0
)

@Entity(tableName = "cards")
data class Card(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("supertype")
    val supertype: String,
    
    @SerializedName("subtypes")
    val subtypes: List<String>?,
    
    @SerializedName("hp")
    val hp: String?,
    
    @SerializedName("types")
    val types: List<String>?,
    
    @SerializedName("rarity")
    val rarity: String?,
    
    @SerializedName("set")
    val set: CardSet?,
    
    @SerializedName("images")
    val image: CardImage?,
    
    @SerializedName("number")
    val number: String?,
    
    @SerializedName("artist")
    val artist: String?,
    
    @SerializedName("tcgplayer")
    val tcgplayer: TCGPlayerData?
) {
    companion object {
        // For Room, we need a no-arg constructor
        fun createInstance(
            id: String,
            name: String,
            supertype: String,
            subtypes: List<String>? = null,
            hp: String? = null,
            types: List<String>? = null,
            rarity: String? = null,
            set: CardSet? = null,
            image: CardImage? = null,
            number: String? = null,
            artist: String? = null,
            tcgplayer: TCGPlayerData? = null
        ) = Card(id, name, supertype, subtypes, hp, types, rarity, set, image, number, artist, tcgplayer)
    }
}

data class CardImage(
    @SerializedName("small")
    val small: String?,
    
    @SerializedName("large")
    val large: String?
)

data class CardSet(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("series")
    val series: String?,
    
    @SerializedName("total")
    val total: Int?,
    
    @SerializedName("printedTotal")
    val printedTotal: Int? = null,
    
    @SerializedName("ptcgoCode")
    val ptcgoCode: String? = null,
    
    @SerializedName("releaseDate")
    val releaseDate: String? = null
) {
    // Helper to determine if this is a Japanese set
    // According to Pokemon TCG API documentation:
    // - Japanese sets have Japanese characters in name/series
    // - Japanese set IDs follow patterns like: sm12a, sm11b, xy12a, etc. (no language suffix)
    // - English/International sets often have different ID patterns
    // - Set names in Japanese use Hiragana/Katakana/Kanji
    val isJapanese: Boolean
        get() {
            // Check for Japanese characters in name or series
            val hasJapaneseText = name?.let { containsJapanese(it) } == true ||
                                  series?.let { containsJapanese(it) } == true
            
            // Also check for common Japanese set ID patterns
            // Japanese sets from Sun & Moon era: sm1-sm12 with letters (sm12a)
            // Japanese sets from XY era: xy1-xy12 with letters
            // Japanese sets from Sword & Shield: s1-s12 with letters
            val japaneseIdPattern = Regex("^(sm|xy|s|bw)\\d+[a-z]$", RegexOption.IGNORE_CASE)
            val hasJapaneseIdPattern = japaneseIdPattern.matches(id)
            
            return hasJapaneseText || hasJapaneseIdPattern
        }
    
    private fun containsJapanese(text: String): Boolean {
        // Check for Japanese characters (Hiragana, Katakana, Kanji)
        return text.any { char ->
            char in '\u3040'..'\u309F' || // Hiragana
            char in '\u30A0'..'\u30FF' || // Katakana  
            char in '\u4E00'..'\u9FAF'    // Kanji
        }
    }
}

data class TCGPlayerData(
    @SerializedName("url")
    val url: String?,
    
    @SerializedName("updatedAt")
    val updatedAt: String?,
    
    @SerializedName("prices")
    val prices: Map<String, PriceData>?
)

data class PriceData(
    @SerializedName("low")
    val low: Double?,
    
    @SerializedName("mid")
    val mid: Double?,
    
    @SerializedName("high")
    val high: Double?,
    
    @SerializedName("market")
    val market: Double?,
    
    @SerializedName("directLow")
    val directLow: Double?
)

data class SetResponse(
    @SerializedName("data")
    val sets: List<PokemonSet>
)

data class PokemonSet(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("series")
    val series: String,
    
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("printedTotal")
    val printedTotal: Int,
    
    @SerializedName("language")
    val language: String?,
    
    @SerializedName("images")
    val images: SetImages?
)

data class SetImages(
    @SerializedName("logo")
    val logo: String?,
    
    @SerializedName("symbol")
    val symbol: String?
)
