package com.example.pokemonmastersettracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pokemonmastersettracker.data.models.Card
import com.example.pokemonmastersettracker.ui.components.CardItem
import com.example.pokemonmastersettracker.ui.theme.PokemonColors
import com.example.pokemonmastersettracker.viewmodel.CardViewModel

@Composable
fun HomeScreen(
    viewModel: CardViewModel = hiltViewModel(),
    onCardClick: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf("en") }
    val cardUiState by viewModel.cardUiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PokemonColors.Background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Section
        SearchSection(
            searchQuery = searchQuery,
            onSearchQueryChanged = { searchQuery = it },
            selectedLanguage = selectedLanguage,
            onLanguageChanged = { selectedLanguage = it },
            onSearch = {
                if (searchQuery.isNotEmpty()) {
                    viewModel.searchPokemonCards(searchQuery, selectedLanguage)
                }
            },
            onBackClick = if (cardUiState.selectedPokemonName != null) {
                { viewModel.clearSelection() }
            } else null
        )

        // Content Section
        when {
            cardUiState.loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PokemonColors.Primary)
                }
            }

            cardUiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${cardUiState.error}",
                        color = PokemonColors.Error
                    )
                }
            }

            cardUiState.selectedPokemonName != null && cardUiState.cards.isNotEmpty() -> {
                // Show all cards for selected Pokemon
                CardDetailView(
                    pokemonName = cardUiState.selectedPokemonName!!,
                    cards = cardUiState.cards,
                    onCardClick = onCardClick
                )
            }

            cardUiState.cards.isEmpty() && searchQuery.isNotEmpty() && cardUiState.selectedPokemonName == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No cards found for \"$searchQuery\"")
                }
            }

            cardUiState.cards.isNotEmpty() && cardUiState.selectedPokemonName == null -> {
                // Show Pokemon selection view (grouped by name)
                PokemonSelectionView(
                    cards = cardUiState.cards,
                    onPokemonSelect = { pokemonName ->
                        viewModel.selectPokemonCards(pokemonName)
                    }
                )
            }

            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Search for a Pokemon to get started")
                }
            }
        }
    }
}

@Composable
fun SearchSection(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    selectedLanguage: String,
    onLanguageChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onBackClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PokemonColors.Surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Back button if viewing details
        if (onBackClick != null) {
            Button(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = PokemonColors.Secondary
                )
            ) {
                Text("← Back to Search Results")
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search Pokemon") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            singleLine = true
        )

        // Language Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onLanguageChanged("en") },
                modifier = Modifier.weight(1f),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = if (selectedLanguage == "en") PokemonColors.Primary else Color.LightGray
                )
            ) {
                Text("English")
            }

            Button(
                onClick = { onLanguageChanged("ja") },
                modifier = Modifier.weight(1f),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = if (selectedLanguage == "ja") PokemonColors.Primary else Color.LightGray
                )
            ) {
                Text("Japanese")
            }
        }

        Button(
            onClick = onSearch,
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = PokemonColors.Primary
            )
        ) {
            Text("Search")
        }
    }
}

@Composable
fun PokemonSelectionView(
    cards: List<Card>,
    onPokemonSelect: (String) -> Unit
) {
    // Group cards by Pokemon name
    val groupedByName = cards.groupBy { it.name }
    val pokemonNames = groupedByName.keys.toList()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(count = pokemonNames.size) { index ->
            val pokemonName = pokemonNames[index]
            val cardsForPokemon = groupedByName[pokemonName] ?: emptyList()
            PokemonSelectionCard(
                pokemonName = pokemonName,
                cardCount = cardsForPokemon.size,
                onClick = { onPokemonSelect(pokemonName) }
            )
        }
    }
}

@Composable
fun PokemonSelectionCard(
    pokemonName: String,
    cardCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = PokemonColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pokemon info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = pokemonName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PokemonColors.Primary
                )
                Text(
                    text = "$cardCount card${if (cardCount != 1) "s" else ""} available",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Arrow indicator
            Text(
                text = "→",
                fontSize = 20.sp,
                color = PokemonColors.Primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CardDetailView(
    pokemonName: String,
    cards: List<Card>,
    onCardClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "All $pokemonName Cards (${cards.size})",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = PokemonColors.Primary,
            modifier = Modifier.padding(8.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cards) { card ->
                CardItem(
                    card = card,
                    onCardClick = { onCardClick(card.id) },
                    onFavoriteToggle = { /* Handle favorite toggle */ }
                )
            }
        }
    }
}
