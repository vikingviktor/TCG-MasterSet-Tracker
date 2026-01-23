package com.example.pokemonmastersettracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pokemonmastersettracker.ui.screens.ApiTestScreen
import com.example.pokemonmastersettracker.ui.screens.CollectionScreen
import com.example.pokemonmastersettracker.ui.screens.FavoritesScreen
import com.example.pokemonmastersettracker.ui.screens.HomeScreen
import com.example.pokemonmastersettracker.ui.theme.PokemonColors
import com.example.pokemonmastersettracker.viewmodel.CardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PokemonTrackerAppScreen()
        }
    }
}

@Composable
fun PokemonTrackerAppScreen() {
    // For testing, skip login and go straight to home
    // TODO: Implement proper user authentication when ready
    var currentScreen by remember { mutableIntStateOf(0) }
    val homeViewModel: CardViewModel = hiltViewModel()

    MaterialTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = PokemonColors.Background,
            bottomBar = {
                NavigationBar(
                    containerColor = PokemonColors.Surface,
                    contentColor = PokemonColors.OnSurface
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentScreen == 0,
                        onClick = { 
                            if (currentScreen == 0) {
                                // Already on Home screen, refresh to generations
                                homeViewModel.clearSelection()
                                homeViewModel.clearGeneration()
                            }
                            currentScreen = 0 
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Favorite, contentDescription = "Collection") },
                        label = { Text("Collection") },
                        selected = currentScreen == 1,
                        onClick = { currentScreen = 1 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Person, contentDescription = "Favorites") },
                        label = { Text("Favorites") },
                        selected = currentScreen == 2,
                        onClick = { currentScreen = 2 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "API Test") },
                        label = { Text("API Test") },
                        selected = currentScreen == 3,
                        onClick = { currentScreen = 3 }
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (currentScreen) {
                    0 -> HomeScreen(viewModel = homeViewModel)
                    1 -> CollectionScreen(userId = "test-user")
                    2 -> FavoritesScreen()
                    3 -> ApiTestScreen()
                }
            }
        }
    }
}
