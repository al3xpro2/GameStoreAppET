package com.example.gamestoreapp.data

import android.util.Log

class GameRepository {
    // Instancia del cliente Retrofit para hacer las llamadas a la API
    private val api = RetrofitClient.api

    // --- LECTURA (READ) ---
    suspend fun getGames(): List<ProductResponse> {
        return try {
            val response = api.getProducts()
            Log.d("API_XANO", "Juegos recibidos: ${response.size}")
            response
        } catch (e: Exception) {
            Log.e("API_XANO", "Error obteniendo juegos: ${e.message}")
            emptyList()
        }
    }

    // --- CREACIÓN (CREATE) ---
    suspend fun createProduct(authToken: String, product: ProductRequest): ProductResponse? {
        return try {
            val response = api.createProduct("Bearer $authToken", product)
            Log.d("API_XANO", "Producto creado exitosamente: ${response.name}")
            response
        } catch (e: Exception) {
            Log.e("API_XANO", "Error al crear el producto: ${e.message}")
            null
        }
    }

    // --- ACTUALIZACIÓN (UPDATE) ---
    suspend fun updateProduct(authToken: String, productId: Int, product: ProductRequest): ProductResponse? {
        return try {
            val response = api.updateProduct("Bearer $authToken", productId, product)
            Log.d("API_XANO", "Producto actualizado exitosamente: ${response.name}")
            response
        } catch (e: Exception) {
            Log.e("API_XANO", "Error al actualizar el producto: ${e.message}")
            null
        }
    }

    // --- ELIMINACIÓN (DELETE) ---
    suspend fun deleteProduct(authToken: String, productId: Int): Boolean {
        return try {
            api.deleteProduct("Bearer $authToken", productId)
            Log.d("API_XANO", "Producto con ID $productId eliminado exitosamente.")
            true
        } catch (e: Exception) {
            Log.e("API_XANO", "Error al eliminar el producto: ${e.message}")
            false
        }
    }
}