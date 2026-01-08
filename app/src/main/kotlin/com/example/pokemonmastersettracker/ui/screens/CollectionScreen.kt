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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pokemonmastersettracker.data.models.Card
import com.example.pokemonmastersettracker.ui.components.CardDetailDialog
import com.example.pokemonmastersettracker.ui.components.CardItem
import com.example.pokemonmastersettracker.ui.theme.PokemonColors
import com.example.pokemonmastersettracker.viewmodel.CardViewModel
import com.example.pokemonmastersettracker.viewmodel.UserCollectionViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.math.RoundingMode
import java.text.DecimalFormat
import androidx.compose.material3.ExperimentalMaterial3Api
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun CollectionScreen(
    viewModel: UserCollectionViewModel = hiltViewModel(),
    cardViewModel: CardViewModel = hiltViewModel(),
    userId: String
) {
    val collectionUiState by viewModel.collectionUiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Collection", "Wishlist")
    var selectedCardForDialog by remember { mutableStateOf<Card?>(null) }
    var isCardOwned by remember { mutableStateOf(false) }
    var isCardInWishlist by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Initialize the collection viewModel with userId
    LaunchedEffect(userId) {
        viewModel.setCurrentUser(userId)
    }
    
    // Refresh when refreshTrigger changes
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            viewModel.setCurrentUser(userId)
        }
    }
    
    // Show dialog when a card is selected
    selectedCardForDialog?.let { card ->
        CardDetailDialog(
            card = card,
            isOwned = isCardOwned,
            isInWishlist = isCardInWishlist,
            onDismiss = { 
                selectedCardForDialog = null
                refreshTrigger++ // Trigger refresh
            },
            onToggleOwned = { condition ->
                scope.launch {
                    cardViewModel.toggleCardOwnership(card.id, isCardOwned, condition)
                    // Wait a bit for database operation to complete
                    delay(100)
                    // Re-query the actual state from database
                    isCardOwned = cardViewModel.isCardOwned(card.id)
                }
            },
            onToggleWishlist = {
                scope.launch {
                    cardViewModel.toggleWishlist(card.id, isCardInWishlist)
                    // Wait a bit for database operation to complete
                    delay(100)
                    // Re-query the actual state from database
                    isCardInWishlist = cardViewModel.isInWishlist(card.id)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PokemonColors.Background)
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = PokemonColors.Surface
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> CollectionContent(
                collectionUiState = collectionUiState,
                isRefreshing = isRefreshing,
                refreshTrigger = refreshTrigger,
                onRefresh = {
                    scope.launch {
                        isRefreshing = true
                        viewModel.setCurrentUser(userId)
                        refreshTrigger++
                        delay(500)
                        isRefreshing = false
                    }
                },
                onCardClick = { card ->
                    selectedCardForDialog = card
                    scope.launch {
                        isCardOwned = cardViewModel.isCardOwned(card.id)
                        isCardInWishlist = cardViewModel.isInWishlist(card.id)
                    }
                }
            )
            1 -> WishlistContent(
                cardViewModel = cardViewModel,
                userId = userId,
                isRefreshing = isRefreshing,
                refreshTrigger = refreshTrigger,
                onRefresh = {
                    scope.launch {
                        isRefreshing = true
                        viewModel.setCurrentUser(userId)
                        refreshTrigger++
                        delay(500)
                        isRefreshing = false
                    }
                },
                onCardClick = { card ->
                    selectedCardForDialog = card
                    scope.launch {
                        isCardOwned = cardViewModel.isCardOwned(card.id)
                        isCardInWishlist = cardViewModel.isInWishlist(card.id)
                    }
                }
            )
        }
    }
}

@Composable
fun CollectionContent(
    collectionUiState: com.example.pokemonmastersettracker.viewmodel.UserCollectionUiState,
    isRefreshing: Boolean,
    refreshTrigger: Int,
    onRefresh: () -> Unit,
    onCardClick: (Card) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header with stats
        CollectionHeader(
            ownedCount = collectionUiState.ownedCount,
            totalCount = collectionUiState.totalCount,
            completionPercentage = collectionUiState.completionPercentage,
            onRefresh = onRefresh
        )

        // Card list
        when {
            collectionUiState.loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PokemonColors.Primary)
                }
            }

            collectionUiState.userCardsWithDetails.isEmpty() -> {
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing),
                    onRefresh = onRefresh
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No cards in your collection yet")
                    }
                }
            }

            else -> {
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing),
                    onRefresh = onRefresh
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(collectionUiState.userCardsWithDetails.size) { index ->
                            val (userCard, card) = collectionUiState.userCardsWithDetails[index]
                            CollectionCardItem(
                                userCard = userCard,
                                card = card,
                                onRemove = { /* Handle remove */ },
                                onClick = { card?.let { onCardClick(it) } }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CollectionHeader(
    ownedCount: Int,
    totalCount: Int,
    completionPercentage: Float,
    onRefresh: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Collection",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Refresh button
                onRefresh?.let {
                    Button(
                        onClick = it,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PokemonColors.Primary
                        )
                    ) {
                        Text("Refresh", fontSize = 12.sp)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Owned", fontSize = 12.sp, color = Color.Gray)
                    Text("$ownedCount", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Completion", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        "${
                            DecimalFormat("#.##").apply {
                                roundingMode = RoundingMode.HALF_UP
                            }.format(completionPercentage)
                        }%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PokemonColors.Primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total", fontSize = 12.sp, color = Color.Gray)
                    Text("$totalCount", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            LinearProgressIndicator(
                progress = completionPercentage / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = PokemonColors.Primary,
                trackColor = Color.LightGray
            )
        }
    }
}

@Composable
fun CollectionCardItem(
    userCard: com.example.pokemonmastersettracker.data.models.UserCard,
    card: Card?,
    onRemove: () -> Unit,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card?.name ?: "Card #${userCard.cardId}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = card?.set?.name ?: "Unknown Set",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                Text("Condition: ${userCard.condition}", fontSize = 12.sp, color = Color.Gray)
                if (userCard.isGraded) {
                    Text(
                        "Graded: ${userCard.gradingCompany} - ${userCard.grade}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = PokemonColors.Error
                )
            }
        }
    }
}

@Composable
fun WishlistContent(
    cardViewModel: CardViewModel,
    userId: String,
    isRefreshing: Boolean,
    refreshTrigger: Int,
    onRefresh: () -> Unit,
    onCardClick: (Card) -> Unit
) {
    val viewModel: UserCollectionViewModel = hiltViewModel()
    val wishlistUiState by viewModel.wishlistUiState.collectAsState()
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(userId, refreshTrigger) {
        viewModel.loadWishlist(userId)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Your Wishlist",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        when {
            wishlistUiState.loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PokemonColors.Primary)
                }
            }
            
            wishlistUiState.wishlistCards.isEmpty() -> {
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing),
                    onRefresh = onRefresh
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("No cards in your wishlist yet")
                            Text(
                                "Add cards from search to build your wishlist",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
            
            else -> {
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing),
                    onRefresh = onRefresh
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(wishlistUiState.wishlistCards) { card ->
                            var cardOwned by remember { mutableStateOf(false) }
                            var cardInWishlist by remember { mutableStateOf(true) }
                            
                            LaunchedEffect(card.id, refreshTrigger) {
                                cardOwned = cardViewModel.isCardOwned(card.id)
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
        }
    }
}
