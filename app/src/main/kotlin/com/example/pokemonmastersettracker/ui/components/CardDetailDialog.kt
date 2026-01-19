package com.example.pokemonmastersettracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.pokemonmastersettracker.data.models.Card
import com.example.pokemonmastersettracker.ui.theme.PokemonColors

@Composable
fun CardDetailDialog(
    card: Card,
    isOwned: Boolean,
    isInWishlist: Boolean,
    onDismiss: () -> Unit,
    onToggleOwned: (com.example.pokemonmastersettracker.data.models.CardCondition) -> Unit,
    onToggleWishlist: () -> Unit
) {
    var selectedCondition by remember { mutableStateOf(com.example.pokemonmastersettracker.data.models.CardCondition.NEAR_MINT) }
    var showConditionMenu by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }
                
                // Card Image
                AsyncImage(
                    model = card.image?.large,
                    contentDescription = card.name,
                    modifier = Modifier
                        .size(300.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Card Name
                Text(
                    text = card.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PokemonColors.Primary
                )
                
                // Card Set - show name and ID
                card.set?.let { set ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        set.name?.let { setName ->
                            Text(
                                text = setName,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                        // Show set ID below the name for easy reference
                        Text(
                            text = set.id,
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Card Number and Rarity
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#${card.number}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    card.rarity?.let { rarity ->
                        Text(
                            text = "•",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = rarity,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Condition Selector (only show when not owned)
                if (!isOwned) {
                    OutlinedButton(
                        onClick = { showConditionMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Condition: ${selectedCondition.name.replace("_", " ")}",
                            fontSize = 14.sp
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showConditionMenu,
                        onDismissRequest = { showConditionMenu = false }
                    ) {
                        com.example.pokemonmastersettracker.data.models.CardCondition.values().forEach { condition ->
                            DropdownMenuItem(
                                text = { Text(condition.name.replace("_", " ")) },
                                onClick = {
                                    selectedCondition = condition
                                    showConditionMenu = false
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Collection Button
                    Button(
                        onClick = { onToggleOwned(selectedCondition) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOwned) Color(0xFF4CAF50) else PokemonColors.Primary
                        )
                    ) {
                        Icon(
                            imageVector = if (isOwned) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isOwned) "In Collection" else "Add to Collection",
                            fontSize = 12.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Wishlist Button
                OutlinedButton(
                    onClick = onToggleWishlist,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isInWishlist) PokemonColors.Primary.copy(alpha = 0.1f) else Color.Transparent,
                        contentColor = if (isInWishlist) PokemonColors.Primary else Color.Gray
                    )
                ) {
                    Icon(
                        imageVector = if (isInWishlist) Icons.Default.Favorite else Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (isInWishlist) PokemonColors.Primary else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isInWishlist) "In Wishlist" else "Add to Wishlist",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
