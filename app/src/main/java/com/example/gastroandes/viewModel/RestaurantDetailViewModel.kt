package com.example.gastroandes.viewModel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastroandes.model.MenuItem
import com.example.gastroandes.model.Restaurante
import com.example.gastroandes.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class RestaurantDetailViewModel : ViewModel() {

    private val _restaurant = MutableLiveData<Restaurante>()
    val restaurant: LiveData<Restaurante> get() = _restaurant

    private val _menuItems = MutableLiveData<List<MenuItem>>()
    val menuItems: LiveData<List<MenuItem>> get() = _menuItems

    private val _restaurantImage = MutableLiveData<Bitmap?>()
    val restaurantImage: LiveData<Bitmap?> get() = _restaurantImage

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // Función para obtener los detalles del restaurante por su ID
    fun fetchRestaurantDetails(restaurantId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val restaurant = RetrofitInstance.api.getRestauranteDetail(restaurantId)
                _restaurant.postValue(restaurant)
                loadImageFromUrl(restaurant.image_url)
            } catch (e: Exception) {
                _errorMessage.postValue("Error al obtener los detalles: ${e.message}")
            }
        }
    }

    // Cargar la imagen del restaurante desde la URL
    private fun loadImageFromUrl(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input: InputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(input)
                _restaurantImage.postValue(bitmap)
            } catch (e: Exception) {
                _restaurantImage.postValue(null)
            }
        }
    }

    fun fetchMenuItems(restaurantId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val menuItems = RetrofitInstance.api.getMenuItemByRestaurant(restaurantId)
                _menuItems.postValue(menuItems)
            } catch (e: Exception) {
                _errorMessage.postValue("Error al obtener los ítems del menú: ${e.message}")
            }
        }
    }

}
