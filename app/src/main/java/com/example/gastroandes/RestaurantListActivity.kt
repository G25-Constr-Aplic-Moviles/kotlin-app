package com.example.gastroandes

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gastroandes.viewModel.RestaurantListViewModel

class RestaurantListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var restaurantAdapter: RestaurantAdapter
    private val viewModel: RestaurantListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.restaurantes_v3)

        recyclerView = findViewById(R.id.restaurantRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Observa los datos de restaurantes
        viewModel.restaurants.observe(this, Observer { restaurantList ->
            restaurantAdapter = RestaurantAdapter(restaurantList)
            recyclerView.adapter = restaurantAdapter
        })

        // Observa si hay errores
        viewModel.errorMessage.observe(this, Observer { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        })

        // Llama al ViewModel para cargar los restaurantes
        viewModel.fetchRestaurantList()
    }
}
