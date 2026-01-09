package com.example.pokemonmastersettracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pokemonmastersettracker.ui.theme.PokemonColors
import com.example.pokemonmastersettracker.viewmodel.CardViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ApiTestScreen(
    viewModel: CardViewModel = hiltViewModel()
) {
    var testResults by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PokemonColors.Background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "API Connection Test",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = PokemonColors.Primary
        )
        
        Text(
            text = "Test the Pokemon TCG API connection speed and reliability",
            fontSize = 14.sp,
            color = Color.Gray
        )

        // Pokemon TCG API Test Button
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    testResults = emptyList()
                    
                    val results = mutableListOf<String>()
                    results.add("=== Pokemon TCG API Test Started ===\n")
                    
                    // Test 0: Direct API call to verify endpoint
                    results.add("Test 0: Direct API endpoint check")
                    val test0Start = System.currentTimeMillis()
                    try {
                        // Try the simplest possible query - no parameters
                        viewModel.testDirectApiCall()
                        delay(2000)
                        val test0Time = System.currentTimeMillis() - test0Start
                        results.add("✓ Base endpoint reachable in ${test0Time}ms\n")
                    } catch (e: Exception) {
                        results.add("✗ Base endpoint failed: ${e.message}\n")
                    }
                    testResults = results.toList()
                    
                    // Test 1: Simple search query
                    results.add("Test 1: Searching for 'Pikachu' (25 cards)")
                    val test1Start = System.currentTimeMillis()
                    try {
                        viewModel.searchPokemonLocal("Pikachu")
                        delay(100) // Give it time to process
                        val test1Time = System.currentTimeMillis() - test1Start
                        results.add("✓ Local search completed in ${test1Time}ms\n")
                    } catch (e: Exception) {
                        results.add("✗ Failed: ${e.message}\n")
                    }
                    testResults = results.toList()
                    
                    // Test 2: Card search with pagination
                    results.add("Test 2: Fetching Pikachu cards from API (page 1, 25 cards)")
                    val test2Start = System.currentTimeMillis()
                    try {
                        viewModel.selectPokemonCards("Pikachu", setOf("en"), 1, 25)
                        delay(500) // Give API time to respond
                        val test2Time = System.currentTimeMillis() - test2Start
                        results.add("✓ API request completed in ${test2Time}ms")
                        
                        if (test2Time > 10000) {
                            results.add("⚠ WARNING: Response took over 10 seconds!")
                        } else if (test2Time > 5000) {
                            results.add("⚠ Response is slow (>5s)")
                        } else if (test2Time > 2000) {
                            results.add("ℹ Response is acceptable (2-5s)")
                        } else {
                            results.add("✓ Response is fast (<2s)")
                        }
                        results.add("")
                    } catch (e: Exception) {
                        results.add("✗ Failed: ${e.message}\n")
                    }
                    testResults = results.toList()
                    
                    // Test 3: Large page size
                    results.add("Test 3: Fetching with larger page size (50 cards)")
                    val test3Start = System.currentTimeMillis()
                    try {
                        viewModel.selectPokemonCards("Charizard", setOf("en"), 1, 50)
                        delay(500)
                        val test3Time = System.currentTimeMillis() - test3Start
                        results.add("✓ Large request completed in ${test3Time}ms\n")
                    } catch (e: Exception) {
                        results.add("✗ Failed: ${e.message}\n")
                    }
                    testResults = results.toList()
                    
                    // Test 4: Multi-language request
                    results.add("Test 4: Multi-language search (English + Japanese)")
                    val test4Start = System.currentTimeMillis()
                    try {
                        viewModel.selectPokemonCards("Mewtwo", setOf("en", "ja"), 1, 25)
                        delay(500)
                        val test4Time = System.currentTimeMillis() - test4Start
                        results.add("✓ Multi-language completed in ${test4Time}ms")
                        results.add("ℹ Note: Multi-language makes 2 API calls\n")
                    } catch (e: Exception) {
                        results.add("✗ Failed: ${e.message}\n")
                    }
                    testResults = results.toList()
                    
                    // Test 5: Exact query format we use in app
                    results.add("Test 5: Testing exact app query format + Total Count")
                    val test5Start = System.currentTimeMillis()
                    try {
                        results.add("Pokemon: Pikachu | PageSize: 50 | Page: 1")
                        results.add("(Query details in Logcat - will use Pokedex #25 if available)")
                        viewModel.selectPokemonCards("Pikachu", setOf("en"), 1, 50)
                        
                        // Wait for loading to complete (max 5 seconds)
                        var waitTime = 0
                        while (viewModel.cardUiState.value.loading && waitTime < 5000) {
                            delay(100)
                            waitTime += 100
                        }
                        
                        val test5Time = System.currentTimeMillis() - test5Start
                        results.add("✓ Query completed in ${test5Time}ms")
                        
                        val cardCount = viewModel.cardUiState.value.cards.size
                        val allCardsCount = viewModel.cardUiState.value.allCards.size
                        val debugInfo = viewModel.cardUiState.value.debugInfo ?: "No debug info"
                        val errorInfo = viewModel.cardUiState.value.error
                        val isLoading = viewModel.cardUiState.value.loading
                        val selectedPokemon = viewModel.cardUiState.value.selectedPokemonName
                        
                        results.add("ℹ Cards in state: $cardCount")
                        results.add("ℹ All cards: $allCardsCount")
                        results.add("ℹ Still loading: $isLoading")
                        results.add("ℹ Selected Pokemon: $selectedPokemon")
                        results.add("ℹ Debug: $debugInfo")
                        
                        if (errorInfo != null) {
                            results.add("⚠ Error occurred: $errorInfo")
                            results.add("  Check Logcat for full details")
                        } else if (cardCount == 0) {
                            results.add("⚠ WARNING: No cards returned!")
                            results.add("  Possible issues:")
                            results.add("  • API returned empty results")
                            results.add("  • Query failed but no error set")
                            results.add("  • Check Logcat for API logs")
                        } else {
                            results.add("✓ Cards successfully loaded")
                            results.add("  First card: ${viewModel.cardUiState.value.cards.firstOrNull()?.name ?: "N/A"}")
                        }
                        results.add("")
                    } catch (e: Exception) {
                        val errorMsg = e.message ?: "Unknown error"
                        results.add("✗ Failed: $errorMsg")
                        if (errorMsg.contains("504") || errorMsg.contains("timeout")) {
                            results.add("⚠ This is a TIMEOUT error - API took >120s to respond")
                        } else if (errorMsg.contains("404")) {
                            results.add("⚠ This is a NOT FOUND error")
                        }
                        results.add("")
                    }
                    testResults = results.toList()
                    
                    results.add("=== Test Complete ===")
                    results.add("\nDiagnostic Info:")
                    results.add("• API: https://api.pokemontcg.io/v2/")
                    results.add("• Timeout: 120s read, 30s connect")
                    results.add("• Retry on failure: Enabled")
                    results.add("\nCommon Issues:")
                    results.add("• Slow response (>5s): API server may be under load")
                    results.add("• Timeout errors: Check internet connection")
                    results.add("• 429 errors: Rate limit exceeded (wait a minute)")
                    results.add("• 404 errors: Card not found or query incorrect")
                    
                    testResults = results.toList()
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = PokemonColors.Primary
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp),
                    color = Color.White
                )
            }
            Text(if (isLoading) "Testing..." else "Run API Tests")
        }

        // TCGdex API Test Button
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    testResults = emptyList()
                    
                    val results = mutableListOf<String>()
                    results.add("=== TCGdex API Test Started ===")
                    results.add("")
                    
                    // Test 1: English cards
                    results.add("Test 1: Fetching Scyther* cards in English")
                    results.add("(Using wildcard to get all variants)")
                    val test1Start = System.currentTimeMillis()
                    try {
                        viewModel.loadCardsFromTCGdex("Scyther", "en")
                        
                        // Wait for loading to complete
                        var waitTime = 0
                        while (viewModel.cardUiState.value.loading && waitTime < 10000) {
                            delay(100)
                            waitTime += 100
                        }
                        
                        val test1Time = System.currentTimeMillis() - test1Start
                        val cardCount = viewModel.cardUiState.value.cards.size
                        results.add("✓ English cards loaded in ${test1Time}ms")
                        results.add("ℹ Cards found: $cardCount")
                        
                        if (cardCount > 0) {
                            val firstCard = viewModel.cardUiState.value.cards.first()
                            results.add("ℹ First card: ${firstCard.name}")
                            results.add("ℹ Set: ${firstCard.set?.name ?: "Unknown"}")
                            results.add("ℹ Image: ${if (firstCard.image?.small != null) "✓" else "✗ Missing"}")
                        }
                        results.add("")
                    } catch (e: Exception) {
                        results.add("✗ Failed: ${e.message}")
                        results.add("")
                    }
                    testResults = results.toList()
                    
                    // Test 2: Japanese cards
                    results.add("Test 2: Fetching Scyther* cards in Japanese")
                    results.add("(Using wildcard to get all variants)")
                    val test2Start = System.currentTimeMillis()
                    try {
                        viewModel.loadCardsFromTCGdex("Scyther", "ja")
                        
                        // Wait for loading to complete
                        var waitTime = 0
                        while (viewModel.cardUiState.value.loading && waitTime < 10000) {
                            delay(100)
                            waitTime += 100
                        }
                        
                        val test2Time = System.currentTimeMillis() - test2Start
                        val cardCount = viewModel.cardUiState.value.cards.size
                        results.add("✓ Japanese cards loaded in ${test2Time}ms")
                        results.add("ℹ Cards found: $cardCount")
                        
                        if (cardCount > 0) {
                            val firstCard = viewModel.cardUiState.value.cards.first()
                            results.add("ℹ First card: ${firstCard.name}")
                            results.add("ℹ Set: ${firstCard.set?.name ?: "Unknown"}")
                            results.add("ℹ Image: ${if (firstCard.image?.small != null) "✓" else "✗ Missing"}")
                        }
                        results.add("")
                    } catch (e: Exception) {
                        results.add("✗ Failed: ${e.message}")
                        results.add("")
                    }
                    testResults = results.toList()
                    
                    // Test 3: Try another Pokemon - Pikachu
                    results.add("Test 3: Fetching Pikachu* cards in English")
                    results.add("(Using wildcard to get all variants)")
                    val test3Start = System.currentTimeMillis()
                    try {
                        viewModel.loadCardsFromTCGdex("Pikachu", "en")
                        
                        var waitTime = 0
                        while (viewModel.cardUiState.value.loading && waitTime < 10000) {
                            delay(100)
                            waitTime += 100
                        }
                        
                        val test3Time = System.currentTimeMillis() - test3Start
                        val cardCount = viewModel.cardUiState.value.cards.size
                        results.add("✓ Pikachu cards loaded in ${test3Time}ms")
                        results.add("ℹ Cards found: $cardCount")
                        results.add("")
                    } catch (e: Exception) {
                        results.add("✗ Failed: ${e.message}")
                        results.add("")
                    }
                    testResults = results.toList()
                    
                    results.add("=== TCGdex Test Complete ===")
                    results.add("")
                    results.add("Diagnostic Info:")
                    results.add("• API: https://api.tcgdex.net/v2/")
                    results.add("• Languages: en (English), ja (Japanese)")
                    results.add("• Used for Japanese card support only")
                    results.add("")
                    results.add("Notes:")
                    results.add("• TCGdex has fewer cards than Pokemon TCG API")
                    results.add("• Some cards may not have images immediately")
                    results.add("• Check Logcat for detailed API responses")
                    
                    testResults = results.toList()
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9C27B0) // Purple to differentiate from main API test
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp),
                    color = Color.White
                )
            }
            Text(if (isLoading) "Testing..." else "Run TCGdex API Tests")
        }

        // Results display
        if (testResults.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    testResults.forEach { line ->
                        Text(
                            text = line,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = when {
                                line.startsWith("✓") -> Color(0xFF4CAF50)
                                line.startsWith("✗") -> Color(0xFFEF5350)
                                line.startsWith("⚠") -> Color(0xFFFFA726)
                                line.startsWith("ℹ") -> Color(0xFF42A5F5)
                                line.startsWith("===") -> PokemonColors.Primary
                                else -> Color.Black
                            },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
