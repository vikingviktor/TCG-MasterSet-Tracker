package com.example.pokemonmastersettracker.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
            }
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

            cardUiState.cards.isEmpty() && searchQuery.isNotEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No cards found for \"$searchQuery\"")
                }
            }

            cardUiState.cards.isNotEmpty() -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cardUiState.cards) { card ->
                        CardItem(
                            card = card,
                            onCardClick = { onCardClick(card.id) },
                            onFavoriteToggle = { /* Handle favorite toggle */ }
                        )
                    }
                }
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
    onSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PokemonColors.Surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
