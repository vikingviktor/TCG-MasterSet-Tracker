package com.example.pokemonmastersettracker.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Converts ThemeColors to Material3 ColorScheme
 */
fun ThemeColors.toMaterial3ColorScheme(): ColorScheme {
    return lightColorScheme(
        primary = this.primary,
        onPrimary = this.onPrimary,
        primaryContainer = this.primary.copy(alpha = 0.1f),
        onPrimaryContainer = this.primary,
        secondary = this.secondary,
        onSecondary = Color.White,
        secondaryContainer = this.secondary.copy(alpha = 0.1f),
        onSecondaryContainer = this.secondary,
        tertiary = this.accent,
        onTertiary = Color.Black,
        tertiaryContainer = this.accent.copy(alpha = 0.1f),
        onTertiaryContainer = this.accent,
        error = this.error,
        onError = Color.White,
        errorContainer = this.error.copy(alpha = 0.1f),
        onErrorContainer = this.error,
        background = this.background,
        onBackground = this.onBackground,
        surface = this.surface,
        onSurface = this.onSurface,
        surfaceVariant = this.surface.copy(alpha = 0.8f),
        onSurfaceVariant = this.onSurface.copy(alpha = 0.8f),
        outline = this.primary.copy(alpha = 0.5f),
        outlineVariant = this.primary.copy(alpha = 0.2f),
        scrim = Color.Black,
        inverseSurface = this.onSurface,
        inverseOnSurface = this.surface,
        inversePrimary = this.primary.copy(alpha = 0.8f)
    )
}
