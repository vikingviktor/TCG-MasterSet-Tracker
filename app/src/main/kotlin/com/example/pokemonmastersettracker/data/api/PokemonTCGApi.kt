package com.example.pokemonmastersettracker.data.api

import com.example.pokemonmastersettracker.data.models.CardResponse
import com.example.pokemonmastersettracker.data.models.SetResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokemonTCGApi {
    
    companion object {
        const val BASE_URL = "https://api.pokemontcg.io/v2/"
    }

    /**
     * Get all cards with optional filters
     * @param q Query string for filtering (e.g., "q=name:Pikachu")
     * @param pageSize Number of cards per page
     * @param page Page number for pagination
     */
    @GET("cards")
    suspend fun searchCards(
        @Query("q") query: String? = null,
        @Query("pageSize") pageSize: Int = 50,
        @Query("page") page: Int = 1
    ): CardResponse

    /**
     * Get cards by pokemon name
     */
    @GET("cards")
    suspend fun getCardsByPokemonName(
        @Query("q") query: String,
        @Query("pageSize") pageSize: Int = 250
    ): CardResponse

    /**
     * Get cards from a specific set
     */
    @GET("cards")
    suspend fun getCardsBySet(
        @Query("q") query: String,
        @Query("pageSize") pageSize: Int = 250
    ): CardResponse

    /**
     * Get card details by ID
     */
    @GET("cards/{cardId}")
    suspend fun getCardById(
        @Path("cardId") cardId: String
    ): CardResponse

    /**
     * Get all sets with optional filters
     */
    @GET("sets")
    suspend fun getSets(
        @Query("pageSize") pageSize: Int = 250,
        @Query("page") page: Int = 1
    ): SetResponse

    /**
     * Get set details by ID
     */
    @GET("sets/{setId}")
    suspend fun getSetById(
        @Path("setId") setId: String
    ): SetResponse

    /**
     * Search for English cards of a specific Pokemon
     */
    @GET("cards")
    suspend fun getPokemonEnglishCards(
        @Query("q") query: String = "name:*",
        @Query("pageSize") pageSize: Int = 250
    ): CardResponse

    /**
     * Search for Japanese cards of a specific Pokemon
     */
    @GET("cards")
    suspend fun getPokemonJapaneseCards(
        @Query("q") query: String = "name:*",
        @Query("pageSize") pageSize: Int = 250
    ): CardResponse
}
