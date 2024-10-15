package com.example.gastroandes

import HistoryAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private val restaurantNames = mutableSetOf<String>()
    private lateinit var completeHistoryList: List<UserHistoryEntry>
    private var selectedRestaurant: String? = null
    private var selectedTimeRange: Long? = null



    @RequiresApi(Build.VERSION_CODES.O)
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
        setupTimeFilterButton()
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
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
                    selectedRestaurant = null // Resetea el filtro de restaurante
                } else {
                    val selectedRestaurant = restaurantList[item.itemId]
                    filterHistoryByRestaurant(selectedRestaurant)
                }
                applyFilters()
                true
            }

            popupMenu.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupTimeFilterButton() {
        val timeFilterButton: ImageView = findViewById(R.id.timeFilter)
        timeFilterButton.setOnClickListener {
            val popupMenu = PopupMenu(this, timeFilterButton)
            popupMenu.menu.add(0, 1, 0, "Última semana")
            popupMenu.menu.add(0, 2, 0, "Último mes")
            popupMenu.menu.add(0, 3, 0, "Últimos 6 meses")

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> filterHistoryByTime(7)    // Última semana
                    2 -> filterHistoryByTime(30)   // Último mes
                    3 -> filterHistoryByTime(180)  // Últimos 6 meses
                }
                true
            }
            popupMenu.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterHistoryByRestaurant(restaurantName: String) {
        selectedRestaurant = restaurantName
        applyFilters() // Aplica ambos filtros
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterHistoryByTime(days: Long) {
        selectedTimeRange = days
        applyFilters() // Aplica ambos filtros
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun applyFilters() {
        var filteredList = completeHistoryList

        // Filtrar por restaurante si está seleccionado
        selectedRestaurant?.let { restaurantName ->
            filteredList = filteredList.filter { it.restaurantName == restaurantName }
        }

        // Filtrar por tiempo si está seleccionado
        selectedTimeRange?.let { days ->
            val currentDate = LocalDateTime.now()
            filteredList = filteredList.filter {
                val formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
                val entryDate = LocalDateTime.parse(it.timestamp, formatter)
                val daysBetween = java.time.Duration.between(entryDate, currentDate).toDays()
                daysBetween <= days
            }
        }

        // Actualizar el adaptador con la lista filtrada
        adapter.updateData(filteredList)
    }

}
