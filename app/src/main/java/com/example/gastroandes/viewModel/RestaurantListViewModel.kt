package com.example.gastroandes.viewModel

import android.util.Log
import android.util.LruCache
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastroandes.cache.RestaurantCache
import com.example.gastroandes.model.Restaurante
import com.example.gastroandes.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RestaurantListViewModel : ViewModel() {

    private val _restaurants = MutableLiveData<List<Restaurante>?>()
    val restaurants: LiveData<List<Restaurante>?> get() = _restaurants

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun fetchRestaurantList(isNetworkAvailable: Boolean) {
        val cachedRestaurants = RestaurantCache.getRestaurants()

        if (cachedRestaurants != null) {
            Log.d("RestaurantListViewModel", "Cargando restaurantes desde caché.")
            _restaurants.value = cachedRestaurants
        } else if (isNetworkAvailable) {
            Log.d("RestaurantListViewModel", "Conexión disponible. Cargando restaurantes desde la API.")
            viewModelScope.launch {
                try {
                    val restaurantList = RetrofitInstance.api.getRestaurantes()
                    _restaurants.postValue(restaurantList)
                    RestaurantCache.putRestaurants(restaurantList) // Guardar en caché
                    Log.d("RestaurantListViewModel", "Restaurantes guardados en caché.")
                } catch (e: Exception) {
                    _errorMessage.postValue("Error al obtener restaurantes: ${e.message}")
                }
            }
        } else {
            Log.d("RestaurantListViewModel", "No hay conexión a Internet y no hay datos en caché.")
            _errorMessage.postValue("No hay conexión a Internet y no hay datos en caché.")
        }
    }
}
