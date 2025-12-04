package com.example.gamestoreapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.gamestoreapp.data.ProductResponse
import com.example.gamestoreapp.navigation.AppScreen
import com.example.gamestoreapp.ui.viewmodel.AuthViewModel
import com.example.gamestoreapp.ui.viewmodel.CartViewModel
import com.example.gamestoreapp.ui.viewmodel.GamesUiState
import com.example.gamestoreapp.ui.viewmodel.GamesViewModel
import com.example.gamestoreapp.util.ShakeDetector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    navController: NavController,
    gamesViewModel: GamesViewModel,
    cartViewModel: CartViewModel,
    authViewModel: AuthViewModel
) {
    val gamesState by gamesViewModel.gamesState.collectAsState()
    val cartItems by cartViewModel.cartItems.collectAsState()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val shakeDetector = ShakeDetector(context) {
            gamesViewModel.fetchGames()
        }
        shakeDetector.start()

        onDispose {
            shakeDetector.stop()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Catálogo de Juegos",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Azul Profundo
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // Badge del Carrito con estilo Cyan
                    BadgedBox(
                        badge = {
                            if (cartItems.isNotEmpty()) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.secondary, // Cyan
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ) {
                                    Text("${cartItems.sumOf { it.quantity }}")
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = {
                            navController.navigate(AppScreen.CartScreen.route)
                        }) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Carrito de compras"
                            )
                        }
                    }
                    IconButton(onClick = {
                        authViewModel.resetLoginState()
                        navController.navigate(AppScreen.LoginScreen.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar Sesión"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // Fondo General Oscuro
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            when (val currentState = gamesState) {
                is GamesUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is GamesUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error al cargar los juegos.",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { gamesViewModel.fetchGames() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
                is GamesUiState.Success -> {
                    val allGames = currentState.games
                    // Filtra para mostrar solo los activos, a menos que no haya ninguno
                    val gamesToShow = if (allGames.any { it.active }) {
                        allGames.filter { it.active }
                    } else {
                        allGames
                    }

                    if (gamesToShow.isEmpty()) {
                        Text(
                            text = "No hay juegos disponibles en este momento.",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(gamesToShow) { game ->
                                GameCard(game = game) {
                                    navController.navigate(
                                        "${AppScreen.DetailScreen.route.substringBeforeLast("/")}/${game.id}"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameCard(
    game: ProductResponse,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Gris azulado oscuro
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column {
            // Imagen de portada con tu lógica personalizada
            AsyncImage(
                model = game.images?.getOrNull(1)?.url ?: game.images?.firstOrNull()?.url ?: "",
                contentDescription = "Carátula de ${game.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
                    .background(Color.Black) // Fondo negro mientras carga
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Precio estilizado "Gamer" (Monospace y Azul Profundo)
                Text(
                    text = "$${game.price}",
                    style = MaterialTheme.typography.labelLarge, // Fuente técnica
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary // Azul Profundo
                )
            }
        }
    }
}