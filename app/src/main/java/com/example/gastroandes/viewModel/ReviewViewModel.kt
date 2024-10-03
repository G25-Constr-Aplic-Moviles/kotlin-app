package com.example.gastroandes.viewModel// com.example.gastroandes.viewModel.ReviewViewModel.kt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.example.gastroandes.model.Review

class ReviewViewModel : ViewModel() {
    private val _reviews = MutableLiveData<List<Review>>()
    val reviews: LiveData<List<Review>> = _reviews

    init {
        loadReviews()
    }

    private fun loadReviews() {
        val mockReviews = listOf(
            Review(1, "Increíble", "Probé el arroz de lomo y me gustó demasiado, la atención es muy buena. fue rapido y no es muy caro. Lo recomiendo particularmente si tienen prisa, fui a las 2 de la tarde y no habia mucha gente en el lugar. Ponen música agradable y realmente se disfruta mucho el almuerzo", 5f, "Mario Laserna 777", "hace 3d"),
            Review(2, "normal", "No me gusto demasiado el lugar, la comida era muy normal y se alcanzaron a demorar bastante en entregarme mi pedido. no lo recomiendo si tienen afan", 3f, "The goat Seneca", "hace 2h"),
            Review(3, "yo repetiría", "Me gustó demasiado el lugar, muy buen ambiente y buena comida. el tiempo de espera fue normal ni muy rapido ni muy demorado", 5f, "Ozuna23", "hace 5d")
        )
        _reviews.value = mockReviews
    }
}