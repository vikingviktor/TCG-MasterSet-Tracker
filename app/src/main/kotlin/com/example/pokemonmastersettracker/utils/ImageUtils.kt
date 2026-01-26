package com.example.pokemonmastersettracker.utils

import android.content.Context
import coil.request.ImageRequest

/**
 * Get the appropriate image source for a Pokemon.
 * For Gen 1 Pokemon (#1-151), attempts to use local sprite assets first.
 * Falls back to remote URL if local sprite doesn't exist or for other Pokemon.
 */
fun getPokemonImageModel(
    context: Context,
    nationalPokedexNumber: Int?,
    remoteImageUrl: String?
): Any? {
    // For Gen 1 Pokemon (1-151), try to use local sprite
    if (nationalPokedexNumber != null && nationalPokedexNumber in 1..151) {
        val spriteFileName = String.format("%03d.png", nationalPokedexNumber)
        val assetPath = "file:///android_asset/sprites/$spriteFileName"
        
        return ImageRequest.Builder(context)
            .data(assetPath)
            .error(remoteImageUrl) // Fallback to remote URL if local sprite not found
            .build()
    }
    
    // For other Pokemon or if no dex number, use remote URL
    return remoteImageUrl
}

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
