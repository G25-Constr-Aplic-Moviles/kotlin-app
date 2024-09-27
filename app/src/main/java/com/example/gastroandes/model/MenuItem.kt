package com.example.gastroandes.model

data class MenuItem(
    val item_id: Int,
    val restaurant_id: Int,
    val name: String,
    val description: String,
    val price: Float,
    val image_url: String
)