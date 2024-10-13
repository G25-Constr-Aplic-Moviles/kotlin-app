package com.example.gastroandes

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gastroandes.viewModel.RestaurantListViewModel
import android.widget.ImageButton
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

class RestaurantListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var restaurantAdapter: RestaurantAdapter
    private val viewModel: RestaurantListViewModel by viewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.restaurantes_v3)

        // Encuentra el ImageButton
        val locationButton: ImageButton = findViewById(R.id.location_image_button)

        locationButton.setOnClickListener {
            // Captura el tiempo de inicio
            val startTime = System.currentTimeMillis()

            // Crea un intent para navegar a la nueva actividad
            val intent = Intent(this, NearbyRestaurantsActivity::class.java)

            // Añadir el tiempo de inicio al intent
            intent.putExtra("startTime", startTime)

            // Inicia la nueva actividad
            startActivity(intent)
        }
        recyclerView = findViewById(R.id.restaurantRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Observa los datos de restaurantes
        viewModel.restaurants.observe(this, Observer { restaurantList ->
            restaurantAdapter = RestaurantAdapter(restaurantList)
            recyclerView.adapter = restaurantAdapter
        })

        // Observa si hay errores
        viewModel.errorMessage.observe(this, Observer { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        })

        // Llama al ViewModel para cargar los restaurantes
        viewModel.fetchRestaurantList()

        // Configurar el listener para la barra de navegación
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.history -> {
                    // Navegar a RestaurantListActivity
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}
