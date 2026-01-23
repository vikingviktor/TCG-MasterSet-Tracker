package com.example.pokemonmastersettracker.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// Composition local for theme colors
val LocalThemeColors = compositionLocalOf { ThemeColorSchemes.Light }

object PokemonColors {
    // These are now accessed through LocalThemeColors in composables
    // Kept for backward compatibility - will be dynamically set
    var Primary = Color(0xFFFF5722)
    var PrimaryDark = Color(0xFFE64A19)
    var Secondary = Color(0xFF2196F3)
    var Accent = Color(0xFFFFEB3B)
    var Background = Color(0xFFFAFAFA)
    var Surface = Color(0xFFFFFFFF)
    var Error = Color(0xFFD32F2F)
    var OnPrimary = Color(0xFFFFFFFF)
    var OnSurface = Color(0xFF212121)
    var OnBackground = Color(0xFF212121)
    
    fun applyTheme(colors: ThemeColors) {
        Primary = colors.primary
        PrimaryDark = colors.primaryDark
        Secondary = colors.secondary
        Accent = colors.accent
        Background = colors.background
        Surface = colors.surface
        Error = colors.error
        OnPrimary = colors.onPrimary
        OnSurface = colors.onSurface
        OnBackground = colors.onBackground
    }
    
    // Pokemon type colors (theme-independent)
    val FireType = Color(0xFFFDA113)
    val WaterType = Color(0xFF87CEEB)
    val GrassType = Color(0xFF78C850)
    val ElectricType = Color(0xFFFFDD33)
    val PsychicType = Color(0xFFF85888)
    val NormalType = Color(0xFFA8A878)
    val FightingType = Color(0xFFC03028)
    val FlyingType = Color(0xFFA890F0)
    val PoisonType = Color(0xFFA040A0)
    val GroundType = Color(0xFFE0C068)
    val RockType = Color(0xFFB8A038)
    val BugType = Color(0xFFA8B820)
    val GhostType = Color(0xFF705898)
    val SteelType = Color(0xFFB8B8D0)
    val IceType = Color(0xFF98D8D8)
    val DragonType = Color(0xFF7038F8)
    val DarkType = Color(0xFF705848)
    val FairyType = Color(0xFFEE99AC)
}
