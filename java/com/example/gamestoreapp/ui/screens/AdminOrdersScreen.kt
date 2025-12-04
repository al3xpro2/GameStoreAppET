package com.example.gamestoreapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gamestoreapp.data.OrderResponse
import com.example.gamestoreapp.ui.viewmodel.AdminOrdersViewModel
import com.example.gamestoreapp.ui.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    ordersViewModel: AdminOrdersViewModel
) {
    val orders by ordersViewModel.orders.collectAsState()
    val loginResult by authViewModel.loginResult.collectAsState()
    val authToken = loginResult?.authToken

    LaunchedEffect(authToken) {
        authToken?.let { token ->
            ordersViewModel.fetchOrders(token)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Gestión de Órdenes",
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background) // Fondo Azul Oscuro
                .padding(paddingValues)
        ) {
            if (orders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Sin órdenes recientes",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { authToken?.let { ordersViewModel.fetchOrders(it) } }) {
                            Text("Recargar")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(orders) { order ->
                        AdminOrderCard(
                            order = order,
                            onStatusChange = { newStatus ->
                                authToken?.let { token ->
                                    ordersViewModel.updateOrderStatus(token, order.id, newStatus)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminOrderCard(
    order: OrderResponse,
    onStatusChange: (String) -> Unit
) {
    val statusColor = when (order.status.lowercase()) {
        "pendiente" -> Color(0xFFFFAB00) // Naranja (Advertencia)
        "enviado" -> Color(0xFF00B4D8)   // Cyan (En Proceso)
        "entregado" -> Color(0xFF38B000) // Verde (Éxito)
        "rechazado" -> Color(0xFFD00000) // Rojo (Error)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // Gris azulado oscuro
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Encabezado: ID y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ORDEN #${order.id}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = order.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Datos del Cliente y Fecha
            val formattedDate = remember(order.createdAt) {
                val timestamp = if (order.createdAt < 10000000000L) order.createdAt * 1000 else order.createdAt
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
            }

            Text("Cliente: ${order.userEmail}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Fecha: $formattedDate", color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(12.dp))

            // Lista de Productos
            Text("Productos:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            order.productsBought.forEach { product ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("• ${product.name} (x${product.quantity})", color = MaterialTheme.colorScheme.onSurface)
                    // ✅ NOTA: Ahora product.price es Double, resolviendo el error 14.24
                    Text("$${product.price * product.quantity}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Total
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "TOTAL: $${order.total}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary // Azul Profundo
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de Acción (Cambiar Estado)
            Text("Cambiar Estado:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (order.status.lowercase() == "pendiente") {
                    Button(
                        onClick = { onStatusChange("enviado") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary), // Cyan Brillante
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ENVIAR", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { onStatusChange("rechazado") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("RECHAZAR", fontSize = 12.sp)
                    }
                } else if (order.status.lowercase() == "enviado") {
                    Button(
                        onClick = { onStatusChange("entregado") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38B000)), // Verde Success
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("FINALIZAR", fontSize = 12.sp)
                    }
                } else {
                    Text(
                        text = "Orden Finalizada",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}