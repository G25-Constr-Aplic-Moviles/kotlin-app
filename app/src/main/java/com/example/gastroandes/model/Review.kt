package com.example.gastroandes.model

data class Review(
    val content: String,
    val rating: Float,
    val restaurant_id: Int,
    val timestamp: Long,
    val user_id: String,
)