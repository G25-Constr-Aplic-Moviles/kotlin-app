package com.example.gastroandes.network

import com.example.gastroandes.model.AuthCredentials
import com.example.gastroandes.model.AuthResponse
import com.example.gastroandes.model.HistoryEntry
import com.example.gastroandes.model.MenuItem
import com.example.gastroandes.model.Restaurante
import com.example.gastroandes.model.TokenUser
import com.example.gastroandes.model.User
import com.example.gastroandes.model.UserHistoryEntry
import com.google.android.gms.common.api.Response
import com.example.gastroandes.model.Review
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

data class TimeData(
    val tiempo: Double,
    val plataforma: String,
    val timestamp: Long = System.currentTimeMillis(), // Genera el timestamp
    val userID: String
)

interface ApiService {
    @GET("/restaurant/list")
    suspend fun getRestaurantes(): List<Restaurante>

    @GET("/restaurant/{restaurant_id}")
    suspend fun getRestauranteDetail(@Path("restaurant_id") id: Int): Restaurante

    @GET("/menu_item/{restaurant_id}")
    suspend fun getMenuItemByRestaurant(@Path("restaurant_id") id: Int): List<MenuItem>

    // Método para obtener reseñas por ID de restaurante
    @GET("/{restaurant_id}") // Cambia la URL según tu endpoint
    suspend fun getReviewsByRestaurant(@Path("restaurant_id") id: Int): List<Review>

    // Métodos para el servicio de usuarios
    @POST("/users")
    suspend fun createUser(@Body user: User)

    @POST("/users/reset")
    suspend fun resetDatabase(@Header("Authorization") token: String)

    @GET("/users/me")
    suspend fun getUserInfo(@Header("Authorization") token: String): TokenUser

    @POST("/users/auth")
    suspend fun authenticateUser(@Body credentials: AuthCredentials): AuthResponse

    // Método para el servicio de analíticas
    @POST("/add_time")
    fun sendTime(@Body timeData: TimeData): Call<Void>

    // Métodos para el servicio de historial
    @POST("/history/add")
    suspend fun addEntry(@Body historyEntry: HistoryEntry)

    @GET("/history/{user_id}")
    suspend fun getUserHistory(@Path("user_id") id: String): List<UserHistoryEntry>

    // Método para agregar una reseña
    @POST("/add_review")  // Cambia la URL a la que corresponde tu servicio
    suspend fun addReview(@Body review: Review)
}