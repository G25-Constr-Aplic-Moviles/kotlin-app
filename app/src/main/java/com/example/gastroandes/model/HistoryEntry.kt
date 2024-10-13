package com.example.gastroandes.model

data class HistoryEntry(
    val user_id: String,     // ID del usuario
    val restaurant_id: String, // ID del restaurante
    val timestamp: Long      // Marca de tiempo (puede ser un valor Long o Timestamp según tu implementación)
)
