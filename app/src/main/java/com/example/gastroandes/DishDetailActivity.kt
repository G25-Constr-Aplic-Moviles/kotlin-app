package com.example.gastroandes

import android.util.Log
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class DishDetailActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DishDetailActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plato_v5)

        Log.d(TAG, "DishDetailActivity created")

        // Obtener los datos del Intent
        val dishName = intent.getStringExtra("DISH_NAME")
        val dishDescription = intent.getStringExtra("DISH_DESCRIPTION")
        val dishPrice = intent.getFloatExtra("DISH_PRICE", 0.0f)
        val dishImageUrl = intent.getStringExtra("DISH_IMAGE_URL")

        Log.d(TAG, "Received data: name=$dishName, description=$dishDescription, price=$dishPrice, imageUrl=$dishImageUrl")

        // Actualizar la UI con los datos del plato
        if (dishName != null) {
            findViewById<TextView>(R.id.dish_name).text = dishName
        } else {
            Log.e(TAG, "Dish name is null")
        }

        if (dishDescription != null) {
            findViewById<TextView>(R.id.dish_description).text = dishDescription
        } else {
            Log.e(TAG, "Dish description is null")
        }

        findViewById<TextView>(R.id.dish_price).text = "$${dishPrice}"

        // Cargar la imagen desde la URL
        if (dishImageUrl != null) {
            loadImageFromUrl(dishImageUrl)
        } else {
            Log.e(TAG, "Dish image URL is null")
        }

        // Configurar el botón de regreso
        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish() // Cierra la actividad y regresa a la anterior
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
}
