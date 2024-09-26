package com.example.gastroandes.network

import com.example.gastroandes.model.Restaurante
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("/restaurant/list")
    suspend fun getRestaurantes(): List<Restaurante>

    // Método para obtener los detalles de un restaurante específico por su ID
    @GET("/restaurant/{restaurant_id}")
    suspend fun getRestauranteDetail(@Path("restaurant_id") id: Int): Restaurante
}