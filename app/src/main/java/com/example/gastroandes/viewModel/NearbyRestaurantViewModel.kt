package com.example.gastroandes.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastroandes.model.Restaurante
import com.example.gastroandes.network.ApiService
import kotlinx.coroutines.launch
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.math.*

class NearbyRestaurantsViewModel(private val apiService: ApiService) : ViewModel() {

    private val _markers = MutableLiveData<List<MarkerOptions>>()
    val markers: LiveData<List<MarkerOptions>> = _markers

    fun loadNearbyRestaurants(userLocation: LatLng) {
        viewModelScope.launch {
            try {
                val restaurantes = apiService.getRestaurantes()
                val nearbyRestaurants = restaurantes.filter { restaurant ->
                    val restaurantLocation = LatLng(restaurant.location.latitude, restaurant.location.longitude)
                    calculateDistance(userLocation, restaurantLocation) <= 30.0 // Ajustar para definir que tan lejos son los restaurantes cercanos
                }
                createMarkers(nearbyRestaurants)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createMarkers(restaurantes: List<Restaurante>) {
        val markerOptionsList = restaurantes.map { restaurante ->
            val position = LatLng(restaurante.location.latitude, restaurante.location.longitude)
            MarkerOptions().position(position).title(restaurante.name)
        }
        _markers.value = markerOptionsList
    }

    // Función para calcular la distancia entre dos coordenadas (en kilómetros)
    private fun calculateDistance(loc1: LatLng, loc2: LatLng): Double {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(loc2.latitude - loc1.latitude)
        val dLng = Math.toRadians(loc2.longitude - loc1.longitude)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(loc1.latitude)) * cos(Math.toRadians(loc2.latitude)) *
                sin(dLng / 2) * sin(dLng / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c
    }
}