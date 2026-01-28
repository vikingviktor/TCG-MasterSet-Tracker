package com.example.pokemonmastersettracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pokemonmastersettracker.data.api.TCGdexService
import com.example.pokemonmastersettracker.ui.theme.PokemonColors
import com.example.pokemonmastersettracker.viewmodel.CardViewModel
import kotlinx.coroutines.launch
import com.example.pokemonmastersettracker.ui.theme.ThemeManager
import com.example.pokemonmastersettracker.ui.theme.AppTheme
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ConfigThemeManagerEntryPoint {
    fun themeManager(): ThemeManager
}

@Composable
fun ConfigScreen(
    viewModel: CardViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val themeManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            ConfigThemeManagerEntryPoint::class.java
        ).themeManager()
    }

    // Observe theme changes and trigger recomposition
    val currentTheme by themeManager.currentTheme.collectAsState(initial = AppTheme.LIGHT)
    // Optionally, update PokemonColors here if needed

    var testResults by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val tcgdexService = remember { TCGdexService() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Configuration & Help",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Theme Selection Section
        ThemeSelectionSection(
            themeManager = themeManager,
            onThemeChanged = { refreshTrigger++ }
        )

        Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), thickness = 1.dp)

        // FAQ Section
        FAQSection()

        Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), thickness = 1.dp)

        // API Test Section
        ApiTestSection(
            isLoading = isLoading,
            testResults = testResults,
            onRunTest = {
                scope.launch {
                    isLoading = true
                    testResults = emptyList()
                    
                    val results = mutableListOf<String>()
                    results.add("=== TCGdex API Diagnostic Test ===")
                    results.add("")
                    
                    // Test 1: Pikachu
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
                    
                    // Test 2: Haunter
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
                    
                    // Test 3: Scyther
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
            }
        )
    }
}

@Composable
fun ThemeSelectionSection(
    themeManager: ThemeManager,
    onThemeChanged: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "App Theme",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = "Choose your preferred color theme",
            fontSize = 14.sp,
            color = Color.Gray
        )
        
        AppTheme.values().forEach { theme ->
            OutlinedButton(
                onClick = {
                    scope.launch {
                        themeManager.setTheme(theme)
                        onThemeChanged()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(theme.displayName, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun FAQSection() {
    var expandedIndex by remember { mutableStateOf<Int?>(null) }
    var isFAQExpanded by remember { mutableStateOf(false) }
    
    val faqs = listOf(
        FAQ(
            question = "How do I search for Pokémon cards?",
            answer = "Go to the Home tab and use the search bar at the top. Type the Pokémon name or browse by generation. You can also view cards by selecting a generation from the list."
        ),
        FAQ(
            question = "How do I add cards to my collection?",
            answer = "When viewing a card, tap on it to open details. Then tap the '+ Collection' button to add it to your collection. You can also add notes about the card's condition and price."
        ),
        FAQ(
            question = "What's the difference between Collection and Wishlist?",
            answer = "Collection contains cards you already own, while Wishlist (Collection tab) contains cards you want to acquire. You can track different information for each."
        ),
        FAQ(
            question = "How do I filter and sort my cards?",
            answer = "In the Favories or Home search result tab, tap the filter icon to access sorting options: by set name, price (low to high), rarity, or card number."
        ),
        FAQ(
            question = "Can I search for Japanese cards?",
            answer = "Yes! When you select View Cards from your Favorite Pokemon the app will fetch Japanese cards. Note: Japanese card support is currently in beta and may have limited availability."
        ),
        FAQ(
            question = "How do I change the app theme?",
            answer = "You can change themes right here in the Config tab! Choose from Light, Dark, Pokémon Red, Pokémon Blue, or Midnight themes."
        ),
        FAQ(
            question = "What are generations?",
            answer = "Generations group Pokémon by when they were introduced:\n• Gen I (Kanto): #1-151\n• Gen II (Johto): #152-251\n• Gen III (Hoenn): #252-386\n• And so on through Gen IX (Paldea): #906-1025"
        ),
        FAQ(
            question = "How do I remove a card from my collection?",
            answer = "Open the card details and tap the 'In Collection' button. This will remove it from your collection but not delete the card data."
        )
    )
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // FAQ Header (collapsible)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isFAQExpanded = !isFAQExpanded },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Frequently Asked Questions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = if (isFAQExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isFAQExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // FAQ Items (only show when expanded)
        if (isFAQExpanded) {
            faqs.forEachIndexed { index, faq ->
                FAQItem(
                    faq = faq,
                    isExpanded = expandedIndex == index,
                    onClick = {
                        expandedIndex = if (expandedIndex == index) null else index
                    }
                )
            }
        }
    }
}

@Composable
fun FAQItem(
    faq: FAQ,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            if (isExpanded) {
                Text(
                    text = faq.answer,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 12.dp),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun ApiTestSection(
    isLoading: Boolean,
    testResults: List<String>,
    onRunTest: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "API Connection Test",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = "Test the TCGdex API connection speed and reliability",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Button(
            onClick = onRunTest,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
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

        if (testResults.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
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
                                line.startsWith("===") -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

data class FAQ(
    val question: String,
    val answer: String
)
