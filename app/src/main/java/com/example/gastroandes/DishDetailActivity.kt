package com.example.gastroandes

import android.content.Context
import android.util.Log
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.example.gastroandes.model.MenuItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import com.bumptech.glide.Glide
import com.example.gastroandes.model.SessionManager
import com.example.gastroandes.network.RetrofitInstance
import com.example.gastroandes.network.TimeData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DishDetailActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DishDetailActivity"
        private const val SHARED_PREFS_NAME = "GastroAndesPrefs"
        private const val SELECTED_DISH_KEY = "selected_dish"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plato_v5)

        Log.d(TAG, "DishDetailActivity created")

        // Intenta recuperar los datos del plato desde SharedPreferences
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val dishJson = sharedPreferences.getString(SELECTED_DISH_KEY, null)

        val dish: MenuItem? = if (dishJson != null) {
            gson.fromJson(dishJson, MenuItem::class.java)
        } else {
            // Si no hay datos en SharedPreferences, usa los datos del Intent
            Log.w(TAG, "No dish data in SharedPreferences, falling back to Intent data")
            getDishFromIntent()
        }

        if (dish != null) {
            updateUIWithDish(dish)
        } else {
            Log.e(TAG, "Dish data is null, cannot update UI")
        }

        // Configurar el botón de regreso
        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish() // Cierra la actividad y regresa a la anterior
        }
    }

    private fun getDishFromIntent(): MenuItem? {
        val dishName = intent.getStringExtra("DISH_NAME")
        val dishDescription = intent.getStringExtra("DISH_DESCRIPTION")
        val dishPrice = intent.getFloatExtra("DISH_PRICE", 0.0f)
        val dishImageUrl = intent.getStringExtra("DISH_IMAGE_URL")

        return if (dishName != null && dishDescription != null && dishImageUrl != null) {
            MenuItem(
                item_id = 0, // Si el ID no es importante en este caso
                restaurant_id = 0, // Puedes asignar un valor predeterminado si no es necesario
                name = dishName,
                description = dishDescription,
                price = dishPrice,
                image_url = dishImageUrl
            )
        } else {
            null
        }
    }

    private fun updateUIWithDish(dish: MenuItem) {
        Log.d(TAG, "Updating UI with dish data: $dish")

        findViewById<TextView>(R.id.dish_name).text = dish.name
        findViewById<TextView>(R.id.dish_description).text = dish.description

        // Formatear el precio sin decimales
        val formattedPrice = String.format("%,.0f", dish.price)
        findViewById<TextView>(R.id.dish_price).text = "$$formattedPrice"

        // Cargar la imagen desde la URL
        if (dish.image_url != null) {
            loadImageFromUrl(dish.image_url)
        } else {
            Log.e(TAG, "Dish image URL is null")
        }
    }

    private fun loadImageFromUrl(url: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input: InputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(input)
                withContext(Dispatchers.Main) {
                    findViewById<ImageView>(R.id.dish_image).setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading image from URL: $url", e)
            }
        }
    }

    private fun sendLoadingTimeToAnalytics() {
        val startTime = intent.getLongExtra("startTime", 0)
        val endTime = System.currentTimeMillis()
        val loadingTime = (endTime - startTime) / 1000.0
        Log.d("LoadingTime", "Tiempo de carga: $loadingTime segundos")

        lifecycleScope.launch {
            try {
                val token = SessionManager.getAuthToken()
                val userInfo = RetrofitInstance.usersApi.getUserInfo("Bearer $token")
                val userId = userInfo.id
                val timeData = TimeData(tiempo = loadingTime, plataforma = "Kotlin", userID = userId)

                RetrofitInstance.analyticsApi.sendTimeDish(timeData).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Log.d("Analytics", "Tiempo de carga enviado correctamente")
                        } else {
                            Log.e("Analytics", "Error en el servidor: ${response.errorBody()}")
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("Analytics", "Fallo al enviar el tiempo de carga", t)
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
