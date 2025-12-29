# Pokemon TCG API Integration Guide

## Overview

This app uses the **PokemonTCG.io** API - a free, publicly available API with comprehensive card data covering the entire Pokemon Trading Card Game history.

**Base URL:** `https://api.pokemontcg.io/v2/`

**Authentication:** None required (public API)

## API Endpoints Used

### 1. Card Search & Retrieval

#### Search Cards
```
GET /cards?q={query}&pageSize={size}&page={number}
```

**Parameters:**
- `q` (optional): Query string for filtering
- `pageSize` (optional): Number of results per page (max 250)
- `page` (optional): Page number for pagination

**Example Requests:**

```bash
# Find all Pikachu cards
curl "https://api.pokemontcg.io/v2/cards?q=name:Pikachu"

# Find English Pikachu cards only
curl "https://api.pokemontcg.io/v2/cards?q=name:Pikachu language:en"

# Find Japanese Pikachu cards
curl "https://api.pokemontcg.io/v2/cards?q=name:Pikachu language:ja"

# Get 50 cards per page
curl "https://api.pokemontcg.io/v2/cards?q=name:Pikachu&pageSize=50&page=1"

# Find cards from specific set
curl "https://api.pokemontcg.io/v2/cards?q=set.id:sv04pt"

# Find Holographic Pikachu
curl "https://api.pokemontcg.io/v2/cards?q=name:Pikachu rarity:Holo"
```

#### Get Single Card
```
GET /cards/{cardId}
```

**Example:**
```bash
curl "https://api.pokemontcg.io/v2/cards/sv04pt-1"
```

### 2. Set Information

#### List All Sets
```
GET /sets?pageSize={size}&page={number}
```

**Example:**
```bash
curl "https://api.pokemontcg.io/v2/sets?pageSize=250"
```

#### Get Single Set
```
GET /sets/{setId}
```

**Example:**
```bash
curl "https://api.pokemontcg.io/v2/sets/sv04pt"
```

## Query Operators & Filters

### Supported Fields for Query

| Field | Type | Example |
|-------|------|---------|
| `name` | string | `name:Pikachu` |
| `id` | string | `id:sv04pt-1` |
| `language` | string | `language:en` or `language:ja` |
| `set.id` | string | `set.id:sv04pt` |
| `types` | array | `types:Electric` |
| `rarity` | string | `rarity:Holo` |
| `hp` | number | `hp:[40 TO 100]` |
| `supertype` | string | `supertype:Pokémon` |
| `number` | string | `number:25` |

### Query Examples

```
# Fire-type Pokémon
q=types:Fire

# Rare Holographic cards
q=rarity:Holo

# Cards with 100+ HP
q=hp:[100 TO *]

# Trainer cards
q=supertype:Trainer

# Combinations (AND logic)
q=name:Charizard language:en set.id:sv04pt

# Multiple values (OR logic)
q=name:Pikachu OR name:Charizard
```

## API Response Format

### Card Response Structure

```json
{
  "data": [
    {
      "id": "sv04pt-1",
      "name": "Pikachu",
      "supertype": "Pokémon",
      "subtypes": ["Basic"],
      "hp": "45",
      "types": ["Electric"],
      "rarity": "Common",
      "set": {
        "id": "sv04pt",
        "name": "Scarlet & Violet"
      },
      "number": "25",
      "artist": "Ken Sugimori",
      "image": {
        "small": "https://images.pokemontcg.io/sv04pt/1.png",
        "large": "https://images.pokemontcg.io/sv04pt/1_hires.png"
      },
      "tcgplayer": {
        "url": "https://www.tcgplayer.com/product/...",
        "prices": {
          "normal": {
            "low": 0.25,
            "mid": 0.50,
            "high": 1.50,
            "market": 0.45
          },
          "holofoil": {
            "low": 5.00,
            "mid": 10.00,
            "high": 25.00,
            "market": 12.50
          }
        }
      }
    }
  ],
  "page": 1,
  "pageSize": 250,
  "count": 250,
  "totalCount": 500
}
```

### Set Response Structure

```json
{
  "data": [
    {
      "id": "sv04pt",
      "name": "Scarlet & Violet",
      "series": "Scarlet & Violet",
      "total": 200,
      "printedTotal": 198,
      "language": "en",
      "images": {
        "logo": "https://images.pokemontcg.io/sv04pt/logo.png",
        "symbol": "https://images.pokemontcg.io/sv04pt/symbol.png"
      }
    }
  ]
}
```

## Implementation in App

### Retrofit API Client

```kotlin
interface PokemonTCGApi {
    @GET("cards")
    suspend fun searchCards(
        @Query("q") query: String? = null,
        @Query("pageSize") pageSize: Int = 250,
        @Query("page") page: Int = 1
    ): CardResponse

    @GET("cards/{cardId}")
    suspend fun getCardById(
        @Path("cardId") cardId: String
    ): CardResponse

    @GET("sets")
    suspend fun getSets(
        @Query("pageSize") pageSize: Int = 250,
        @Query("page") page: Int = 1
    ): SetResponse
}
```

### Using the API in Repository

```kotlin
// Search cards
suspend fun searchPokemonCards(pokemonName: String, language: String = "en"): List<Card> {
    val query = buildCardQuery(pokemonName, language)
    val response = api.searchCards(query = query)
    cardDao.insertCards(response.cards)
    return response.cards
}

// Get specific card
suspend fun getCardById(cardId: String): Card? {
    return try {
        val response = api.getCardById(cardId)
        response.cards.firstOrNull()?.let { card ->
            cardDao.insertCard(card)
            card
        }
    } catch (e: Exception) {
        cardDao.getCardById(cardId)
    }
}
```

