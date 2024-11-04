package com.example.gastroandes

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


class CreateReviewActivity : AppCompatActivity() {

    private lateinit var reviewInput: EditText
    private lateinit var addReviewButton: Button
    private lateinit var starRating: LinearLayout // Para manejar las estrellas de calificación
    private var rating: Float = 0f // Calificación seleccionada

    private var restaurantId: Int = -1 // Inicializar con un valor por defecto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.resenas_v11)

        // Obtener el Intent que inició esta actividad
        val intent = intent
        // Cambia a getIntExtra para recibir el ID como un Int
        restaurantId = intent.getIntExtra("RESTAURANT_ID", -1) // -1 es el valor por defecto si no se encuentra

        // Asegúrate de verificar que restaurantId no sea -1 antes de continuar
        if (restaurantId == -1) {
            Toast.makeText(this, "Error al obtener el ID del restaurante", Toast.LENGTH_SHORT).show()
            finish() // Termina la actividad si no se recibió correctamente
            return
        }


        val btnBack = findViewById<ImageView>(R.id.back_button)
        btnBack.setOnClickListener {
            finish() // Cierra la actividad actual y regresa a la anterior
        }

        reviewInput = findViewById(R.id.review_input)
        addReviewButton = findViewById(R.id.add_review_button)
        starRating = findViewById(R.id.star_rating)

        // Configura las estrellas de calificación
        setupStarRating()

        addReviewButton.setOnClickListener {
            addReview()
        }
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

        val token = SessionManager.getAuthToken()

        if (title.isNotBlank() && content.isNotBlank() && rating > 0) {
            lifecycleScope.launch {
                try {
                    // 1. Obtener la información del usuario
                    val userInfo = RetrofitInstance.usersApi.getUserInfo("Bearer $token")
                    val userId = userInfo.id

                    // 2. Obtener el ID del restaurante y el timestamp actua
                    // 3. Crear el objeto Review
                    val timestamp = System.currentTimeMillis()

                    val review = Review(
                        content = content,
                        rating = rating,
                        user_id = userId,
                        restaurant_id = restaurantId,
                        timestamp = timestamp
                    )

                    // 4. Llamar a la API para agregar la reseña
                    RetrofitInstance.reviewApi.addReview(review)

                    Toast.makeText(this@CreateReviewActivity, "Reseña agregada con éxito", Toast.LENGTH_SHORT).show()
                    finish() // Cierra la actividad después de agregar la reseña

                } catch (e: HttpException) {
                    Toast.makeText(this@CreateReviewActivity, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this@CreateReviewActivity, "Ocurrió un error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Por favor, completa todos los campos antes de agregar la reseña.", Toast.LENGTH_SHORT).show()
        }
    }

}
