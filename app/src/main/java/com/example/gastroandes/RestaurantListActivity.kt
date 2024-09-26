package com.example.gastroandes

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gastroandes.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.gastroandes.model.Restaurante
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class RestaurantListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.restaurantes_v3) // Asegúrate de que este sea el nombre correcto del layout XML

        // Llamar a la API para obtener la lista de restaurantes
        fetchRestaurantList()
    }

    private fun fetchRestaurantList() {
        // Usamos una corutina para realizar la solicitud a la API en segundo plano
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Llamada a la API para obtener la lista de restaurantes
                val response = RetrofitInstance.api.getRestaurantes()

                // Volver al hilo principal para actualizar la UI
                withContext(Dispatchers.Main) {
                    if (response.isNotEmpty()) {
                        // Llenar la UI con los datos del primer restaurante (puedes añadir más después)
                        updateUIWithRestaurant(response[0], R.id.restaurantImage1, R.id.restaurantName1, R.id.restaurantPrices1, R.id.restaurantRating1, R.id.restaurantReviews1)
                        updateUIWithRestaurant(response[1], R.id.restaurantImage2, R.id.restaurantName2, R.id.restaurantPrices2, R.id.restaurantRating2, R.id.restaurantReviews2)
                        updateUIWithRestaurant(response[2], R.id.restaurantImage3, R.id.restaurantName3, R.id.restaurantPrices3, R.id.restaurantRating3, R.id.restaurantReviews3)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateUIWithRestaurant(restaurant: Restaurante, imageViewId: Int, nameViewId: Int, priceViewId: Int, ratingViewId: Int, reviewsViewId: Int) {
        // Actualizar el nombre del restaurante
        val restaurantName = findViewById<TextView>(nameViewId)
        restaurantName.text = restaurant.name

        // Actualizar el precio del restaurante
        val restaurantPrice = findViewById<TextView>(priceViewId)
        restaurantPrice.text = when (restaurant.price) {
            1 -> "$"
            2 -> "$$"
            3 -> "$$$"
            4 -> "$$$$"
            else -> "$"
        }

        // Actualizar la calificación del restaurante
        val restaurantRating = findViewById<TextView>(ratingViewId)
        restaurantRating.text = restaurant.average_rating.toString()

        // Actualizar las reseñas del restaurante
        val restaurantReviews = findViewById<TextView>(reviewsViewId)
        restaurantReviews.text = "(${restaurant.total_reviews})"

        // Descargar y mostrar la imagen del restaurante
        val restaurantImage = findViewById<ImageView>(imageViewId)
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = loadImageFromUrl(restaurant.image_url)
            withContext(Dispatchers.Main) {
                restaurantImage.setImageBitmap(bitmap)
            }
        }
    }

    private suspend fun loadImageFromUrl(url: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input: InputStream = connection.inputStream
                BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
