package com.example.pokemonmastersettracker.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.pokemonmastersettracker.ui.theme.PokemonColors

object TypeColorMapper {
    fun getTypeColor(type: String): Color {
        return when (type.lowercase()) {
            "fire" -> PokemonColors.FireType
            "water" -> PokemonColors.WaterType
            "grass" -> PokemonColors.GrassType
            "electric" -> PokemonColors.ElectricType
            "psychic" -> PokemonColors.PsychicType
            "normal" -> PokemonColors.NormalType
            "fighting" -> PokemonColors.FightingType
            "flying" -> PokemonColors.FlyingType
            "poison" -> PokemonColors.PoisonType
            "ground" -> PokemonColors.GroundType
            "rock" -> PokemonColors.RockType
            "bug" -> PokemonColors.BugType
            "ghost" -> PokemonColors.GhostType
            "steel" -> PokemonColors.SteelType
            "ice" -> PokemonColors.IceType
            "dragon" -> PokemonColors.DragonType
            "dark" -> PokemonColors.DarkType
            "fairy" -> PokemonColors.FairyType
            else -> Color.Gray
        }
    }
}

object CardPriceFormatter {
    fun formatPrice(price: Double?): String {
        return if (price != null) {
            String.format("$%.2f", price)
        } else {
            "N/A"
        }
    }

    fun formatPriceRange(low: Double?, high: Double?): String {
        return when {
            low != null && high != null -> "${formatPrice(low)} - ${formatPrice(high)}"
            low != null -> "${formatPrice(low)}+"
            high != null -> "Up to ${formatPrice(high)}"
            else -> "N/A"
        }
    }
}

object StringExtensions {
    fun String.capitalizeWords(): String {
        return this.split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    fun String.toTitleCase(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
