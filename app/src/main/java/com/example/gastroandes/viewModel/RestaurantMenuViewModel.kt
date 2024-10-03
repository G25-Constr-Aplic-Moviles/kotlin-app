package com.example.gastroandes.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gastroandes.model.MenuItem

class RestaurantMenuViewModel : ViewModel() {
    private val _menuItems = MutableLiveData<List<MenuItem>>()
    val menuItems: LiveData<List<MenuItem>> = _menuItems

    init {
        loadMenuItems()
    }

    private fun loadMenuItems() {
        // In a real app, you might load this data from a repository or API
        val items = listOf(
            MenuItem(1, 1, "Japanese dish", "Delicioso sushi roll", 50000.0F, "https://example.com/sushi_roll.jpg" )
        )
        _menuItems.value = items
    }
}

data class RestaurantMenuItem(val name: String, val price: String, val imageResId: Int)