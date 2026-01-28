package com.example.pokemonmastersettracker.ui.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "theme_preferences")

enum class AppTheme(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    POKEMON_RED("Pokemon Red"),
    POKEMON_BLUE("Pokemon Blue"),
    MIDNIGHT("Midnight")
}

data class ThemeColors(
    val primary: Color,
    val primaryDark: Color,
    val secondary: Color,
    val accent: Color,
    val background: Color,
    val surface: Color,
    val error: Color,
    val onPrimary: Color,
    val onSurface: Color,
    val onBackground: Color
)

object ThemeColorSchemes {
    val Light = ThemeColors(
        primary = Color(0xFFFF5722),
        primaryDark = Color(0xFFE64A19),
        secondary = Color(0xFF2196F3),
        accent = Color(0xFFFFEB3B),
        background = Color(0xFFFAFAFA),
        surface = Color(0xFFFFFFFF),
        error = Color(0xFFD32F2F),
        onPrimary = Color(0xFFFFFFFF),
        onSurface = Color(0xFF212121),
        onBackground = Color(0xFF212121)
    )
    
    val Dark = ThemeColors(
        primary = Color(0xFFFF7043),
        primaryDark = Color(0xFFE64A19),
        secondary = Color(0xFF42A5F5),
        accent = Color(0xFFFFEB3B),
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        error = Color(0xFFCF6679),
        onPrimary = Color(0xFF000000),
        onSurface = Color(0xFFE0E0E0),
        onBackground = Color(0xFFE0E0E0)
    )
    
    val PokemonRed = ThemeColors(
        primary = Color(0xFFE63946),
        primaryDark = Color(0xFFC1121F),
        secondary = Color(0xFFF77F00),
        accent = Color(0xFFFCBF49),
        background = Color(0xFFFFF8F0),
        surface = Color(0xFFFFFFFF),
        error = Color(0xFFD32F2F),
        onPrimary = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1D3557),
        onBackground = Color(0xFF1D3557)
    )
    
    val PokemonBlue = ThemeColors(
        primary = Color(0xFF1E88E5),
        primaryDark = Color(0xFF1565C0),
        secondary = Color(0xFF26C6DA),
        accent = Color(0xFFFFCA28),
        background = Color(0xFFF0F8FF),
        surface = Color(0xFFFFFFFF),
        error = Color(0xFFD32F2F),
        onPrimary = Color(0xFFFFFFFF),
        onSurface = Color(0xFF263238),
        onBackground = Color(0xFF263238)
    )
    
    val Midnight = ThemeColors(
        primary = Color(0xFFB388FF), // lighter purple
        primaryDark = Color(0xFF7C4DFF), // lighter dark purple
        secondary = Color(0xFF00BCD4),
        accent = Color(0xFFFF4081),
        background = Color(0xFF0A0E27),
        surface = Color(0xFF292C4D), // lighter surface
        error = Color(0xFFCF6679),
        onPrimary = Color(0xFF1A1F3A),
        onSurface = Color(0xFFE0E0E0),
        onBackground = Color(0xFFE0E0E0)
    )
    
    fun getThemeColors(theme: AppTheme): ThemeColors {
        return when (theme) {
            AppTheme.LIGHT -> Light
            AppTheme.DARK -> Dark
            AppTheme.POKEMON_RED -> PokemonRed
            AppTheme.POKEMON_BLUE -> PokemonBlue
            AppTheme.MIDNIGHT -> Midnight
        }
    }
}

@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val THEME_KEY = stringPreferencesKey("app_theme")
    
    val currentTheme: Flow<AppTheme> = context.dataStore.data.map { preferences ->
        val themeName = preferences[THEME_KEY] ?: AppTheme.LIGHT.name
        try {
            AppTheme.valueOf(themeName)
        } catch (e: IllegalArgumentException) {
            AppTheme.LIGHT
        }
    }
    
    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }
}
