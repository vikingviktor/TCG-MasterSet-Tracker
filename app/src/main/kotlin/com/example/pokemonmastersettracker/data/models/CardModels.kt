package com.example.pokemonmastersettracker.data.models

import com.google.gson.annotations.SerializedName
import androidx.room.Entity
import androidx.room.PrimaryKey

// API Response Models
data class CardResponse(
    @SerializedName("data")
    val cards: List<Card>
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
    val total: Int?
)

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
