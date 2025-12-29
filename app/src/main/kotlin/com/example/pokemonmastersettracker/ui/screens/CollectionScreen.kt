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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pokemonmastersettracker.ui.theme.PokemonColors
import com.example.pokemonmastersettracker.viewmodel.UserCollectionViewModel
import java.math.RoundingMode
import java.text.DecimalFormat

@Composable
fun CollectionScreen(
    viewModel: UserCollectionViewModel = hiltViewModel(),
    userId: String
) {
    val collectionUiState by viewModel.collectionUiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PokemonColors.Background)
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(collectionUiState.userCards) { userCard ->
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
