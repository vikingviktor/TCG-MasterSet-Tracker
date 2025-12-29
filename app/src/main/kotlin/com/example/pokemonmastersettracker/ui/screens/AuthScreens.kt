package com.example.pokemonmastersettracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pokemonmastersettracker.ui.theme.PokemonColors
import com.example.pokemonmastersettracker.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    val authUiState by viewModel.authUiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PokemonColors.Background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Pokemon Master Set Tracker",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = PokemonColors.Primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            label = { Text("Email") },
            singleLine = true
        )

        Button(
            onClick = { viewModel.login(email) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = PokemonColors.Primary
            )
        ) {
            Text("Login")
        }

        when {
            authUiState.loading -> {
                CircularProgressIndicator(
                    color = PokemonColors.Primary,
                    modifier = Modifier.padding(16.dp)
                )
            }

            authUiState.error != null -> {
                Text(
                    text = "Error: ${authUiState.error}",
                    color = PokemonColors.Error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            authUiState.isLoggedIn -> {
                onLoginSuccess()
            }
        }
    }
}

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onRegisterSuccess: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    val authUiState by viewModel.authUiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PokemonColors.Background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = PokemonColors.Primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            label = { Text("Email") },
            singleLine = true
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            label = { Text("Username") },
            singleLine = true
        )

        Button(
            onClick = { viewModel.register(email, username) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = PokemonColors.Primary
            )
        ) {
            Text("Register")
        }

        when {
            authUiState.loading -> {
                CircularProgressIndicator(
                    color = PokemonColors.Primary,
                    modifier = Modifier.padding(16.dp)
                )
            }

            authUiState.error != null -> {
                Text(
                    text = "Error: ${authUiState.error}",
                    color = PokemonColors.Error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            authUiState.isLoggedIn -> {
                onRegisterSuccess()
            }
        }
    }
}
