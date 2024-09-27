package com.example.gastroandes.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastroandes.model.Restaurante
import com.example.gastroandes.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RestaurantListViewModel : ViewModel() {

    private val _restaurants = MutableLiveData<List<Restaurante>>()
    val restaurants: LiveData<List<Restaurante>> get() = _restaurants

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // Función para obtener la lista de restaurantes
    fun fetchRestaurantList() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val restaurantList = RetrofitInstance.api.getRestaurantes()
                _restaurants.postValue(restaurantList) // Actualizar LiveData con la lista de restaurantes
            } catch (e: Exception) {
                _errorMessage.postValue("Error al obtener restaurantes: ${e.message}")
            }
        }
    }
}
