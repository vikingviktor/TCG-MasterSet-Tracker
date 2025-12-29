package com.example.pokemonmastersettracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pokemonmastersettracker.data.models.Card
import com.example.pokemonmastersettracker.ui.theme.PokemonColors

@Composable
fun CardItem(
    card: Card,
    isOwned: Boolean = false,
    isFavorite: Boolean = false,
    onCardClick: (Card) -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(PokemonColors.Surface)
            .clickable { onCardClick(card) }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray)
        ) {
            card.image?.large?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = card.name,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image Available")
                }
            }

            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color.Red else Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = card.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    card.number?.let {
                        Text(
                            text = "No. $it",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    card.rarity?.let {
                        Text(
                            text = "Rarity: $it",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isOwned) PokemonColors.Primary else Color.LightGray
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = if (isOwned) "OWNED" else "MISSING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOwned) Color.White else Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun CardDetailView(
    card: Card,
    isOwned: Boolean = false,
    price: String? = null,
    condition: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray)
        ) {
            card.image?.large?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = card.name,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Text(
            text = card.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DetailRow(label = "Card Number", value = card.number ?: "N/A")
            DetailRow(label = "Card Type", value = card.supertype ?: "N/A")
            DetailRow(label = "Rarity", value = card.rarity ?: "N/A")
            DetailRow(label = "Artist", value = card.artist ?: "N/A")
            DetailRow(label = "Status", value = if (isOwned) "OWNED" else "MISSING")
            condition?.let { DetailRow(label = "Condition", value = it) }
            price?.let { DetailRow(label = "Price", value = it) }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, color = Color.Gray)
        Text(text = value, fontWeight = FontWeight.SemiBold)
    }
}
