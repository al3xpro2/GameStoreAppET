package com.example.gamestoreapp.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gamestoreapp.data.GameRepository
import com.example.gamestoreapp.data.ProductRequest
import com.example.gamestoreapp.data.ProductResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

data class ProductEditUiState(
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val stock: String = "",
    val active: Boolean = true,
    val selectedImageUri: Uri? = null, // URI temporal de la nueva imagen seleccionada
    val isLoading: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

class ProductEditViewModel(private val gameRepository: GameRepository) : ViewModel() {

    var uiState by mutableStateOf(ProductEditUiState())
        private set

    private var editingProductId: Int? = null
    // Guardamos la URL original (si estamos editando) para mostrarla si el usuario no sube una nueva
    private var originalImageUrl: String? = null

    fun loadProduct(product: ProductResponse?) {
        if (product != null) {
            editingProductId = product.id
            // Intentamos obtener la imagen principal (segunda o primera de la lista)
            originalImageUrl = product.images?.getOrNull(1)?.url ?: product.images?.firstOrNull()?.url

            uiState = uiState.copy(
                name = product.name,
                description = product.description,
                price = product.price.toString(),
                stock = product.stock.toString(),
                active = product.active,
                selectedImageUri = null // Al cargar, no hay imagen "nueva" seleccionada aún
            )
        } else {
            editingProductId = null
            originalImageUrl = null
            uiState = ProductEditUiState()
        }
    }

    fun onNameChange(newName: String) { uiState = uiState.copy(name = newName) }
    fun onDescriptionChange(newDescription: String) { uiState = uiState.copy(description = newDescription) }
    fun onPriceChange(newPrice: String) { uiState = uiState.copy(price = newPrice) }
    fun onStockChange(newStock: String) { uiState = uiState.copy(stock = newStock) }
    fun onActiveChange(newActive: Boolean) { uiState = uiState.copy(active = newActive) }

    // Función llamada cuando el usuario selecciona una foto de la galería
    fun onImageSelected(uri: Uri?) { uiState = uiState.copy(selectedImageUri = uri) }

    fun saveProduct(authToken: String, context: Context) {
        if (uiState.isLoading) return

        val priceInt = uiState.price.toIntOrNull()
        val stockInt = uiState.stock.toIntOrNull()

        if (uiState.name.isBlank() || priceInt == null || stockInt == null) {
            uiState = uiState.copy(error = "Nombre, precio y stock no pueden estar vacíos.")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            // 1. Procesar Imagen: Si hay una nueva seleccionada, la convertimos a Base64
            val base64Image = if (uiState.selectedImageUri != null) {
                uriToBase64(context, uiState.selectedImageUri!!)
            } else {
                null
            }

            // 2. Crear Request
            val productRequest = ProductRequest(
                name = uiState.name,
                description = uiState.description,
                price = priceInt,
                stock = stockInt,
                active = uiState.active,
                image_base64 = base64Image // Enviamos el string codificado (o null si no cambió)
            )

            val result: ProductResponse?

            // 3. Enviar a Xano
            if (editingProductId != null) {
                result = gameRepository.updateProduct(authToken, editingProductId!!, productRequest)
            } else {
                result = gameRepository.createProduct(authToken, productRequest)
            }

            uiState = uiState.copy(
                isLoading = false,
                saveSuccess = result != null,
                error = if (result == null) "Error al guardar. Verifica la conexión o la API." else null
            )
        }
    }

    fun deleteProduct(authToken: String, productId: Int) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            val success = gameRepository.deleteProduct(authToken, productId)
            uiState = uiState.copy(
                isLoading = false,
                saveSuccess = success,
                error = if (!success) "Error al eliminar el producto." else null
            )
        }
    }

    fun resetState() {
        uiState = ProductEditUiState()
        editingProductId = null
        originalImageUrl = null
    }

    // Función auxiliar para que la UI sepa qué imagen mostrar (la original o un placeholder)
    fun getOriginalImageUrl(): String? {
        // Si hay una URI nueva seleccionada, la usamos para mostrar la previsualización.
        // Si no, usamos la URL original del producto.
        // Convertimos la URI a String para que AsyncImage la pueda manejar si fue seleccionada.
        return uiState.selectedImageUri?.toString() ?: originalImageUrl
    }

    // --- LÓGICA DE IMAGEN (Conversión y Compresión) ---

    // Convierte una URI (archivo del teléfono) a un String Base64 seguro para subir
    private suspend fun uriToBase64(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Redimensionamos la imagen para que no sea gigante (máx 800px)
            val scaledBitmap = scaleBitmapDown(bitmap, 800)

            val outputStream = ByteArrayOutputStream()
            // Comprimimos a JPEG con calidad 70%
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val bytes = outputStream.toByteArray()

            // Convertimos a String Base64 sin saltos de línea
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Redimensiona manteniendo la proporción (aspect ratio)
    private fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        var resizedWidth = originalWidth
        var resizedHeight = originalHeight

        if (originalHeight > originalWidth) {
            if (originalHeight > maxDimension) {
                resizedHeight = maxDimension
                resizedWidth = (originalWidth * (maxDimension.toFloat() / originalHeight)).toInt()
            }
        } else {
            if (originalWidth > maxDimension) {
                resizedWidth = maxDimension
                resizedHeight = (originalHeight * (maxDimension.toFloat() / originalWidth)).toInt()
            }
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false)
    }
}

class ProductEditViewModelFactory(private val gameRepository: GameRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductEditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductEditViewModel(gameRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}