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
            text = "Test the TCGdex API connection speed and reliability",
            fontSize = 14.sp,
            color = Color.Gray
        )

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
                    results.add("Test 1: Fetching Pikachu cards in English")
                    val test1Start = System.currentTimeMillis()
                    try {
                        viewModel.loadCardsFromTCGdex("Pikachu", "en")
                        delay(500)
                        val test1Time = System.currentTimeMillis() - test1Start
                        val cardCount = viewModel.cardUiState.value.cards.size
                        results.add("✓ English cards loaded in ${test1Time}ms")
                        results.add("  Cards returned: $cardCount")
                        if (test1Time > 2000) {
                            results.add("  ⚠ Response is slow (>2s)")
                        } else {
                            results.add("  ✓ Response is fast (<2s)")
                        }
                        results.add("")
                    } catch (e: Exception) {
                        results.add("✗ Failed: ${e.message}")
                        results.add("")
                    }
                    testResults = results.toList()
                    
                    // Test 2: Japanese cards
                    results.add("Test 2: Fetching Charizard cards in Japanese")
                    val test2Start = System.currentTimeMillis()
                    try {
                        viewModel.loadCardsFromTCGdex("Charizard", "ja")
                        delay(500)
                        val test2Time = System.currentTimeMillis() - test2Start
                        val cardCount = viewModel.cardUiState.value.cards.size
                        results.add("✓ Japanese cards loaded in ${test2Time}ms")
                        results.add("  Cards returned: $cardCount")
                        if (test2Time > 2000) {
                            results.add("  ⚠ Response is slow (>2s)")
                        } else {
                            results.add("  ✓ Response is fast (<2s)")
                        }
                        results.add("")
                    } catch (e: Exception) {
                        results.add("✗ Failed: ${e.message}")
                        results.add("")
                    }
                    testResults = results.toList()
                    
                    // Test 3: Different Pokemon
                    results.add("Test 3: Fetching Mewtwo cards")
                    val test3Start = System.currentTimeMillis()
                    try {
                        viewModel.loadCardsFromTCGdex("Mewtwo", "en")
                        delay(500)
                        val test3Time = System.currentTimeMillis() - test3Start
                        val cardCount = viewModel.cardUiState.value.cards.size
                        results.add("✓ Cards loaded in ${test3Time}ms")
                        results.add("  Cards returned: $cardCount")
                        if (cardCount > 0) {
                            results.add("  First card: ${viewModel.cardUiState.value.cards.firstOrNull()?.name ?: "N/A"}")
                        }
                        results.add("")
                    } catch (e: Exception) {
                        results.add("✗ Failed: ${e.message}")
                        results.add("")
                    }
                    testResults = results.toList()
                    
                    // Test 4: Gen 2 Pokemon (Togepi)
                    results.add("Test 4: Testing Gen 2 Pokemon (Togepi)")
                    val test4Start = System.currentTimeMillis()
                    try {
                        viewModel.loadCardsFromTCGdex("Togepi", "en")
                        delay(500)
                        val test4Time = System.currentTimeMillis() - test4Start
                        val cardCount = viewModel.cardUiState.value.cards.size
                        results.add("✓ Togepi cards loaded in ${test4Time}ms")
                        results.add("  Cards returned: $cardCount")
                        results.add("  ℹ Should be ~30-40 cards, not 587+")
                        results.add("")
                    } catch (e: Exception) {
                        results.add("✗ Failed: ${e.message}")
                        results.add("")
                    }
                    testResults = results.toList()
                    
                    results.add("=== Test Complete ===")
                    results.add("")
                    results.add("Diagnostic Info:")
                    results.add("• API: https://api.tcgdex.net/v2/")
                    results.add("• Supports: Gen 1-9 Pokemon (1025 total)")
                    results.add("• Languages: en, ja, and 15+ others")
                    results.add("• Speed: Typically <1s per request")
                    results.add("")
                    results.add("Common Issues:")
                    results.add("• Slow response: Check internet connection")
                    results.add("• 0 cards returned: Pokemon not in Pokedex map")
                    results.add("• Too many cards: May need name-based search")
                    
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
