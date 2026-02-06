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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.activity.compose.BackHandler
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.FileProvider
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.pokemonmastersettracker.ui.utils.exportWishlistAsImage
import com.example.pokemonmastersettracker.ui.utils.shareWishlistImage
import com.example.pokemonmastersettracker.ui.utils.exportWishlistAsCSV
import com.example.pokemonmastersettracker.ui.utils.exportWishlistAsText
import com.example.pokemonmastersettracker.ui.utils.shareFile

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
    
    // Handle device back button - close card details if open, otherwise system default
    BackHandler(enabled = selectedCardForDialog != null) {
        selectedCardForDialog = null
    }
    
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
            onToggleOwned = { condition, variant ->
                scope.launch {
                    cardViewModel.toggleCardOwnership(card.id, isCardOwned, condition, variant)
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
                viewModel = viewModel,
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
    viewModel: UserCollectionViewModel,
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
            totalValue = collectionUiState.totalValue,
            countVariants = collectionUiState.countVariants,
            onRefresh = onRefresh,
            onToggleVariantCounting = { viewModel.toggleVariantCounting() }
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
                                onRemove = {
                                    viewModel.markCardAsMissing(userCard.cardId)
                                    onRefresh()
                                },
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
    totalValue: Double = 0.0,
    countVariants: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    onToggleVariantCounting: (() -> Unit)? = null
) {
    var showSettingsDialog by remember { mutableStateOf(false) }
    
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
                    text = "Your Master Set",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Settings button
                    onToggleVariantCounting?.let {
                        IconButton(onClick = { showSettingsDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Collection Settings",
                                tint = PokemonColors.Primary
                            )
                        }
                    }
                    
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
            
            // Collection Value
            if (totalValue > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Est. Collection Value", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            "€${
                                DecimalFormat("#,##0.00").apply {
                                    roundingMode = RoundingMode.HALF_UP
                                }.format(totalValue)
                            }",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
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
    
    // Settings Dialog
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Collection Tracking Settings") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "How should variants be counted?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = if (countVariants) {
                            "✓ Currently counting variants\n\nWith this enabled:\n• Total = all available variants in master set\n• Owned = total variant entries you own\n\nExample: If you own Holo + Normal of the same card, owned count = 2"
                        } else {
                            "✓ Currently counting unique cards only\n\nWith this enabled:\n• Total = unique cards in master set\n• Owned = unique cards you own (ignoring variants)\n\nExample: If you own Holo + Normal of the same card, owned count = 1"
                        },
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onToggleVariantCounting?.invoke()
                        showSettingsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PokemonColors.Primary
                    )
                ) {
                    Text(if (countVariants) "Count Unique Cards Only" else "Count All Variants")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
                // Display variant if available
                if (userCard.variant != null) {
                    Text(
                        text = "Variant: ${userCard.variant}",
                        fontSize = 12.sp,
                        color = PokemonColors.Primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text("Condition: ${userCard.condition}", fontSize = 12.sp, color = Color.Gray)
                if (userCard.isGraded) {
                    Text(
                        "Graded: ${userCard.gradingCompany} - ${userCard.grade}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            // Card pricing section
            Column(
                modifier = Modifier.padding(end = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                // CardMarket pricing (EUR)
                val cardmarketPrice = card?.cardmarket?.avg
                if (cardmarketPrice != null) {
                    Text(
                        text = "CM: €${String.format("%.2f", cardmarketPrice)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2196F3)
                    )
                }
                
                // TCGPlayer pricing (USD)
                val tcgPrice = card?.tcgplayer?.prices?.get("normal")?.mid 
                    ?: card?.tcgplayer?.prices?.get("holofoil")?.mid
                    ?: card?.tcgplayer?.prices?.get("reverse")?.mid
                
                if (tcgPrice != null) {
                    Text(
                        text = "TCG: $${String.format("%.2f", tcgPrice)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4CAF50)
                    )
                }
                
                // Show N/A if no pricing available
                if (cardmarketPrice == null && tcgPrice == null) {
                    Text(
                        text = "Price: N/A",
                        fontSize = 11.sp,
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
    val context = LocalContext.current
    
    LaunchedEffect(userId, refreshTrigger) {
        viewModel.loadWishlist(userId)
    }
    
    var showExportMenu by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Wishlist",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Box {
                Button(
                    onClick = { showExportMenu = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PokemonColors.Primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export",
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Export")
                }
                
                DropdownMenu(
                    expanded = showExportMenu,
                    onDismissRequest = { showExportMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Export as Image (PNG)") },
                        onClick = {
                            showExportMenu = false
                            scope.launch {
                                try {
                                    val imageFile = exportWishlistAsImage(context, wishlistUiState.wishlistCards)
                                    if (imageFile != null) {
                                        shareWishlistImage(context, imageFile)
                                    } else {
                                        android.util.Log.e("WishlistExport", "Failed to create wishlist image")
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("WishlistExport", "Error exporting wishlist: ${e.message}", e)
                                }
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Export as CSV (Excel)") },
                        onClick = {
                            showExportMenu = false
                            scope.launch {
                                try {
                                    val csvFile = exportWishlistAsCSV(context, wishlistUiState.wishlistCards)
                                    if (csvFile != null) {
                                        shareFile(context, csvFile, "text/csv")
                                    } else {
                                        android.util.Log.e("WishlistExport", "Failed to create CSV file")
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("WishlistExport", "Error exporting wishlist: ${e.message}", e)
                                }
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Export as Text (TXT)") },
                        onClick = {
                            showExportMenu = false
                            scope.launch {
                                try {
                                    val textFile = exportWishlistAsText(context, wishlistUiState.wishlistCards)
                                    if (textFile != null) {
                                        shareFile(context, textFile, "text/plain")
                                    } else {
                                        android.util.Log.e("WishlistExport", "Failed to create text file")
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("WishlistExport", "Error exporting wishlist: ${e.message}", e)
                                }
                            }
                        }
                    )
                }
            }
        }
        
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
