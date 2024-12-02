package com.example.gastroandes

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gastroandes.model.SessionManager
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.example.gastroandes.network.RetrofitInstance
import com.example.gastroandes.model.Review
import androidx.collection.ArrayMap

class CreateReviewActivity : AppCompatActivity() {

    private lateinit var reviewInput: EditText
    private lateinit var addReviewButton: Button
    private lateinit var starRating: LinearLayout
    private var rating: Float = 0f
    private var restaurantId: Int = -1

    companion object {
        // Replace HashMap with ArrayMap for better memory efficiency
        private var cachedReview: ArrayMap<String, Any> = ArrayMap()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.resenas_v11)

        // Same initialization code as before
        restaurantId = intent.getIntExtra("RESTAURANT_ID", -1)
        if (restaurantId == -1) {
            Toast.makeText(this, "Error al obtener el ID del restaurante", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val btnBack = findViewById<ImageView>(R.id.back_button)
        btnBack.setOnClickListener { finish() }
        reviewInput = findViewById(R.id.review_input)
        addReviewButton = findViewById(R.id.add_review_button)
        starRating = findViewById(R.id.star_rating)
        setupStarRating()
        addReviewButton.setOnClickListener { addReview() }

        loadCachedReview()
    }

    private fun setupStarRating() {
        for (i in 0 until starRating.childCount) {
            val star = starRating.getChildAt(i) as ImageView
            star.setOnClickListener {
                rating = (i + 1).toFloat()
                updateStarIcons(i)
            }
        }
    }

    private fun updateStarIcons(selectedIndex: Int) {
        for (i in 0 until starRating.childCount) {
            val star = starRating.getChildAt(i) as ImageView
            star.setImageResource(if (i <= selectedIndex) R.drawable.ic_star_filled else R.drawable.ic_star_outline)
        }
    }

    private fun addReview() {
        val content = reviewInput.text.toString()

        if (!isNetworkAvailable()) {
            // Guardar la reseña en caché temporalmente
            cacheReview(content, rating)
            Toast.makeText(this, "No hay conexión. Reseña guardada en caché.", Toast.LENGTH_LONG).show()
            return
        }

        if (content.isBlank()) {
            Toast.makeText(this, "Por favor ingresa el texto de la reseña.", Toast.LENGTH_SHORT).show()
            return
        }
        if (rating <= 0) {
            Toast.makeText(this, "Por favor selecciona una calificación.", Toast.LENGTH_SHORT).show()
            return
        }

        val token = SessionManager.getAuthToken()

        lifecycleScope.launch {
            try {
                val userInfo = RetrofitInstance.usersApi.getUserInfo("Bearer $token")
                val userId = userInfo.id
                val timestamp = System.currentTimeMillis()
                val review = Review(
                    content = content,
                    rating = rating,
                    user_id = userId,
                    restaurant_id = restaurantId,
                    timestamp = timestamp.toString()
                )

                RetrofitInstance.reviewApi.addReview(review)
                clearCachedReview() // Borra la caché si la reseña se envía correctamente
                Toast.makeText(this@CreateReviewActivity, "Reseña agregada con éxito", Toast.LENGTH_SHORT).show()
                finish()

            } catch (e: HttpException) {
                Toast.makeText(this@CreateReviewActivity, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@CreateReviewActivity, "Ocurrió un error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }
    }

    private fun cacheReview(content: String, rating: Float) {
        cachedReview["content"] = content
        cachedReview["rating"] = rating
    }

    private fun loadCachedReview() {
        // Loading from ArrayMap
        val cachedContent = cachedReview["content"] as? String
        val cachedRating = cachedReview["rating"] as? Float

        if (cachedContent != null && cachedRating != null) {
            reviewInput.setText(cachedContent)
            rating = cachedRating
            updateStarIcons((cachedRating - 1).toInt())
            Toast.makeText(this, "Reseña cargada desde la caché", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearCachedReview() {
        cachedReview.clear() // Limpia el HashMap cuando se envía exitosamente la reseña
    }
}
