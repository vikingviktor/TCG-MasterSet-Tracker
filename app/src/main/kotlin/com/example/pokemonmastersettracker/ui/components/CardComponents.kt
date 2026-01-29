package com.example.pokemonmastersettracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
    val borderColor = if (isOwned) Color(0xFF4CAF50) else Color(0xFFEF5350) // Green for owned, Red for not owned
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 3.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
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
            AsyncImage(
                model = card.image?.large ?: "file:///android_asset/cards/SubstituteImgCard.png",
                contentDescription = card.name,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            
            // Owned checkmark
            if (isOwned) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(32.dp)
                        .background(Color(0xFF4CAF50), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Owned",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
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
                maxLines = 1,
                color = PokemonColors.OnSurface
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }
            
            // Status banner or pricing at bottom
            if (isOwned) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF4CAF50))
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "OWNED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                // Show pricing for missing cards
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(
                            width = 2.dp,
                            color = Color(0xFFEF5350),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(vertical = 6.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // CardMarket price
                    card.cardmarket?.avg?.let { price ->
                        Text(
                            text = "CM: €${String.format("%.2f", price)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2196F3)
                        )
                    }
                    
                    // TCGPlayer price
                    val tcgPrice = card.tcgplayer?.prices?.get("normal")?.mid 
                        ?: card.tcgplayer?.prices?.get("holofoil")?.mid
                        ?: card.tcgplayer?.prices?.get("reverse")?.mid
                    
                    tcgPrice?.let { price ->
                        Text(
                            text = "TCG: $${String.format("%.2f", price)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    
                    // Show "Price: N/A" if no pricing available
                    if (card.cardmarket?.avg == null && tcgPrice == null) {
                        Text(
                            text = "Price: N/A",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
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
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .placeholder(coil.base.R.drawable.avd_hide_password)
                        .error(coil.base.R.drawable.avd_hide_password)
                        .build(),
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
            modifier = Modifier.padding(top = 16.dp),
            color = PokemonColors.OnSurface
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
