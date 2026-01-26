package com.example.pokemonmastersettracker.utils

/**
 * Get local sprite path for Gen 1 Pokemon (#1-151)
 * Returns null if not a Gen 1 Pokemon
 */
fun getLocalSpritePath(nationalPokedexNumber: Int?): String? {
    return if (nationalPokedexNumber != null && nationalPokedexNumber in 1..151) {
        val spriteFileName = String.format("%03d.png", nationalPokedexNumber)
        "file:///android_asset/sprites/$spriteFileName"
    } else {
        null
    }
}
