package com.example.pokemonmastersettracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pokemonmastersettracker.data.models.Card
import com.example.pokemonmastersettracker.ui.theme.PokemonColors

@Composable
fun CardInfoDialog(
    card: Card,
    onDismiss: () -> Unit
) {
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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Card Details",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PokemonColors.Primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Variants Section
                Text(
                    text = "Variants Available",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PokemonColors.Primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (card.variants != null) {
                    val availableVariants = card.variants!!.getAvailableVariants()
                    if (availableVariants.isNotEmpty()) {
                        availableVariants.forEach { variant ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• $variant",
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "No variant information available",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    Text(
                        text = "No variant information available",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Pricing Section
                Text(
                    text = "Market Pricing",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PokemonColors.Primary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // TCG Player Pricing
                if (card.tcgplayer != null && card.tcgplayer!!.prices != null) {
                    Text(
                        text = "TCG Player (USD)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    val normalPrice = card.tcgplayer!!.prices!!["normal"]
                    val reversePrice = card.tcgplayer!!.prices!!["reverse"]
                    
                    // Normal pricing
                    if (normalPrice != null) {
                        PriceRow("Normal - Low:", normalPrice.low)
                        PriceRow("Normal - Mid:", normalPrice.mid)
                    }
                    
                    // Reverse pricing
                    if (reversePrice != null) {
                        PriceRow("Reverse - Low:", reversePrice.low)
                        PriceRow("Reverse - Mid:", reversePrice.mid)
                    }
                    
                    if (normalPrice == null && reversePrice == null) {
                        Text(
                            text = "No pricing data available",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    Text(
                        text = "No TCG Player pricing available",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // CardMarket Pricing
                if (card.cardmarket != null) {
                    Text(
                        text = "CardMarket (${card.cardmarket!!.unit ?: "EUR"})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    PriceRow("Low:", card.cardmarket!!.low)
                    PriceRow("Average:", card.cardmarket!!.avg)
                    
                    if (card.cardmarket!!.low == null && card.cardmarket!!.avg == null) {
                        Text(
                            text = "No pricing data available",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    Text(
                        text = "No CardMarket pricing available",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun PriceRow(label: String, price: Double?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Black
        )
        Text(
            text = if (price != null && price > 0) {
                String.format("$%.2f", price)
            } else {
                "N/A"
            },
            fontSize = 12.sp,
            color = if (price != null && price > 0) PokemonColors.Primary else Color.Gray,
            fontWeight = FontWeight.SemiBold
        )
    }
}
