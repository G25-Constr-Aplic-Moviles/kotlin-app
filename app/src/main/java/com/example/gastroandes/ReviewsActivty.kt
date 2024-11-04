package com.example.gastroandes
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gastroandes.R
import com.example.gastroandes.model.Review
import com.example.gastroandes.network.RetrofitInstance
import kotlinx.coroutines.launch

class ReviewActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.restaurant_review)

        recyclerView = findViewById(R.id.reviewsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val restaurantId = intent.getIntExtra("RESTAURANTE_ID", 1)
        fetchReviews(restaurantId)
    }

    private fun fetchReviews(restaurantId: Int) {
        lifecycleScope.launch {
            try {
                val reviews = RetrofitInstance.reviewApi.getReviewsByRestaurant(restaurantId)
                Log.d("ReviewActivity", "Reseñas obtenidas: $reviews")
                adapter = ReviewAdapter(reviews)
                recyclerView.adapter = adapter
            } catch (e: Exception) {
                Log.e("ReviewActivity", "Error al obtener reseñas: ${e.message}")
                Toast.makeText(this@ReviewActivity, "Error al obtener reseñas", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
