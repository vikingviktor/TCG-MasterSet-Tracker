package com.example.pokemonmastersettracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pokemonmastersettracker.data.models.Card
import com.example.pokemonmastersettracker.ui.theme.PokemonColors
import com.example.pokemonmastersettracker.viewmodel.CardViewModel
import com.example.pokemonmastersettracker.viewmodel.CardSortOption
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pokemonmastersettracker.ui.components.CardItem
import com.example.pokemonmastersettracker.ui.components.CardDetailDialog
import kotlinx.coroutines.launch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    viewModel: CardViewModel = hiltViewModel(),
    onCardClick: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val cardUiState by viewModel.cardUiState.collectAsState()
    var selectedCardForDialog by remember { mutableStateOf<Card?>(null) }
    var isCardOwned by remember { mutableStateOf(false) }
    var isCardInWishlist by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) } // Trigger for refreshing card states
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
            onToggleOwned = { condition ->
                scope.launch {
                    viewModel.toggleCardOwnership(card.id, isCardOwned, condition)
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
    
    // Show collection tracking confirmation dialog
    cardUiState.showTrackingDialog?.let { pokemonName ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissTrackingDialog() },
            title = {
                Text("Track $pokemonName in your Collection?")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "This will track your progress collecting all $pokemonName cards.",
                        fontSize = 14.sp
                    )
                    Text(
                        "You can change this later by unfavoriting and favoriting again.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        viewModel.updateFavoriteWithTracking(pokemonName, enableTracking = true)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PokemonColors.Primary
                    )
                ) {
                    Text("Yes, Track It")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        viewModel.updateFavoriteWithTracking(pokemonName, enableTracking = false)
                    }
                ) {
                    Text("No, Just Favorite")
                }
            }
        )
    }

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = {
            scope.launch {
                isRefreshing = true
                // Clear search and return to generations
                searchQuery = ""
                viewModel.clearSelection()
                viewModel.clearGeneration()
                delay(300)
                isRefreshing = false
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PokemonColors.Background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Only show search section when NOT viewing a Pokemon's cards
        if (cardUiState.selectedPokemonName == null) {
            SearchSection(
                searchQuery = searchQuery,
                onSearchQueryChanged = { 
                    searchQuery = it
                    // Clear generation selection when user starts typing
                    if (it.isNotEmpty() && cardUiState.selectedGeneration != null) {
                        viewModel.clearGeneration()
                    }
                },
                onSearch = {
                    if (searchQuery.isNotEmpty()) {
                        viewModel.searchPokemonLocal(searchQuery)
                    }
                }
            )
        } else {
            // Show back button and favorite button when viewing cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { 
                        viewModel.clearSelection()
                        // If we came from a generation, reload it
                        cardUiState.selectedGeneration?.let { gen ->
                            viewModel.selectGeneration(gen)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = PokemonColors.Secondary
                    )
                ) {
                    Text("← Back to Search Results")
                }
                
                // Check if this Pokemon is in favorites
                val pokemonName = cardUiState.selectedPokemonName ?: ""
                val isFavorite = cardUiState.pokemonList.find { it.name == pokemonName }?.isFavorite ?: false
                
                Button(
                    onClick = { 
                        if (pokemonName.isNotEmpty()) {
                            viewModel.toggleFavorite(pokemonName)
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = if (isFavorite) PokemonColors.Primary else Color.Gray
                    )
                ) {
                    Text(
                        text = if (isFavorite) "★" else "☆",
                        fontSize = 20.sp
                    )
                }
            }
        }

        // Content Section
        when {
            cardUiState.loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = PokemonColors.Primary)
                        Text(
                            text = "Loading Cards...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = PokemonColors.Primary
                        )
                    }
                }
            }

            cardUiState.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Unable to load cards",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PokemonColors.Error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when {
                            cardUiState.error!!.contains("404") -> "Pokemon not found. Try a different Pokemon name."
                            cardUiState.error!!.contains("504") || cardUiState.error!!.contains("timeout") -> 
                                "Server timeout. The Pokemon TCG API is slow right now. Please try again."
                            cardUiState.error!!.contains("ConnectException") || cardUiState.error!!.contains("Failed to connect") ->
                                "Unable to connect to Pokemon TCG API. Check your internet connection and try again."
                            cardUiState.error!!.contains("UnknownHostException") ->
                                "Cannot reach api.pokemontcg.io. Check your internet connection or DNS settings."
                            else -> "Error: ${cardUiState.error}"
                        },
                        color = PokemonColors.Error.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    
                    // Show diagnostic info
                    cardUiState.debugInfo?.let { debug ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.7f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Debug Info:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Yellow
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = debug,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                cardUiState.selectedPokemonName?.let { pokemonName ->
                                    viewModel.loadCardsFromTCGdex(pokemonName, "en")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PokemonColors.Primary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Retry")
                        }
                        
                        // Test query button for debugging
                        OutlinedButton(
                            onClick = {
                                // Test with Pikachu (very common Pokemon)
                                viewModel.loadCardsFromTCGdex("Pikachu", "en")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Test API", fontSize = 12.sp)
                        }
                    }
                }
            }

            cardUiState.selectedPokemonName != null && cardUiState.cards.isNotEmpty() -> {
                Column {
                    // Debug info banner (shows query used)
                    cardUiState.debugInfo?.let { debug ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1B5E20).copy(alpha = 0.9f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = debug,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    
                    // Show all cards for selected Pokemon
                    CardDetailView(
                        pokemonName = cardUiState.selectedPokemonName!!,
                        cards = cardUiState.cards,
                        currentPage = cardUiState.currentPage,
                        hasMorePages = cardUiState.hasMorePages,
                        pageSize = cardUiState.pageSize,
                        refreshTrigger = refreshTrigger,
                        viewModel = viewModel,
                        onCardClick = { card ->
                            selectedCardForDialog = card
                            scope.launch {
                                isCardOwned = viewModel.isCardOwned(card.id)
                                isCardInWishlist = viewModel.isInWishlist(card.id)
                            }
                        }
                    )
                }
            }

            cardUiState.pokemonList.isEmpty() && searchQuery.isNotEmpty() && cardUiState.selectedPokemonName == null && !cardUiState.loading && !cardUiState.lastQuery.isNullOrEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Pokemon found for \"$searchQuery\"")
                }
            }

            cardUiState.pokemonList.isNotEmpty() && cardUiState.selectedPokemonName == null -> {
                // Show Pokemon selection view (from search or generation filter)
                Column {
                    // Show generation header if we're viewing a generation
                    cardUiState.selectedGeneration?.let { genNumber ->
                        val generation = com.example.pokemonmastersettracker.viewmodel.GENERATIONS.find { it.number == genNumber }
                        generation?.let {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = it.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PokemonColors.Primary
                                )
                                Button(
                                    onClick = { viewModel.clearGeneration() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PokemonColors.Secondary
                                    )
                                ) {
                                    Text("← Back to Generations")
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    
                    PokemonListView(
                        pokemonList = cardUiState.pokemonList,
                        onPokemonSelect = { pokemonName ->
                            viewModel.loadCardsFromTCGdex(pokemonName, "en")
                        },
                        onFavoriteToggle = { pokemonName ->
                            viewModel.toggleFavorite(pokemonName)
                        }
                    )
                }
            }

            else -> {
                // Show generation selector when no search is active
                if (searchQuery.isEmpty() && cardUiState.selectedGeneration == null) {
                    GenerationSelector(
                        onGenerationSelect = { generationNumber ->
                            viewModel.selectGeneration(generationNumber)
                        }
                    )
                } else {
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
    }
}

@Composable
fun SearchSection(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
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
fun PokemonListView(
    pokemonList: List<com.example.pokemonmastersettracker.data.models.Pokemon>,
    onPokemonSelect: (String) -> Unit,
    onFavoriteToggle: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(count = pokemonList.size) { index ->
            val pokemon = pokemonList[index]
            PokemonSelectionCard(
                pokemonName = pokemon.name,
                imageUrl = pokemon.imageUrl,
                isFavorite = pokemon.isFavorite,
                onClick = { onPokemonSelect(pokemon.name) },
                onFavoriteClick = { onFavoriteToggle(pokemon.name) }
            )
        }
    }
}

@Composable
fun PokemonSelectionCard(
    pokemonName: String,
    imageUrl: String?,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
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
            // Pokemon image placeholder
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = pokemonName,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = pokemonName.first().toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            // Pokemon info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = pokemonName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PokemonColors.Primary
                )
                Text(
                    text = "Tap to view all cards",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Favorite button
            androidx.compose.material3.IconButton(
                onClick = { onFavoriteClick() }
            ) {
                Text(
                    text = if (isFavorite) "★" else "☆",
                    fontSize = 24.sp,
                    color = if (isFavorite) PokemonColors.Primary else Color.Gray
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
    currentPage: Int,
    hasMorePages: Boolean,
    pageSize: Int,
    refreshTrigger: Int,
    viewModel: CardViewModel,
    onCardClick: (Card) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header with card count and pagination controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$pokemonName Cards (${cards.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PokemonColors.Primary
            )
            
            // Page size selector
            // Page size selector removed - TCGdex returns all cards at once
        }
        
        // Sort options
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
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
                        containerColor = if (viewModel.cardUiState.value.sortOption == option) 
                            PokemonColors.Primary.copy(alpha = 0.1f) else Color.Transparent,
                        contentColor = if (viewModel.cardUiState.value.sortOption == option) 
                            PokemonColors.Primary else Color.Gray
                    ),
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (viewModel.cardUiState.value.sortOption == option) 
                            FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(cards) { card ->
                var cardOwned by remember { mutableStateOf(false) }
                
                LaunchedEffect(card.id, refreshTrigger) {
                    cardOwned = viewModel.isCardOwned(card.id)
                }
                
                CardItem(
                    card = card,
                    isOwned = cardOwned,
                    onCardClick = { onCardClick(card) },
                    onFavoriteToggle = { /* Handle favorite toggle */ }
                )
            }
        }
    }
}

@Composable
fun GenerationSelector(
    onGenerationSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Select a Generation",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = PokemonColors.Primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Browse Pokémon by generation",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(com.example.pokemonmastersettracker.viewmodel.GENERATIONS.size) { index ->
                val generation = com.example.pokemonmastersettracker.viewmodel.GENERATIONS[index]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGenerationSelect(generation.number) },
                    colors = CardDefaults.cardColors(
                        containerColor = PokemonColors.Surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = generation.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PokemonColors.Primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "#${generation.dexStart} - #${generation.dexEnd}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                        Text(
                            text = "→",
                            fontSize = 24.sp,
                            color = PokemonColors.Secondary
                        )
                    }
                }
            }
        }
    }
}
