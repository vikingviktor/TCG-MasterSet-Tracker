package com.example.pokemonmastersettracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pokemonmastersettracker.ui.screens.CollectionScreen
import com.example.pokemonmastersettracker.ui.screens.FavoritesScreen
import com.example.pokemonmastersettracker.ui.screens.HomeScreen
import com.example.pokemonmastersettracker.ui.screens.LoginScreen
import com.example.pokemonmastersettracker.ui.theme.PokemonColors
import com.example.pokemonmastersettracker.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PokemonTrackerApp()
        }
    }
}

@Composable
fun PokemonTrackerApp(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authUiState by authViewModel.authUiState.collectAsState()

    if (!authUiState.isLoggedIn) {
        LoginScreen(
            viewModel = authViewModel,
            onLoginSuccess = { /* Navigate or update UI */ }
        )
    } else {
        MainAppScreen(userId = authUiState.currentUser?.id ?: "")
    }
}

@Composable
fun MainAppScreen(userId: String) {
    var selectedNavItem by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = PokemonColors.Background,
        bottomBar = {
            NavigationBar(
                containerColor = PokemonColors.Surface,
                contentColor = PokemonColors.OnSurface
            ) {
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Filled.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home") },
                    selected = selectedNavItem == 0,
                    onClick = { selectedNavItem = 0 }
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = "Favorites"
                        )
                    },
                    label = { Text("Favorites") },
                    selected = selectedNavItem == 1,
                    onClick = { selectedNavItem = 1 }
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Collection"
                        )
                    },
                    label = { Text("Collection") },
                    selected = selectedNavItem == 2,
                    onClick = { selectedNavItem = 2 }
                )
            }
        }
    ) { paddingValues ->
        when (selectedNavItem) {
            0 -> HomeScreen(
                onCardClick = { cardId ->
                    // Handle card click - show details
                }
            )

            1 -> FavoritesScreen(userId = userId)

            2 -> CollectionScreen(userId = userId)
        }
    }
}
