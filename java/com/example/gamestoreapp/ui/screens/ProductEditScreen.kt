package com.example.gamestoreapp.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.gamestoreapp.ui.viewmodel.AuthViewModel
import com.example.gamestoreapp.ui.viewmodel.GamesUiState
import com.example.gamestoreapp.ui.viewmodel.GamesViewModel
import com.example.gamestoreapp.ui.viewmodel.ProductEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditScreen(
    navController: NavController,
    productId: String?,
    gamesViewModel: GamesViewModel,
    editViewModel: ProductEditViewModel,
    authViewModel: AuthViewModel
) {
    val uiState by remember { derivedStateOf { editViewModel.uiState } }
    // CORRECCIÓN 1: Simplificamos el acceso al StateFlow
    val gamesState by gamesViewModel.gamesState.collectAsState()
    val games = remember(gamesState) {
        if (gamesState is GamesUiState.Success) {
            (gamesState as GamesUiState.Success).games
        } else {
            emptyList()
        }
    }

    // CORRECCIÓN 2: Simplificamos el acceso al StateFlow
    val authToken = authViewModel.loginResult.collectAsState().value?.authToken

    val context = LocalContext.current
    val isEditing = productId != null
    val title = if (isEditing) "Editar Producto" else "Añadir Producto"

    var dataLoaded by remember { mutableStateOf(false) }

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            editViewModel.onImageSelected(uri)
        }
    )

    // Lógica de Carga de Datos (Usando la lista 'games')
    LaunchedEffect(key1 = games.isNotEmpty(), key2 = productId) {
        if (isEditing && !dataLoaded && games.isNotEmpty()) {
            val productToEdit = games.find { it.id.toString() == productId }
            if (productToEdit != null) {
                editViewModel.loadProduct(productToEdit)
                dataLoaded = true
            }
        } else if (!isEditing && !dataLoaded) {
            editViewModel.resetState()
            dataLoaded = true
        }
        // Si estamos editando y la lista aún no carga, forzamos la carga.
        if (isEditing && games.isEmpty()) {
            gamesViewModel.fetchGames()
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "¡Operación completada!", Toast.LENGTH_SHORT).show()
            gamesViewModel.fetchGames()
            navController.popBackStack()
            editViewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
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
        // Mostramos carga si estamos editando y los datos no se han cargado (dataLoaded=false)
        if (isEditing && !dataLoaded) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues).background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            // Formulario Visible
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // --- SECCIÓN DE IMAGEN ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .clickable {
                            singlePhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val imageToShow = uiState.selectedImageUri ?: editViewModel.getOriginalImageUrl()

                    if (imageToShow != null) {
                        AsyncImage(
                            model = imageToShow,
                            contentDescription = "Imagen seleccionada",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cambiar", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color.White)
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Toca para subir imagen", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // --- CAMPOS DE TEXTO ---
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { editViewModel.onNameChange(it) },
                    label = { Text("Nombre del Producto") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { editViewModel.onDescriptionChange(it) },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = uiState.price,
                        onValueChange = { editViewModel.onPriceChange(it) },
                        label = { Text("Precio") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    OutlinedTextField(
                        value = uiState.stock,
                        onValueChange = { editViewModel.onStockChange(it) },
                        label = { Text("Stock") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // --- SWITCH ACTIVO ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Estado del Producto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(if (uiState.active) "Visible en catálogo" else "Oculto para clientes", style = MaterialTheme.typography.bodySmall, color = if (uiState.active) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = uiState.active,
                            onCheckedChange = { editViewModel.onActiveChange(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primaryContainer)
                        )
                    }
                }

                // Mensaje de Error
                uiState.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                // --- BOTÓN GUARDAR ---
                Button(
                    onClick = {
                        authToken?.let { token ->
                            editViewModel.saveProduct(token, context)
                        } ?: run { Toast.makeText(context, "Error de autenticación", Toast.LENGTH_SHORT).show() }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("GUARDAR CAMBIOS", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
            }
        }
    }
}