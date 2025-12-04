package com.example.gamestoreapp.data

import android.util.Log

class CartRepository {
    private val api = RetrofitClient.api

    suspend fun getCart(authToken: String): CartGetResponse? {
        return try {
            val response = api.getCart("Bearer $authToken")
            Log.d("API_XANO", "Carrito recibido exitosamente.")
            response
        } catch (e: Exception) {
            Log.e("API_XANO", "Error al obtener el carrito: ${e.message}")
            null
        }
    }

    suspend fun updateCart(authToken: String, cart: CartPostRequest) {
        try {
            api.updateCart("Bearer $authToken", cart)
            Log.d("API_XANO", "Carrito actualizado exitosamente.")
        } catch (e: Exception) {
            Log.e("API_XANO", "Error al actualizar el carrito: ${e.message}")
        }
    }

    suspend fun postOrder(authToken: String, order: OrderRequest) {
        try {
            api.postOrder("Bearer $authToken", order)
            Log.d("API_XANO", "Orden registrada exitosamente.")
        } catch (e: Exception) {
            Log.e("API_XANO", "Error al registrar la orden: ${e.message}")
            throw e
        }
    }
}