# API Error Fix (HTTP 404/504)

## Problem
Users were experiencing HTTP 404 and 504 errors when clicking on Pokemon to view their cards. The API Test screen showed that API connectivity was working fine (500ms responses), but actual Pokemon searches were failing.

## Root Cause
The issue was in the `buildCardQuery()` function in `PokemonRepository.kt`. The query format was:
```kotlin
"name:\"${pokemonName}*\""  // With quotes around the name
```

This format was causing issues with:
1. Special characters in Pokemon names (e.g., "Nidoran♀", "Mr. Mime")
2. The Pokemon TCG API not properly handling the quoted format
3. Potential URL encoding issues

## Solution

### 1. Fixed Query Format
Updated the `buildCardQuery()` function to remove quotes:
```kotlin
private fun buildCardQuery(pokemonName: String, language: String): String {
    val cleanName = pokemonName.trim()
    return "name:$cleanName*"  // Without quotes
}
```

### 2. Enhanced Error Logging
Added detailed error logging in `searchPokemonCardsWithPagination()`:
- Logs the exact Pokemon name being searched
- Logs the exact query string being sent to the API
- Shows both the Pokemon name and query in error messages
- Attempts to return cached results if API fails

### 3. Improved Error Display
Updated the error UI in `HomeScreen.kt`:
- Shows user-friendly messages instead of technical stack traces
- Different messages for different error types:
  - **404 Error**: "Pokemon not found. Try a different Pokemon name."
  - **504 Error**: "Server timeout. The Pokemon TCG API is slow right now. Please try again."
  - **Other Errors**: Shows the actual error message
- Added a "Retry" button to try the search again

### 4. Fallback to Cache
If the API call fails, the repository now:
1. Checks for cached cards in the local database
2. Returns cached results if available
3. Logs that cached data is being used
4. Only throws an error if no cache is available

## Files Modified
1. **PokemonRepository.kt**:
   - Fixed `buildCardQuery()` (removed quotes)
   - Enhanced `searchPokemonCardsWithPagination()` error handling
   - Added cache fallback on API errors

2. **HomeScreen.kt**:
   - Improved error display UI
   - Added retry button
   - Added user-friendly error messages
   - Added `Spacer` import

## Testing
After making these changes:
1. Launch the app
2. Click on any Pokemon from search results or favorites
3. Check the Logcat output for the actual query being sent:
   - Look for: `🌐 API REQUEST: pokemonName='...', query='...', page=..., pageSize=...`
4. If errors still occur, the error message will now show both the Pokemon name and query used

## Next Steps
If errors persist:
1. Check the Logcat for the exact query being sent
2. Test the query manually using the Pokemon TCG API documentation: https://docs.pokemontcg.io/
3. Some Pokemon names might need special handling (e.g., Pokemon with accents, symbols, or special characters)
4. Consider adding URL encoding for special characters if needed
