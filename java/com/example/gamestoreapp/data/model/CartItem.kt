package com.example.gamestoreapp.data.model

import com.example.gamestoreapp.data.ProductResponse

// Modelo de datos para un ítem en el carrito.
// Se usa 'data class' para obtener automáticamente el método .copy(),
// necesario para actualizar la cantidad de forma inmutable en el ViewModel.
data class CartItem(
    val product: ProductResponse,
    var quantity: Int
)