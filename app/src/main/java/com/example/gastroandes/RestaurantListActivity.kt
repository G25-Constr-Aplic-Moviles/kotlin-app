package com.example.gastroandes

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gastroandes.viewModel.RestaurantListViewModel
import android.widget.ImageButton
import com.example.gastroandes.model.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class RestaurantListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var restaurantAdapter: RestaurantAdapter
    private val viewModel: RestaurantListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.restaurantes_v3)

        val locationButton: ImageButton = findViewById(R.id.location_image_button)
        locationButton.setOnClickListener {
            if (isNetworkAvailable()) {
                val startTime = System.currentTimeMillis()
                val intent = Intent(this, NearbyRestaurantsActivity::class.java)
                intent.putExtra("startTime", startTime)
                startActivity(intent)
            } else {
                // Mostrar mensaje de error específico cuando no hay conexión a Internet
                Toast.makeText(
                    this,
                    "Por favor, verifica tu conexión a Internet e inténtalo nuevamente más tarde",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        recyclerView = findViewById(R.id.restaurantRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.restaurants.observe(this, Observer { restaurantList ->
            restaurantAdapter = RestaurantAdapter(this, restaurantList)
            recyclerView.adapter = restaurantAdapter
        })

        viewModel.errorMessage.observe(this, Observer { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        })

        viewModel.fetchRestaurantList()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.home
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.history -> {
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.logOut -> {
                    logoutUser()
                    true
                }
                else -> false
            }
        }
    }

    private fun logoutUser() {
        // Limpia el token de SharedPreferences
        SessionManager.clearAuthToken()

        // Redirige a la pantalla de inicio de sesión
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Cierra la actividad actual
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

}
