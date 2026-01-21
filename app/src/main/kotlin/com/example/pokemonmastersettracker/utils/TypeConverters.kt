package com.example.pokemonmastersettracker.utils

import androidx.room.TypeConverter
import com.example.pokemonmastersettracker.data.models.CardImage
import com.example.pokemonmastersettracker.data.models.CardMarketData
import com.example.pokemonmastersettracker.data.models.TCGPlayerData
import com.example.pokemonmastersettracker.data.models.PriceData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object CardImageTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromCardImage(value: CardImage?): String? {
        return if (value == null) null else gson.toJson(value)
    }

    @TypeConverter
    fun toCardImage(value: String?): CardImage? {
        return if (value == null) null else gson.fromJson(value, CardImage::class.java)
    }
}

object TCGPlayerDataTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromTCGPlayerData(value: TCGPlayerData?): String? {
        return if (value == null) null else gson.toJson(value)
    }

    @TypeConverter
    fun toTCGPlayerData(value: String?): TCGPlayerData? {
        return if (value == null) null else gson.fromJson(value, TCGPlayerData::class.java)
    }
}

object CardMarketDataTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromCardMarketData(value: CardMarketData?): String? {
        return if (value == null) null else gson.toJson(value)
    }

    @TypeConverter
    fun toCardMarketData(value: String?): CardMarketData? {
        return if (value == null) null else gson.fromJson(value, CardMarketData::class.java)
    }
}

object PriceDataTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromPriceDataMap(value: Map<String, PriceData>?): String? {
        return if (value == null) null else gson.toJson(value)
    }

    @TypeConverter
    fun toPriceDataMap(value: String?): Map<String, PriceData>? {
        return if (value == null) null else {
            val type = object : TypeToken<Map<String, PriceData>>() {}.type
            gson.fromJson(value, type)
        }
    }
}

object StringListTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return if (value == null) null else gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return if (value == null) null else {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(value, type)
        }
    }
}
