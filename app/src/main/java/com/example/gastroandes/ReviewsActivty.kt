package com.example.gastroandes

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gastroandes.network.RetrofitInstance
import com.example.gastroandes.network.RetrofitInstance.ReviewRetrofit
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ReviewActivty : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.restaurant_review)


        val restaurantId = intent.getIntExtra("RESTAURANTE_ID", 1)
        // Simular la carga de datos
        fetchReviews(restaurantId)
    }
    private fun fetchReviews(restaurantId: Int) {
        // Llamar a las reseñas en un hilo diferente usando corutinas
        lifecycleScope.launch {
            try {
                val reviews = RetrofitInstance.reviewApi.getReviewsByRestaurant(restaurantId)

                Log.d("ReviewsActivity", "Reseñas obtenidas: $reviews")
            } catch (e: Exception) {
                Log.e("ReviewsActivity", "Error al obtener reseñas: ${e.message}")
            }
        }
    }

}
