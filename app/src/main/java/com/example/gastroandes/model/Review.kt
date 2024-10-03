package com.example.gastroandes.model

data class Review(
    val id: Int,
    val title: String,
    val content: String,
    val rating: Float,
    val username: String,
    val date: String
)