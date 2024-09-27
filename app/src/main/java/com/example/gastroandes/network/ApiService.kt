package com.example.gastroandes.network

import com.example.gastroandes.model.AuthCredentials
import com.example.gastroandes.model.AuthResponse
import com.example.gastroandes.model.MenuItem
import com.example.gastroandes.model.Restaurante
import com.example.gastroandes.model.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("/restaurant/list")
    suspend fun getRestaurantes(): List<Restaurante>

    @GET("/restaurant/{restaurant_id}")
    suspend fun getRestauranteDetail(@Path("restaurant_id") id: Int): Restaurante

    @GET("/menu_item/{restaurant_id}")
    suspend fun getMenuItemByRestaurant(@Path("restaurant_id") id: Int): List<MenuItem>

    // Métodos para el servicio de usuarios
    @POST("/users")
    suspend fun createUser(@Body user: User)

    @POST("/users/reset")
    suspend fun resetDatabase(@Header("Authorization") token: String)

    @GET("/users/me")
    suspend fun getUserInfo(@Header("Authorization") token: String): User

    @POST("/users/auth")
    suspend fun authenticateUser(@Body credentials: AuthCredentials): AuthResponse

}