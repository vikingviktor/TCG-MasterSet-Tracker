package com.example.pokemonmastersettracker.data.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Interceptor
import com.example.pokemonmastersettracker.data.models.PriceData
import com.google.gson.annotations.SerializedName

/**
 * PokeWallet.io API - Provides pricing data for Pokemon cards
 * API Key: pk_test_e14c199ca02fac83151cb25f841ed8688dcd0541ff43df04
 */
interface PokeWalletApi {
    /**
     * Search cards by name or other criteria
     * Example: /search?q=charizard
     */
    @GET("search")
    suspend fun searchCards(
        @Query("q") query: String
    ): PokeWalletSearchResponse
    
    /**
     * Get specific card by ID
     * Example: /cards/pk_xxx
     */
    @GET("cards/{cardId}")
    suspend fun getCard(
        @Path("cardId") cardId: String
    ): PokeWalletCard
}

/**
 * Search response from PokeWallet
 */
data class PokeWalletSearchResponse(
    @SerializedName("data")
    val data: List<PokeWalletSearchResult>?,
    
    @SerializedName("count")
    val count: Int?,
    
    @SerializedName("page")
    val page: Int?,
    
    @SerializedName("total_pages")
    val totalPages: Int?
)

data class PokeWalletSearchResult(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("card_info")
    val cardInfo: PokeWalletCardInfo
)

data class PokeWalletCardInfo(
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("clean_name")
    val cleanName: String?,
    
    @SerializedName("set_name")
    val setName: String?,
    
    @SerializedName("set_code")
    val setCode: String?,
    
    @SerializedName("card_number")
    val cardNumber: String?,
    
    @SerializedName("rarity")
    val rarity: String?
)

/**
 * Full card details from PokeWallet
 */
data class PokeWalletCard(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("card_info")
    val cardInfo: PokeWalletCardInfo,
    
    @SerializedName("tcgplayer")
    val tcgplayer: PokeWalletTCGPlayer?,
    
    @SerializedName("cardmarket")
    val cardmarket: PokeWalletCardMarket?
)

data class PokeWalletTCGPlayer(
    @SerializedName("url")
    val url: String?,
    
    @SerializedName("prices")
    val prices: List<PokeWalletPricing>?
)

data class PokeWalletCardMarket(
    @SerializedName("product_name")
    val productName: String?,
    
    @SerializedName("product_url")
    val productUrl: String?,
    
    @SerializedName("prices")
    val prices: List<PokeWalletCMPricing>?
)

data class PokeWalletPricing(
    @SerializedName("low_price")
    val lowPrice: Double?,
    
    @SerializedName("mid_price")
    val midPrice: Double?,
    
    @SerializedName("high_price")
    val highPrice: Double?,
    
    @SerializedName("market_price")
    val marketPrice: Double?,
    
    @SerializedName("direct_low_price")
    val directLowPrice: Double?,
    
    @SerializedName("updated_at")
    val updatedAt: String?
)

data class PokeWalletCMPricing(
    @SerializedName("variant_type")
    val variantType: String?,
    
    @SerializedName("avg")
    val avg: Double?,
    
    @SerializedName("low")
    val low: Double?,
    
    @SerializedName("trend")
    val trend: Double?,
    
    @SerializedName("avg1")
    val avg1: Double?,
    
    @SerializedName("avg7")
    val avg7: Double?,
    
    @SerializedName("avg30")
    val avg30: Double?,
    
    @SerializedName("updated_at")
    val updatedAt: String?
)

/**
 * Service wrapper for PokeWallet.io API
 * Used to fetch pricing data when TCGdex doesn't have it
 */
class PokeWalletService {
    
    companion object {
        private const val API_KEY = "pk_test_e14c199ca02fac83151cb25f841ed8688dcd0541ff43df04"
        private const val TAG = "PokeWalletService"
    }
    
    private val api: PokeWalletApi = Retrofit.Builder()
        .baseUrl("https://api.pokewallet.io/")
        .client(
            OkHttpClient.Builder()
                .addInterceptor(Interceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("X-API-Key", API_KEY)
                        .build()
                    chain.proceed(request)
                })
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PokeWalletApi::class.java)
    
    /**
     * Search for a card by name and set
     * Returns the first matching result
     */
    suspend fun searchCard(cardName: String, setName: String? = null): PokeWalletSearchResult? = withContext(Dispatchers.IO) {
        return@withContext try {
            val query = if (setName != null) {
                "$cardName $setName"
            } else {
                cardName
            }
            
            Log.d(TAG, "🔍 Searching PokeWallet for: $query")
            val response = api.searchCards(query)
            val result = response.data?.firstOrNull()
            
            if (result != null) {
                Log.d(TAG, "✓ Found card: ${result.cardInfo.name} from ${result.cardInfo.setName}")
            } else {
                Log.d(TAG, "⚠ No results found for: $query")
            }
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ Search failed: ${e.message}", e)
            null
        }
    }
    
    /**
     * Get full card details including pricing
     */
    suspend fun getCardDetails(cardId: String): PokeWalletCard? = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "📡 Fetching card details for: $cardId")
            val card = api.getCard(cardId)
            Log.d(TAG, "✓ Loaded pricing for: ${card.cardInfo.name}")
            card
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to get card details: ${e.message}", e)
            null
        }
    }
    
    /**
     * Fetch pricing for a card by searching and then getting details
     */
    suspend fun fetchPricing(cardName: String, setName: String?, cardNumber: String? = null): Map<String, PriceData>? = withContext(Dispatchers.IO) {
        return@withContext try {
            // Search for the card
            val searchResult = searchCard(cardName, setName) ?: return@withContext null
            
            // Verify card number matches if provided
            if (cardNumber != null && searchResult.cardInfo.cardNumber != null) {
                val normalizedSearch = searchResult.cardInfo.cardNumber!!.replace("/", "").trim()
                val normalizedProvided = cardNumber.replace("/", "").trim()
                if (!normalizedSearch.contains(normalizedProvided) && !normalizedProvided.contains(normalizedSearch)) {
                    Log.d(TAG, "⚠ Card number mismatch: ${searchResult.cardInfo.cardNumber} vs $cardNumber")
                    return@withContext null
                }
            }
            
            // Get full details with pricing
            val cardDetails = getCardDetails(searchResult.id) ?: return@withContext null
            
            // Convert PokeWallet pricing to our PriceData format
            val prices = mutableMapOf<String, PriceData>()
            
            // Add TCGPlayer pricing if available
            cardDetails.tcgplayer?.prices?.firstOrNull()?.let { tcgPrice ->
                prices["normal"] = PriceData(
                    low = tcgPrice.lowPrice,
                    mid = tcgPrice.midPrice,
                    high = tcgPrice.highPrice,
                    market = tcgPrice.marketPrice,
                    directLow = tcgPrice.directLowPrice
                )
            }
            
            Log.d(TAG, "✓ Successfully fetched ${prices.size} price variants")
            if (prices.isNotEmpty()) prices else null
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to fetch pricing: ${e.message}", e)
            null
        }
    }
}
