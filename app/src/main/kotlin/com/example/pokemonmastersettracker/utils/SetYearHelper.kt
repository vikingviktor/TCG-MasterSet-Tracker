package com.example.pokemonmastersettracker.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SetYearHelper {
    private var englishSetYears: Map<String, String>? = null
    private var japaneseSetYears: Map<String, String>? = null
    
    fun getSetYear(context: Context, setName: String?, isJapanese: Boolean = false): String? {
        if (setName == null) return null
        
        return try {
            val years = if (isJapanese) {
                getJapaneseSetYears(context)
            } else {
                getEnglishSetYears(context)
            }
            
            years[setName]
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getEnglishSetYears(context: Context): Map<String, String> {
        if (englishSetYears == null) {
            englishSetYears = loadSetYears(context, "set_years_english.json")
        }
        return englishSetYears ?: emptyMap()
    }
    
    private fun getJapaneseSetYears(context: Context): Map<String, String> {
        if (japaneseSetYears == null) {
            japaneseSetYears = loadSetYears(context, "set_years_japanese.json")
        }
        return japaneseSetYears ?: emptyMap()
    }
    
    private fun loadSetYears(context: Context, fileName: String): Map<String, String> {
        return try {
            val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val type = object : TypeToken<Map<String, String>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
