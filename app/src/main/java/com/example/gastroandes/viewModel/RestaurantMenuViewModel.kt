package com.example.gastroandes.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastroandes.model.MenuItem
import com.example.gastroandes.network.RetrofitInstance
import kotlinx.coroutines.launch

class RestaurantMenuViewModel : ViewModel() {
    private val _menuItems = MutableLiveData<List<MenuItem>>()
    val menuItems: LiveData<List<MenuItem>> = _menuItems

    fun loadMenuItems(restaurantId: Int) {
        viewModelScope.launch {
            try {
                val items = RetrofitInstance.api.getMenuItemByRestaurant(restaurantId)
                _menuItems.value = items
            } catch (e: Exception) {
                _menuItems.value = emptyList() // Manejar error y actualizar la UI si falla la carga
            }
        }
    }
}

data class RestaurantMenuItem(val name: String, val price: String, val imageResId: Int)