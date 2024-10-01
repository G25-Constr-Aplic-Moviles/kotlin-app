package com.example.gastroandes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gastroandes.network.ApiService
import com.example.gastroandes.viewModel.NearbyRestaurantsViewModel

class NearbyRestaurantsViewModelFactory(
    private val apiService: ApiService
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NearbyRestaurantsViewModel::class.java)) {
            return NearbyRestaurantsViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
