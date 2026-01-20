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
import com.example.pokemonmastersettracker.data.api.TCGdexService
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
    val tcgdexService = remember { TCGdexService() }

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
                    results.add("=== TCGdex API Diagnostic Test ===")
                    results.add("")
                    
                    // Test 1: Pikachu with detailed diagnostics
                    results.add("Test 1: Pikachu (English)")
                    results.add("")
                    try {
                        val diagnostics = tcgdexService.searchCardsByPokemonWithDiagnostics("Pikachu", "en")
                        results.addAll(diagnostics.diagnostics)
                        results.add("")
                        results.add("Final Result: ${diagnostics.cards.size} cards loaded")
                        if (diagnostics.cards.isNotEmpty()) {
                            results.add("Sample cards:")
                            diagnostics.cards.take(3).forEach { card ->
                                results.add("  • ${card.name} (${card.set?.name ?: "Unknown Set"})")
                            }
                        }
                    } catch (e: Exception) {
                        results.add("✗ Exception: ${e.message}")
                    }
                    results.add("")
                    results.add("═══════════════════════════════")
                    results.add("")
                    testResults = results.toList()
                    
                    // Test 2: Haunter (was having filter issues)
                    results.add("Test 2: Haunter (English)")
                    results.add("")
                    try {
                        val diagnostics = tcgdexService.searchCardsByPokemonWithDiagnostics("Haunter", "en")
                        results.addAll(diagnostics.diagnostics)
                        results.add("")
                        results.add("Final Result: ${diagnostics.cards.size} cards loaded")
                        if (diagnostics.cards.isNotEmpty()) {
                            results.add("Sample cards:")
                            diagnostics.cards.take(3).forEach { card ->
                                results.add("  • ${card.name} (${card.set?.name ?: "Unknown Set"})")
                            }
                        }
                    } catch (e: Exception) {
                        results.add("✗ Exception: ${e.message}")
                    }
                    results.add("")
                    results.add("═══════════════════════════════")
                    results.add("")
                    testResults = results.toList()
                    
                    // Test 3: Scyther (common search)
                    results.add("Test 3: Scyther (English)")
                    results.add("")
                    try {
                        val diagnostics = tcgdexService.searchCardsByPokemonWithDiagnostics("Scyther", "en")
                        results.addAll(diagnostics.diagnostics)
                        results.add("")
                        results.add("Final Result: ${diagnostics.cards.size} cards loaded")
                    } catch (e: Exception) {
                        results.add("✗ Exception: ${e.message}")
                    }
                    results.add("")
                    results.add("═══════════════════════════════")
                    results.add("")
                    testResults = results.toList()
                    
                    results.add("=== Diagnostic Test Complete ===")
                    results.add("")
                    results.add("What to look for:")
                    results.add("• API should return >0 cards")
                    results.add("• Check if dexId field is present in API response")
                    results.add("• See if filtering removes valid cards")
                    results.add("• Verify final card count matches expectations")
                    
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
            Text(if (isLoading) "Running Diagnostics..." else "Run Diagnostic Test")
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
