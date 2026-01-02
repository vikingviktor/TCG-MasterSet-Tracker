package com.example.pokemonmastersettracker.ui.screens

import androidx.compose.foundation.background
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
            onToggleOwned = {
                cardViewModel.toggleCardOwnership(card.id, isCardOwned)
                isCardOwned = !isCardOwned
            },
            onToggleWishlist = {
                cardViewModel.toggleWishlist(card.id, isCardInWishlist)
                isCardInWishlist = !isCardInWishlist
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
            completionPercentage = collectionUiState.completionPercentage
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

            collectionUiState.userCards.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No cards in your collection yet")
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
                        items(collectionUiState.userCards.size) { index ->
                            val userCard = collectionUiState.userCards[index]
                            CollectionCardItem(
                                userCard = userCard,
                                onRemove = { /* Handle remove */ }
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
    completionPercentage: Float
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
            Text(
                text = "Your Collection",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

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
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Card #${userCard.cardId}", fontWeight = FontWeight.Bold)
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
    // Get wishlist cards from repository
    var wishlistCardIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var cards by remember { mutableStateOf<List<Card>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(refreshTrigger) {
        loading = true
        try {
            // This would need to be implemented in the ViewModel
            // For now, we'll show a placeholder
            loading = false
        } catch (e: Exception) {
            loading = false
        }
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
            loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PokemonColors.Primary)
                }
            }
            
            cards.isEmpty() -> {
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cards) { card ->
                        var cardOwned by remember { mutableStateOf(false) }
                        var cardInWishlist by remember { mutableStateOf(true) }
                        
                        LaunchedEffect(card.id) {
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
