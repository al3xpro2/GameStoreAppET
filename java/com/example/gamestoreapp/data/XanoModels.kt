package com.example.gamestoreapp.data

import com.google.gson.annotations.SerializedName
import java.util.Date

// --- MODELOS DE PRODUCTO ---
data class ProductResponse(
    val id: Int,
    val name: String,
    val description: String,
    val price: Int,
    val stock: Int,
    val active: Boolean,
    val images: List<ImageXano>?
)

data class ImageXano(
    val url: String
)

data class ProductRequest(
    val name: String,
    val description: String,
    val price: Int,
    val stock: Int,
    val active: Boolean,
    val image_base64: String? = null
)

// --- MODELOS DE AUTENTICACIÓN ---
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val email: String,
    val authToken: String,
    val role: String
)

// --- MODELOS DE CARRITO ---
data class CartGetResponse(
    val id: Int,
    val items: List<CartItemDetails>
)

data class CartItemDetails(
    val product: ProductResponse,
    val qty: Int
)

data class CartPostRequest(
    val items: List<CartItemPost>
)

data class CartItemPost(
    @SerializedName("product_id") val productId: Int,
    val qty: Int
)

// --- MODELOS DE ÓRDENES ---
data class OrderRequest(
    val total: Int,
    val status: String,
    @SerializedName("products_bought") val products: List<OrderProduct>,
    @SerializedName("user_email") val user_email: String
)

data class OrderProduct(
    @SerializedName("product_id") val productId: Int,
    val quantity: Int,
    val price: Int
)

data class OrderResponse(
    val id: Int,
    @SerializedName("created_at") val createdAt: Long,
    val total: Double, // ✅ CORRECCIÓN: Total debe ser Double
    val status: String,
    @SerializedName("products_bought") val productsBought: List<ProductBought>,
    @SerializedName("user_email") val userEmail: String
)

data class ProductBought(
    @SerializedName("product_id") val productId: Int,
    val quantity: Int,
    val price: Double, // ✅ CORRECCIÓN: Precio del item debe ser Double
    val name: String
)

data class UpdateOrderStatusRequest(
    val status: String
)