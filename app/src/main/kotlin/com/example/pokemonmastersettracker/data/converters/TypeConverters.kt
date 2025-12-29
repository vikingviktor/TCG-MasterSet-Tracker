package com.example.pokemonmastersettracker.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.example.pokemonmastersettracker.data.models.CardImage
import com.example.pokemonmastersettracker.data.models.TCGPlayerData
import com.example.pokemonmastersettracker.data.models.PriceData

class TypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return if (value == null) null else gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return if (value == null) null else gson.fromJson(value, Array<String>::class.java).toList()
    }

    @TypeConverter
    fun fromCardImage(value: CardImage?): String? {
        return if (value == null) null else gson.toJson(value)
    }

    @TypeConverter
    fun toCardImage(value: String?): CardImage? {
        return if (value == null) null else gson.fromJson(value, CardImage::class.java)
    }

    @TypeConverter
    fun fromTCGPlayerData(value: TCGPlayerData?): String? {
        return if (value == null) null else gson.toJson(value)
    }

    @TypeConverter
    fun toTCGPlayerData(value: String?): TCGPlayerData? {
        return if (value == null) null else gson.fromJson(value, TCGPlayerData::class.java)
    }

    @TypeConverter
    fun fromPriceDataMap(value: Map<String, PriceData>?): String? {
        return if (value == null) null else gson.toJson(value)
    }

    @TypeConverter
    fun toPriceDataMap(value: String?): Map<String, PriceData>? {
        return if (value == null) null else gson.fromJson(value, Map::class.java) as? Map<String, PriceData>
    }
}
