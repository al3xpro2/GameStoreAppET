package com.example.gamestoreapp.data

import android.util.Log

class OrderRepository {
    private val api = RetrofitClient.api

    suspend fun getOrders(authToken: String): List<OrderResponse> {
        return try {
            val response = api.getOrders("Bearer $authToken")
            Log.d("API_XANO", "Órdenes recibidas: ${response.size}")
            response
        } catch (e: Exception) {
            Log.e("API_XANO", "Error obteniendo las órdenes: ${e.message}")
            emptyList()
        }
    }

    // --- ¡NUEVA FUNCIÓN AÑADIDA! ---
    suspend fun updateOrderStatus(authToken: String, orderId: Int, newStatus: String): Boolean {
        return try {
            val request = UpdateOrderStatusRequest(status = newStatus)
            api.updateOrderStatus("Bearer $authToken", orderId, request)
            Log.d("API_XANO", "Estado de la orden $orderId actualizado a $newStatus.")
            true
        } catch (e: Exception) {
            Log.e("API_XANO", "Error al actualizar estado de la orden: ${e.message}")
            false
        }
    }
}