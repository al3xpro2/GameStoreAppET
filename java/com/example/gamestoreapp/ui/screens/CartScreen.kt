package com.example.gamestoreapp.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.gamestoreapp.data.model.CartItem
import com.example.gamestoreapp.navigation.AppScreen
import com.example.gamestoreapp.ui.viewmodel.AuthViewModel
import com.example.gamestoreapp.ui.viewmodel.CartViewModel
import com.example.gamestoreapp.ui.viewmodel.GamesViewModel
import com.example.gamestoreapp.util.NotificationManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    gamesViewModel: GamesViewModel,
    authViewModel: AuthViewModel
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val subtotal by cartViewModel.subtotalAmount.collectAsState()
    val iva by cartViewModel.ivaAmount.collectAsState()
    val total by cartViewModel.totalAmount.collectAsState()

    val loginResult by authViewModel.loginResult.collectAsState()
    val authToken = loginResult?.authToken
    val loggedInUserEmail = loginResult?.email

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    val notificationManager = remember { NotificationManager(context) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(context, "Permission for notifications was denied.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirmar Compra") },
            text = { Text("¿Estás seguro de que quieres finalizar tu compra por un total de $$total? Se registrará a nombre de: ${loggedInUserEmail ?: "usuario no identificado"}") },
            confirmButton = {
                Button(
                    onClick = {
                        val token = authToken
                        val email = loggedInUserEmail

                        if (token != null && email != null) {
                            cartViewModel.placeOrder(
                                authToken = token,
                                userEmail = email,
                                onSuccess = {
                                    scope.launch {
                                        notificationManager.showPurchaseCompletedNotification()
                                        gamesViewModel.fetchGames()
                                        navController.navigate(AppScreen.CatalogScreen.route) {
                                            popUpTo(AppScreen.CatalogScreen.route) { inclusive = true }
                                        }
                                    }
                                },
                                onError = { errorMsg ->
                                    scope.launch {
                                        Toast.makeText(context, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                                    }
                                }
                            )
                        } else {
                            Toast.makeText(context, "Error de autenticación. Por favor, inicia sesión de nuevo.", Toast.LENGTH_LONG).show()
                        }
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Carrito de Compras",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (cartItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tu carrito está vacío",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(cartItems) { item ->
                        CartItemRow(
                            item = item,
                            onIncrease = { cartViewModel.increaseQuantity(item) },
                            onDecrease = { cartViewModel.decreaseQuantity(item) }
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 16.dp,
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        SummaryRow("Subtotal:", "$$subtotal")
                        SummaryRow("IVA (19%):", "$$iva")

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )

                        SummaryRow("TOTAL:", "$$total", isTotal = true)

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { showDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = authToken != null
                        ) {
                            Text(
                                "FINALIZAR COMPRA",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        if (authToken == null) {
                            Text(
                                "Debes iniciar sesión para comprar.",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(item: CartItem, onIncrease: () -> Unit, onDecrease: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.product.images?.getOrNull(1)?.url
                    ?: item.product.images?.firstOrNull()?.url
                    ?: "",
                contentDescription = item.product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$${item.product.price * item.quantity}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onDecrease,
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (item.quantity > 1) Icons.Default.Remove else Icons.Default.Delete,
                        contentDescription = "Disminuir",
                        tint = if (item.quantity == 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${item.quantity}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = onIncrease,
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Aumentar",
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, isTotal: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isTotal) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = if (isTotal) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isTotal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}