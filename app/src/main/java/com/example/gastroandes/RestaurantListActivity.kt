package com.example.gastroandes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gastroandes.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RestaurantListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var restaurantAdapter: RestaurantAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.restaurantes_v3)

        recyclerView = findViewById(R.id.restaurantRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchRestaurantList()
    }

    private fun fetchRestaurantList() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val restaurantList = RetrofitInstance.api.getRestaurantes()

                runOnUiThread {
                    restaurantAdapter = RestaurantAdapter(restaurantList)
                    recyclerView.adapter = restaurantAdapter
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
