package com.example.gastroandes

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gastroandes.model.Restaurante
import com.example.gastroandes.network.RetrofitInstance
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log

class RestaurantDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.restaurante_v4)
        createFragment()

        val restaurantId = 1 //intent.getIntExtra("RESTAURANTE_ID", 1)

        fetchRestaurantDetails(restaurantId)
    }

    private fun createFragment() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }

    private fun fetchRestaurantDetails(restaurantId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("API_CALL", "Fetching details for restaurant ID: $restaurantId")

                // Realizamos la llamada al backend
                val restaurant = RetrofitInstance.api.getRestauranteDetail(restaurantId)

                Log.d("API_CALL", "Received restaurant data: $restaurant")

                // Actualizamos la UI en el hilo principal
                runOnUiThread {
                    updateUIWithRestaurant(restaurant)
                }
            } catch (e: Exception) {
                Log.e("API_CALL_ERROR", "Error fetching restaurant details: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun updateUIWithRestaurant(restaurant: Restaurante) {
        val restaurantTitle = findViewById<TextView>(R.id.restaurant_title)
        restaurantTitle.text = restaurant.name

        val restaurantImage = findViewById<ImageView>(R.id.restaurant_image)
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = loadImageFromUrl(restaurant.image_url)
            withContext(Dispatchers.Main) {
                restaurantImage.setImageBitmap(bitmap)
            }
        }

        updateMapWithRestaurant(restaurant)
    }

    private fun updateMapWithRestaurant(restaurant: Restaurante) {
        val coordinate = LatLng(restaurant.location.latitude, restaurant.location.longitude)
        val marker = MarkerOptions().position(coordinate).title(restaurant.name)
        map.addMarker(marker)

        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(coordinate, 17f),
            3000,
            null
        )
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