### Using in ViewModel

```kotlin
fun searchPokemonCards(pokemonName: String, language: String = "en") {
    viewModelScope.launch {
        _cardUiState.value = CardUiState(loading = true)
        try {
            val cards = repository.searchPokemonCards(pokemonName, language)
            _cardUiState.value = CardUiState(cards = cards)
        } catch (e: Exception) {
            _cardUiState.value = CardUiState(error = e.message)
        }
    }
}
```

## Query Building Patterns

### English Cards Only
```kotlin
val query = "name:$pokemonName language:en"
```

### Japanese Cards Only
```kotlin
val query = "name:$pokemonName language:ja"
```

### Specific Set
```kotlin
val query = "name:$pokemonName set.id:$setId"
```

### By Type
```kotlin
val query = "types:Electric"
```

### Rarity Filter
```kotlin
val query = "rarity:Holo"
```

### HP Range
```kotlin
val query = "hp:[100 TO 200]"
```

## Price Information

### TCGPlayer Pricing Fields

```json
{
  "normal": {
    "low": 0.25,        // Lowest current listing
    "mid": 0.50,        // Market middle
    "high": 1.50,       // Highest listing
    "market": 0.45      // Market average
  },
  "holofoil": {
    "low": 5.00,
    "mid": 10.00,
    "high": 25.00,
    "market": 12.50
  },
  "reverseHolofoil": {
    "low": 1.00,
    "mid": 2.00,
    "high": 5.00,
    "market": 2.50
  }
}
```

### Price by Condition

The app maps card conditions to pricing variants:

```
Mint → normal prices
Near Mint → holofoil prices (usually 5-10x more)
Lightly Played → reverseHolofoil prices
Moderately Played → Lower than normal
Heavily Played → Significant discount
Damaged → Further discount
```

## Pagination

For large result sets, implement pagination:

```kotlin
suspend fun searchCardsWithPagination(
    pokemonName: String,
    pageSize: Int = 100,
    page: Int = 1
): CardResponse {
    val query = "name:$pokemonName"
    return api.searchCards(
        query = query,
        pageSize = pageSize,
        page = page
    )
}
```

**Response includes pagination info:**
- `page`: Current page number
- `pageSize`: Results per page
- `count`: Results on this page
- `totalCount`: Total available results

## Error Handling

### Common HTTP Status Codes

| Status | Meaning | Handling |
|--------|---------|----------|
| 200 | Success | Process response |
| 400 | Bad Request | Check query syntax |
| 404 | Not Found | No results or invalid ID |
| 429 | Rate Limited | Implement retry with backoff |
| 500 | Server Error | Retry or show error message |

### Implementation

```kotlin
try {
    val response = api.searchCards(query)
    // Process successful response
    cardDao.insertCards(response.cards)
} catch (e: HttpException) {
    when (e.code()) {
        400 -> Log.e("API", "Invalid query")
        404 -> Log.e("API", "No results found")
        429 -> Log.w("API", "Rate limited, retry later")
        else -> Log.e("API", "Server error: ${e.code()}")
    }
} catch (e: Exception) {
    Log.e("API", "Network error: ${e.message}")
}
```

## Rate Limiting

The free API has rate limiting:
- **Limit:** ~1000 requests per hour
- **Per IP:** Shared across all users
- **Recommendation:** Cache results, implement smart pagination

**Best Practices:**
1. Cache all card data locally in Room
2. Don't re-fetch data unnecessarily
3. Implement exponential backoff for retries
4. Use pagination to limit per-request data
5. Consider request debouncing

## Image URLs

### Image Structure

Cards have two image URLs:
```json
{
  "image": {
    "small": "https://images.pokemontcg.io/{set}/{number}.png",
    "large": "https://images.pokemontcg.io/{set}/{number}_hires.png"
  }
}
```

### Image Best Practices

1. Store URLs in database (not images)
2. Use Coil for lazy loading and caching
3. Load thumbnails for lists
4. Load full-size for details
5. Provide fallback UI if images unavailable

## Testing API Calls

### Using curl

```bash
# Test basic search
curl "https://api.pokemontcg.io/v2/cards?q=name:Pikachu" | jq

# Test with pageSize
curl "https://api.pokemontcg.io/v2/cards?q=name:Pikachu&pageSize=10" | jq

# Test specific card
curl "https://api.pokemontcg.io/v2/cards/sv04pt-1" | jq
```

### Using Postman

1. Open Postman
2. Create GET request
3. URL: `https://api.pokemontcg.io/v2/cards`
4. Params:
   - Key: `q`, Value: `name:Pikachu`
   - Key: `pageSize`, Value: `10`
5. Send request

## Integration Checklist

- [x] API client created (PokemonTCGApi)
- [x] Data models match API response
- [x] Repository implements API calls
- [x] Error handling implemented
- [x] Caching to Room database
- [x] Pagination support
- [x] Query building utilities
- [ ] Rate limiting handling
- [ ] Offline fallback strategy
- [ ] Request timeout configuration

## Resources

- **API Docs:** https://docs.pokemontcg.io/
- **API Swagger:** https://api.pokemontcg.io/docs
- **API Status:** https://status.pokemontcg.io/
- **GitHub:** https://github.com/PokemonTCG/pokemon-tcg-sdk-js

## Support

For API issues:
1. Check API documentation at docs.pokemontcg.io
2. Review API status page
3. Test queries in Postman first
4. Check network connectivity
5. Verify query syntax

## Next Steps

Once API integration is working:
1. Implement advanced filtering
2. Add pagination UI with loading states
3. Implement price history tracking
4. Add request caching with TTL
5. Implement offline-first strategy
