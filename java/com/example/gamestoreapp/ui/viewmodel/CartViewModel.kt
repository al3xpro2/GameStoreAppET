package com.example.gamestoreapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gamestoreapp.data.CartRepository
import com.example.gamestoreapp.data.GameRepository
import com.example.gamestoreapp.data.model.CartItem
import com.example.gamestoreapp.data.ProductRequest
import com.example.gamestoreapp.data.ProductResponse
import com.example.gamestoreapp.data.OrderRequest
import com.example.gamestoreapp.data.OrderProduct
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CartViewModel(
    private val vibrationManager: VibrationManager,
    private val uiMessageManager: UiMessageManager,
    private val gameRepository: GameRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems = _cartItems.asStateFlow()

    // Cálculo reactivo: Subtotal, IVA (19%) y Total
    val subtotalAmount = cartItems.map { items ->
        // Usamos Double para la suma interna, pero lo devolvemos como Int
        items.sumOf { (it.product.price * it.quantity).toDouble() }.toInt()
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val ivaAmount = subtotalAmount.map { (it * 0.19).toInt() }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val totalAmount = combine(subtotalAmount, ivaAmount) { subtotal, iva ->
        subtotal + iva
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun addItemsToCart(product: ProductResponse, quantity: Int, context: Context) {
        val existingItem = _cartItems.value.find { it.product.id == product.id }
        val stockInCart = existingItem?.quantity ?: 0
        val availableStock = product.stock - stockInCart

        if (quantity > availableStock) {
            uiMessageManager.showToast(context, "No hay suficiente stock.")
            return
        }

        if (existingItem != null) {
            val updatedItems = _cartItems.value.map {
                if (it.product.id == product.id) {
                    it.copy(quantity = it.quantity + quantity)
                } else {
                    it
                }
            }
            _cartItems.value = updatedItems
        } else {
            _cartItems.value = _cartItems.value + CartItem(product, quantity)
        }

        vibrationManager.vibrate(context)
        uiMessageManager.showToast(context, "$quantity '${product.name}' añadido(s) al carrito.")
    }

    fun increaseQuantity(item: CartItem) {
        val updatedItems = _cartItems.value.map {
            if (it.product.id == item.product.id && it.quantity < item.product.stock) {
                it.copy(quantity = it.quantity + 1)
            } else {
                it
            }
        }
        _cartItems.value = updatedItems
    }

    fun decreaseQuantity(item: CartItem) {
        val updatedItems = _cartItems.value.mapNotNull {
            if (it.product.id == item.product.id) {
                if (it.quantity > 1) {
                    it.copy(quantity = it.quantity - 1)
                } else {
                    null // Elimina el item si la cantidad llega a 0
                }
            } else {
                it
            }
        }
        _cartItems.value = updatedItems
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun placeOrder(authToken: String, userEmail: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (_cartItems.value.isEmpty()) {
            onError("El carrito está vacío.")
            return
        }

        // Mapeamos los ítems del carrito a OrderProduct para el request
        val productsBoughtList = _cartItems.value.map {
            OrderProduct(
                productId = it.product.id,
                quantity = it.quantity,
                // Usamos el precio ENTERO del catálogo para el request
                price = it.product.price
            )
        }

        // Estructura de la solicitud POST
        val orderRequest = OrderRequest(
            total = totalAmount.value,
            status = "pendiente",
            products = productsBoughtList,
            user_email = userEmail
        )

        viewModelScope.launch {
            try {
                // 1. Registrar la orden en Xano
                cartRepository.postOrder(authToken, orderRequest)

                // 2. Actualizar el Stock en Xano (lógica de negocio)
                _cartItems.value.forEach { cartItem ->
                    val product = cartItem.product
                    val newStock = product.stock - cartItem.quantity
                    val productRequest = ProductRequest(
                        name = product.name,
                        description = product.description,
                        price = product.price,
                        stock = newStock,
                        active = product.active
                    )
                    // Este es el endpoint PATCH product/{id} que actualiza el stock
                    gameRepository.updateProduct(authToken, product.id, productRequest)
                }

                clearCart()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                // El error más común aquí es el HTTP 404/405 al intentar actualizar el stock,
                // por una discrepancia en el endpoint PATCH product/{id}.
                onError("Error de red o del servidor: ${e.message}")
            }
        }
    }
}

class CartViewModelFactory(
    private val vibrationManager: VibrationManager,
    private val uiMessageManager: UiMessageManager,
    private val gameRepository: GameRepository,
    private val cartRepository: CartRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CartViewModel(vibrationManager, uiMessageManager, gameRepository, cartRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}