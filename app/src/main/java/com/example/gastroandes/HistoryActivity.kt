package com.example.gastroandes

import HistoryAdapter
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private val restaurantNames = mutableSetOf<String>()
    private lateinit var completeHistoryList: List<UserHistoryEntry>
    private var selectedRestaurant: String? = null
    private var selectedTimeRange: Long? = null
    private val restaurantCache = android.util.ArrayMap<Int, String>()



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
                R.id.logOut -> {
                    logoutUser()
                    true
                }
                else -> false
            }
        }
        fetchUserHistory()
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchUserHistory() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No hay conexión a Internet. Intenta más tarde.", Toast.LENGTH_LONG).show()
            return
        }

        val token = SessionManager.getAuthToken()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userInfo = RetrofitInstance.usersApi.getUserInfo("Bearer $token")
                val userId = userInfo.id
                val historyList = RetrofitInstance.historyApi.getUserHistory(userId)

                // Usa async para hacer peticiones paralelas
                val deferredDetails = historyList.map { entry ->
                    async {
                        val restaurantId = entry.restaurant_id.toInt()
                        if (restaurantCache.containsKey(restaurantId)) {
                            entry.restaurantName = restaurantCache[restaurantId]
                        } else {
                            try {
                                val restaurantDetail = RetrofitInstance.api.getRestauranteDetail(restaurantId)
                                entry.restaurantName = restaurantDetail.name
                                // Guardar en el cache
                                restaurantCache[restaurantId] = restaurantDetail.name
                                restaurantNames.add(restaurantDetail.name)
                            } catch (e: Exception) {
                                Log.e("HistoryActivity", "Error al cargar detalles del restaurante", e)
                            }
                        }
                    }
                }

                // Espera a que todas las tareas asíncronas terminen
                deferredDetails.awaitAll()

                // Ordenar la lista por timestamp
                val sortedHistoryList = historyList.sortedByDescending { entry ->
                    convertFormattedTimestampToMillis(entry.timestamp)
                }

                withContext(Dispatchers.Main) {
                    completeHistoryList = sortedHistoryList
                    adapter = HistoryAdapter(completeHistoryList)
                    recyclerView.adapter = adapter
                    setupFilterButton()
                    setupTimeFilterButton()
                }
            } catch (e: Exception) {
                Log.e("HistoryActivity", "Error al cargar el historial del usuario", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HistoryActivity, "Error al cargar el historial. Revisa tu conexión", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    // Función para convertir el timestamp con varios formatos a milisegundos
    private fun convertFormattedTimestampToMillis(timestamp: String): Long {
        // Primer formato: "EEE, dd MMM yyyy HH:mm:ss 'GMT'"
        val primaryFormatter = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH)
        primaryFormatter.timeZone = TimeZone.getTimeZone("GMT")

        // Segundo formato: "EEE, dd MMM yyyy HH:mm:ss zzz"
        val alternateFormatter = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH)
        alternateFormatter.timeZone = TimeZone.getDefault()

        return try {
            primaryFormatter.parse(timestamp)?.time ?: 0L
        } catch (e: ParseException) {
            try {
                alternateFormatter.parse(timestamp)?.time ?: 0L
            } catch (e2: ParseException) {
                Log.e("HistoryActivity", "Error al parsear la fecha: $timestamp", e2)
                0L // Devuelve 0 si falla la conversión
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupFilterButton() {
        val filterButton: ImageView = findViewById(R.id.filterButton)
        filterButton.setOnClickListener {
            if (!isNetworkAvailable()) {
                Toast.makeText(this, "No hay conexión a Internet. Intenta más tarde.", Toast.LENGTH_LONG).show()
            } else{
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
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupTimeFilterButton() {
        val timeFilterButton: ImageView = findViewById(R.id.timeFilter)
        timeFilterButton.setOnClickListener {
            if (!isNetworkAvailable()) {
                Toast.makeText(this, "No hay conexión a Internet. Intenta más tarde.", Toast.LENGTH_LONG).show()
            }
            else{
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
            val currentDateInMillis = System.currentTimeMillis()
            val daysInMillis = days * 24 * 60 * 60 * 1000 // Convierte días a milisegundos

            filteredList = filteredList.filter { entry ->
                val entryMillis = convertFormattedTimestampToMillis(entry.timestamp)
                entryMillis != 0L && (currentDateInMillis - entryMillis) <= daysInMillis
            }
        }
        // Actualizar el adaptador con la lista filtrada
        adapter.updateData(filteredList)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }
    }


}
