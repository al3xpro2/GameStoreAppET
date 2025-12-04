package com.example.gamestoreapp.data

import retrofit2.http.*

interface XanoApi {
    // --- PRODUCTOS ---
    @GET("product")
    suspend fun getProducts(): List<ProductResponse>

    @POST("product")
    suspend fun createProduct(
        @Header("Authorization") authToken: String,
        @Body product: ProductRequest
    ): ProductResponse

    @PATCH("product/{id}")
    suspend fun updateProduct(
        @Header("Authorization") authToken: String,
        @Path("id") productId: Int,
        @Body product: ProductRequest
    ): ProductResponse

    @DELETE("product/{id}")
    suspend fun deleteProduct(
        @Header("Authorization") authToken: String,
        @Path("id") productId: Int
    )

    // --- AUTENTICACIÓN ---
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/signup")
    suspend fun signup(@Body request: LoginRequest): LoginResponse

    // --- CARRITO ---
    @GET("carrito")
    suspend fun getCart(@Header("Authorization") authToken: String): CartGetResponse

    @POST("carrito")
    suspend fun updateCart(
        @Header("Authorization") authToken: String,
        @Body cart: CartPostRequest
    )

    // --- ÓRDENES ---
    @POST("orders")
    suspend fun postOrder(
        @Header("Authorization") authToken: String,
        @Body order: OrderRequest
    )

    @GET("orders")
    suspend fun getOrders(@Header("Authorization") authToken: String): List<OrderResponse>

    // --- ADMIN ÓRDENES ---
    @PATCH("orders/{id}")
    suspend fun updateOrderStatus(
        @Header("Authorization") authToken: String,
        @Path("id") orderId: Int,
        @Body request: UpdateOrderStatusRequest
    ): OrderResponse
}