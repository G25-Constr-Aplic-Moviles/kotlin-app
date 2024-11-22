package com.example.gastroandes.model

data class Restaurante(
    val restaurant_id: Int,
    val name: String,
    val address: String,
    val price: Int,
    val location: Location,
    val cuisine_type: String,
    val menu_ids: List<Int>,
    val average_rating: Double,
    val total_reviews: Int,
    val image_url: String,
    var local_image_path: String? = null // Nueva propiedad para la imagen en caché
)

data class Location(
    val latitude: Double,
    val longitude: Double
)