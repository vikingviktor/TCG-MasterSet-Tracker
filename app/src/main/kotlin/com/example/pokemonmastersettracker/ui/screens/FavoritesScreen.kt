package com.example.pokemonmastersettracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pokemonmastersettracker.data.models.Card
import com.example.pokemonmastersettracker.ui.theme.PokemonColors
import com.example.pokemonmastersettracker.viewmodel.CardViewModel
import com.example.pokemonmastersettracker.viewmodel.CardSortOption
import com.example.pokemonmastersettracker.ui.components.CardItem
import com.example.pokemonmastersettracker.ui.components.CardDetailDialog
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun FavoritesScreen(
    viewModel: CardViewModel = hiltViewModel(),
    onPokemonClick: (String) -> Unit = {}
) {
    val cardUiState by viewModel.cardUiState.collectAsState()
    var selectedCardForDialog by remember { mutableStateOf<Card?>(null) }
    var isCardOwned by remember { mutableStateOf(false) }
    var isCardInWishlist by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Load favorites when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
    }
    
    // Show dialog when a card is selected
    selectedCardForDialog?.let { card ->
        CardDetailDialog(
            card = card,
            isOwned = isCardOwned,
            isInWishlist = isCardInWishlist,
            onDismiss = { 
                selectedCardForDialog = null
                refreshTrigger++ // Trigger refresh when dialog closes
            },
            onToggleOwned = {
                scope.launch {
                    viewModel.toggleCardOwnership(card.id, isCardOwned)
                    // Wait a bit for database operation to complete
                    delay(100)
                    // Re-query the actual state from database
                    isCardOwned = viewModel.isCardOwned(card.id)
                }
            },
            onToggleWishlist = {
                scope.launch {
                    viewModel.toggleWishlist(card.id, isCardInWishlist)
                    // Wait a bit for database operation to complete
                    delay(100)
                    // Re-query the actual state from database
                    isCardInWishlist = viewModel.isInWishlist(card.id)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PokemonColors.Background)
            .padding(16.dp)
    ) {
        // Show back button when viewing cards
        if (cardUiState.selectedPokemonName != null && cardUiState.cards.isNotEmpty()) {
            Button(
                onClick = { viewModel.clearSelection(); viewModel.loadFavorites() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PokemonColors.Secondary
                )
            ) {
                Text("← Back to Favorites")
            }
        } else {
            // Header with title and refresh button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Favorite Pokemon",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(
                    onClick = { viewModel.loadFavorites() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh favorites",
                        tint = PokemonColors.Primary
                    )
                }
            }
        }

        when {
            cardUiState.loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PokemonColors.Primary)
                }
            }
            
            // Show cards if a Pokemon is selected
            cardUiState.selectedPokemonName != null && cardUiState.cards.isNotEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${cardUiState.selectedPokemonName} Cards (${cardUiState.cards.size})",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PokemonColors.Primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Sort options
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sort:",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        
                        val sortOptions = listOf(
                            CardSortOption.NONE to "None",
                            CardSortOption.SET_NAME to "Set",
                            CardSortOption.PRICE_LOW to "Price↑",
                            CardSortOption.PRICE_HIGH to "Price↓",
                            CardSortOption.RARITY to "Rarity",
                            CardSortOption.CARD_NUMBER to "Number"
                        )
                        
                        sortOptions.forEach { (option, label) ->
                            OutlinedButton(
                                onClick = { viewModel.setSortOption(option) },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (cardUiState.sortOption == option) 
                                        PokemonColors.Primary.copy(alpha = 0.1f) else Color.Transparent,
                                    contentColor = if (cardUiState.sortOption == option) 
                                        PokemonColors.Primary else Color.Gray
                                ),
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (cardUiState.sortOption == option) 
                                        FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    
                    SwipeRefresh(
                        state = rememberSwipeRefreshState(isRefreshing),
                        onRefresh = {
                            scope.launch {
                                isRefreshing = true
                                cardUiState.selectedPokemonName?.let { pokemonName ->
                                    viewModel.selectPokemonCards(pokemonName)
                                }
                                refreshTrigger++
                                delay(500)
                                isRefreshing = false
                            }
                        }
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(cardUiState.cards) { card ->
                                var cardOwned by remember { mutableStateOf(false) }
                                
                                LaunchedEffect(card.id, refreshTrigger) {
                                    cardOwned = viewModel.isCardOwned(card.id)
                                }
                                
                                CardItem(
                                    card = card,
                                    isOwned = cardOwned,
                                    onCardClick = {
                                        selectedCardForDialog = card
                                        scope.launch {
                                            isCardOwned = viewModel.isCardOwned(card.id)
                                            isCardInWishlist = viewModel.isInWishlist(card.id)
                                        }
                                    },
                                    onFavoriteToggle = { /* Handle favorite toggle */ }
                                )
                            }
                        }
                    }
                }
            }

            cardUiState.pokemonList.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("No favorite Pokemon yet")
                        Text(
                            "Star Pokemon from search to add them here",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            else -> {
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing),
                    onRefresh = {
                        scope.launch {
                            isRefreshing = true
                            viewModel.loadFavorites()
                            refreshTrigger++
                            delay(500)
                            isRefreshing = false
                        }
                    }
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(cardUiState.pokemonList.size) { index ->
                            val pokemon = cardUiState.pokemonList[index]
                            FavoritePokemonCard(
                                pokemon = pokemon,
                                onRemove = {
                                    viewModel.toggleFavorite(pokemon.name)
                                    viewModel.loadFavorites() // Refresh list
                                },
                                onViewCards = {
                                    viewModel.selectPokemonCards(pokemon.name)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritePokemonCard(
    pokemon: com.example.pokemonmastersettracker.data.models.Pokemon,
    onRemove: () -> Unit,
    onViewCards: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pokemon Image
            AsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )
            
            // Pokemon name and count
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = pokemon.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Show owned/total count
                if (pokemon.totalCards > 0) {
                    Text(
                        text = "(${pokemon.ownedCount}/${pokemon.totalCards})",
                        fontSize = 14.sp,
                        color = if (pokemon.ownedCount == pokemon.totalCards) 
                            Color(0xFF4CAF50) else Color.Gray
                    )
                }
            }
        }
        
        // Buttons row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onViewCards,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PokemonColors.Primary
                )
            ) {
                Text("View Cards")
            }

            Button(
                onClick = onRemove,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PokemonColors.Error
                )
            ) {
                Text("Remove")
            }
        }
    }
}
