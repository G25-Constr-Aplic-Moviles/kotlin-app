package com.example.gastroandes.cache

import android.util.LruCache
import com.example.gastroandes.model.Restaurante

object RestaurantCache {
    private const val CACHE_SIZE = 1
    private val restaurantCache: LruCache<String, List<Restaurante>> = LruCache(CACHE_SIZE)

    // Método para obtener restaurantes del caché
    fun getRestaurants(): List<Restaurante>? {
        return restaurantCache.get("restaurants")
    }

    // Método para guardar restaurantes en el caché
    fun putRestaurants(restaurants: List<Restaurante>) {
        restaurantCache.put("restaurants", restaurants)
    }

    // Método opcional para limpiar el caché si es necesario
    fun clearCache() {
        restaurantCache.evictAll()
    }
}