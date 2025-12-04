package com.example.gamestoreapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.gamestoreapp.navigation.AppScreen
import com.example.gamestoreapp.ui.viewmodel.AuthViewModel
import com.example.gamestoreapp.ui.viewmodel.CartViewModel
import com.example.gamestoreapp.ui.viewmodel.GamesUiState
import com.example.gamestoreapp.ui.viewmodel.GamesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    navController: NavController,
    gameId: String?,
    gamesViewModel: GamesViewModel,
    cartViewModel: CartViewModel,
    authViewModel: AuthViewModel
) {
    val gamesState by gamesViewModel.gamesState.collectAsState()
    val game by gamesViewModel.selectedGame.collectAsState()
    val cartItems by cartViewModel.cartItems.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var quantity by remember { mutableIntStateOf(1) }
    var mainImageUrl by remember { mutableStateOf("") }

    LaunchedEffect(gameId, gamesState) {
        gamesViewModel.findGameById(gameId)
    }

    LaunchedEffect(game) {
        if (game != null) {
            // Lógica para priorizar la segunda imagen (Portada) o la primera
            mainImageUrl = game?.images?.getOrNull(1)?.url ?: game?.images?.firstOrNull()?.url ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalles",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Azul Profundo
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Acceso rápido al carrito
                    BadgedBox(
                        badge = {
                            if (cartItems.isNotEmpty()) {
                                Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                                    Text("${cartItems.sumOf { it.quantity }}")
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = { navController.navigate(AppScreen.CartScreen.route) }) {
                            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Carrito")
                        }
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
            when (gamesState) {
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
                        Text("Error al cargar los detalles.", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { gamesViewModel.fetchGames() }) {
                            Text("Reintentar")
                        }
                    }
                }
                is GamesUiState.Success -> {
                    val currentGame = game
                    if (currentGame == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Juego no encontrado.", color = MaterialTheme.colorScheme.onBackground)
                        }
                    } else {
                        LaunchedEffect(currentGame, cartItems) {
                            quantity = 1
                        }

                        // Lógica de galería (reordenar para poner portada primero si existe)
                        val galleryImages = remember(currentGame.images) {
                            val originalList = currentGame.images ?: emptyList()
                            if (originalList.size >= 2) {
                                listOf(originalList[1], originalList[0]) + originalList.drop(2)
                            } else {
                                originalList
                            }
                        }

                        val stockInCart = cartItems.find { it.product.id == currentGame.id }?.quantity ?: 0
                        val availableStock = currentGame.stock - stockInCart

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            // --- SECCIÓN HERO (Imagen Principal) ---
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(350.dp)
                                    .background(MaterialTheme.colorScheme.background)
                            ) {
                                AsyncImage(
                                    model = mainImageUrl,
                                    contentDescription = "Carátula de ${currentGame.name}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Degradado para que el texto sea legible
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                                                    MaterialTheme.colorScheme.background
                                                ),
                                                startY = 400f
                                            )
                                        )
                                )
                                // Título sobre la imagen
                                Text(
                                    text = currentGame.name,
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(16.dp)
                                )
                            }

                            // --- GALERÍA ---
                            if (galleryImages.isNotEmpty()) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    items(galleryImages) { image ->
                                        val isSelected = image.url == mainImageUrl
                                        AsyncImage(
                                            model = image.url,
                                            contentDescription = "Imagen de galería",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(90.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { mainImageUrl = image.url }
                                                .border(
                                                    width = if (isSelected) 3.dp else 0.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent, // Borde Cyan
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                        )
                                    }
                                }
                            }

                            // --- INFORMACIÓN Y ACCIONES ---
                            Column(modifier = Modifier.padding(24.dp)) {

                                // Precio y Stock
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$${currentGame.price}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary // Azul Profundo
                                    )

                                    val stockColor = when {
                                        availableStock <= 0 -> MaterialTheme.colorScheme.error
                                        availableStock <= 10 -> Color(0xFFFFAB00) // Naranja advertencia
                                        else -> Color(0xFF00E676) // Verde brillante
                                    }

                                    Surface(
                                        color = stockColor.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = if (availableStock > 0) "Stock: $availableStock" else "AGOTADO",
                                            color = stockColor,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Descripción
                                Text(
                                    "Descripción",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentGame.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 24.sp
                                )

                                Spacer(modifier = Modifier.height(32.dp))

                                // --- CONTROLES DE COMPRA ---
                                if (availableStock > 0) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Selector de Cantidad
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                                .padding(4.dp)
                                        ) {
                                            IconButton(
                                                onClick = { if (quantity > 1) quantity-- },
                                                enabled = quantity > 1
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = "Quitar")
                                            }

                                            Text(
                                                text = "$quantity",
                                                modifier = Modifier.padding(horizontal = 12.dp),
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium
                                            )

                                            IconButton(
                                                onClick = { if (quantity < availableStock) quantity++ },
                                                enabled = quantity < availableStock
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "Añadir")
                                            }
                                        }

                                        // Botón Añadir al Carrito
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    cartViewModel.addItemsToCart(currentGame, quantity, context)
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(56.dp), // Botón alto
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Text(
                                                "AÑADIR",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                letterSpacing = 1.sp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                } else {
                                    // Botón Deshabilitado (Agotado)
                                    Button(
                                        onClick = {},
                                        enabled = false,
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Text("PRODUCTO AGOTADO", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                // Espacio extra al final para que no se corte con la navegación por gestos
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}