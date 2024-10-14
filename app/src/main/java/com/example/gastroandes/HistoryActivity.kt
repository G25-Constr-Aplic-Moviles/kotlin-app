package com.example.gastroandes

import HistoryAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gastroandes.model.SessionManager
import com.example.gastroandes.model.UserHistoryEntry
import com.example.gastroandes.network.RetrofitInstance
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private val restaurantNames = mutableSetOf<String>()
    private lateinit var completeHistoryList: List<UserHistoryEntry>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        recyclerView = findViewById(R.id.historyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }


        // Configurar el listener para la barra de navegación
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.history
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    val intent = Intent(this, RestaurantListActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        fetchUserHistory()
    }

    private fun fetchUserHistory() {
        val token = SessionManager.getAuthToken()
        CoroutineScope(Dispatchers.IO).launch {
            val userInfo = RetrofitInstance.usersApi.getUserInfo("Bearer $token")
            val userId = userInfo.id
            try {
                val historyList = RetrofitInstance.historyApi.getUserHistory(userId)
                // Obtener el nombre del restaurante para cada entrada
                for (entry in historyList) {
                    try {
                        val restaurantDetail = RetrofitInstance.api.getRestauranteDetail(entry.restaurant_id.toInt())
                        entry.restaurantName = restaurantDetail.name
                        restaurantNames.add(restaurantDetail.name)
                    } catch (e: Exception) {
                        Log.e("HistoryActivity", "Error al obtener detalles del restaurante", e)
                    }
                }
                withContext(Dispatchers.Main) {
                    completeHistoryList = historyList
                    adapter = HistoryAdapter(completeHistoryList)
                    recyclerView.adapter = adapter
                    setupFilterButton()
                }
            } catch (e: Exception) {
                Log.e("HistoryActivityDeMierda", "Error al cargar el historial del usuario", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HistoryActivity, "Error al cargar el historial", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupFilterButton() {
        val filterButton: ImageView = findViewById(R.id.filterButton)
        filterButton.setOnClickListener {
            val restaurantList = restaurantNames.toList()
            val popupMenu = PopupMenu(this, filterButton)

            popupMenu.menu.add(0, -1, 0, "Mostrar todos")

            restaurantList.forEachIndexed { index, restaurant ->
                popupMenu.menu.add(0, index, 0, restaurant)
            }

            popupMenu.setOnMenuItemClickListener { item ->
                if (item.itemId == -1) {
                    adapter.updateData(completeHistoryList)
                } else {
                    val selectedRestaurant = restaurantList[item.itemId]
                    filterHistoryByRestaurant(selectedRestaurant)
                }
                true
            }

            popupMenu.show()
        }
    }


    private fun filterHistoryByRestaurant(restaurantName: String) {
        val filteredList = completeHistoryList.filter { it.restaurantName == restaurantName }
        adapter.updateData(filteredList)
    }
}
