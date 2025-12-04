package com.example.gamestoreapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
// Importación necesaria para el modificador .clip()
import androidx.compose.ui.draw.clip
import com.example.gamestoreapp.data.ProductResponse
import com.example.gamestoreapp.navigation.AppScreen
import com.example.gamestoreapp.ui.viewmodel.AuthViewModel
import com.example.gamestoreapp.ui.viewmodel.GamesUiState
import com.example.gamestoreapp.ui.viewmodel.GamesViewModel
import com.example.gamestoreapp.ui.viewmodel.ProductEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductListScreen(
    navController: NavController,
    gamesViewModel: GamesViewModel,
    editViewModel: ProductEditViewModel,
    authViewModel: AuthViewModel
) {
    val gamesState by gamesViewModel.gamesState.collectAsState()
    val games = remember(gamesState) {
        if (gamesState is GamesUiState.Success) {
            (gamesState as GamesUiState.Success).games
        } else {
            emptyList()
        }
    }

    val editUiState = editViewModel.uiState
    val authToken by authViewModel.loginResult.collectAsState()
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<ProductResponse?>(null) }

    // Recargar la lista al entrar y manejar el éxito de las operaciones
    LaunchedEffect(Unit) {
        gamesViewModel.fetchGames()
    }

    LaunchedEffect(editUiState.saveSuccess) {
        if (editUiState.saveSuccess) {
            Toast.makeText(context, "¡Operación completada!", Toast.LENGTH_SHORT).show()
            gamesViewModel.fetchGames() // Refresca la lista
            editViewModel.resetState()
        }
    }

    // Dialogo de confirmación de eliminación
    if (showDeleteDialog && productToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Confirmar Eliminación",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = { Text("¿Estás seguro de que quieres eliminar el producto '${productToDelete?.name}'? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        authToken?.authToken?.let { token ->
                            editViewModel.deleteProduct(token, productToDelete!!.id)
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Gestionar Productos",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Azul Profundo
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(AppScreen.ProductEditScreen.createRoute(null))
                },
                containerColor = MaterialTheme.colorScheme.secondary, // Cyan Brillante
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Producto")
            }
        }
    ) { paddingValues ->
        // Manejo de estado visual (Carga, Error o Lista)
        Box(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Fondo Azul Oscuro
            .padding(paddingValues)
        ) {
            when (gamesState) {
                is GamesUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
                }
                is GamesUiState.Error -> {
                    Text("Error al cargar los juegos.", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                }
                is GamesUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(games) { game ->
                            // Estilo de tarjeta basado en si el producto está activo
                            val cardContainerColor = if (game.active) {
                                MaterialTheme.colorScheme.surface
                            } else {
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) // Más tenue si está inactivo
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = cardContainerColor),
                                elevation = CardDefaults.cardElevation(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Imagen de portada con fallback seguro
                                    AsyncImage(
                                        model = game.images?.getOrNull(1)?.url
                                            ?: game.images?.firstOrNull()?.url
                                            ?: "",
                                        contentDescription = "Imagen de ${game.name}",
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(8.dp)) // Corregido: clip ahora está definido
                                            .padding(end = 16.dp),
                                        contentScale = ContentScale.Crop
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = game.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        // Indicador de Stock
                                        Text(
                                            text = "Stock: ${game.stock}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.secondary // Cyan para resaltar stock
                                        )

                                        // Indicador de Estado Activo/Inactivo
                                        if (!game.active) {
                                            Text(
                                                text = "(INACTIVO)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Botones de Acción (Editar/Eliminar)
                                    Column(horizontalAlignment = Alignment.End) {
                                        Button(
                                            onClick = {
                                                navController.navigate(AppScreen.ProductEditScreen.createRoute(game.id.toString()))
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer // Azul Oscuro
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Editar")
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        TextButton(
                                            onClick = {
                                                productToDelete = game
                                                showDeleteDialog = true
                                            },
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error
                                            )
                                        ) {
                                            Text("Eliminar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}