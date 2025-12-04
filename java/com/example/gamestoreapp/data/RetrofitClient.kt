package com.example.gamestoreapp.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // URL Base de Xano corregida para apuntar a tu grupo de APIs específico.
    // Asegúrate de que este endpoint (api:NEoI9qGr) contenga todas las tablas necesarias.
    private const val BASE_URL = "https://x8ki-letl-twmt.n7.xano.io/api:NEoI9qGr/"

    val api: XanoApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(XanoApi::class.java)
    }
}