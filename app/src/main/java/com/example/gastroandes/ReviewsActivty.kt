package com.example.gastroandes
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gastroandes.R
import com.example.gastroandes.model.Review
import com.example.gastroandes.model.SessionManager
import com.example.gastroandes.network.RetrofitInstance
import com.example.gastroandes.network.TimeData
import com.example.gastroandes.network.TimeDataReview
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ReviewActivity : AppCompatActivity() {

    private var selectedTimeRange: Long? = null
    private var selectedRating: Float? = null// Nueva variable para almacenar el filtro de calificación
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReviewAdapter
    private var restaurantId: Int = 1
    private lateinit var completeReviewList: List<Review>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.restaurant_review)

        recyclerView = findViewById(R.id.reviewsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inicializar los botones de filtro
        setupTimeFilterButton()
        setupRatingFilterButton() // Nueva llamada para el botón de filtro de calificación

        restaurantId = intent.getIntExtra("RESTAURANTE_ID", 1)
        fetchReviews(restaurantId)
        sendAccessDateToAnalytics(restaurantId)

        val btnAddResenia = findViewById<ImageButton>(R.id.addReviewButton)
        btnAddResenia.setOnClickListener {
            val intent = Intent(this, CreateReviewActivity::class.java)
            intent.putExtra("RESTAURANT_ID", restaurantId)
            startActivity(intent)
        }

        val btnBack = findViewById<ImageButton>(R.id.backButton)
        btnBack.setOnClickListener {
            finish()
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    val intent = Intent(this, RestaurantListActivity::class.java)
                    startActivity(intent)
                    true
                }
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

    override fun onResume() {
        super.onResume()
        fetchReviews(restaurantId)
    }

    private fun fetchReviews(restaurantId: Int) {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No hay conexión a Internet. Intenta más tarde.", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            try {
                val reviews = RetrofitInstance.reviewApi.getReviewsByRestaurant(restaurantId)
                Log.d("ReviewActivity", "Reseñas obtenidas: $reviews")

                // Ordena las reseñas por timestamp (más reciente a más antigua)
                completeReviewList = reviews.sortedByDescending { convertFormattedTimestampToMillis(it.timestamp) }

                // Configura el adaptador con la lista ordenada
                adapter = ReviewAdapter(completeReviewList)
                recyclerView.adapter = adapter
            } catch (e: Exception) {
                Log.e("ReviewActivity", "Error al obtener reseñas: ${e.message}")
                Toast.makeText(this@ReviewActivity, "Error al obtener reseñas", Toast.LENGTH_SHORT).show()
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

    private fun sendAccessDateToAnalytics(restaurantId: Int) {
        lifecycleScope.launch {
            try {
                val token = SessionManager.getAuthToken()
                val userInfo = RetrofitInstance.usersApi.getUserInfo("Bearer $token")
                val userId = userInfo.id
                val timestamp = System.currentTimeMillis()
                val timeDate = TimeDataReview(timestamp = timestamp, userID = userId, restaurantID = restaurantId)

                RetrofitInstance.analyticsApi.sendTime(timeDate).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Log.d("Analytics", "Datos de acceso enviado correctamente")
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e("Analytics", "Error en el servidor: $errorBody")
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

    private fun logoutUser() {
        SessionManager.clearAuthToken()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
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
    private fun setupRatingFilterButton() {
        val ratingFilterButton: ImageView = findViewById(R.id.filterButton)
        ratingFilterButton.setOnClickListener {
            val popupMenu = PopupMenu(this, ratingFilterButton)

            // Añadir la opción para mostrar todas las calificaciones
            popupMenu.menu.add(0, -1, 0, "Mostrar todas")

            // Obtener calificaciones únicas de las reseñas
            val uniqueRatings = completeReviewList.map { it.rating }.distinct().sortedDescending()

            // Agregar cada calificación única al menú
            uniqueRatings.forEachIndexed { index, rating ->
                popupMenu.menu.add(0, index, 0, "$rating estrellas")
            }

            popupMenu.setOnMenuItemClickListener { item ->
                if (item.itemId == -1) {
                    selectedRating = null // Resetea el filtro de calificación
                } else {
                    selectedRating = uniqueRatings[item.itemId] // Fija la calificación seleccionada
                }
                applyFilters() // Aplica los filtros
                true
            }

            popupMenu.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterByRating(rating: Float) {
        selectedRating = rating
        applyFilters() // Aplica ambos filtros
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterHistoryByTime(days: Long) {
        selectedTimeRange = days
        applyFilters() // Aplica ambos filtros
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun applyFilters() {
        var filteredList = completeReviewList

        // Filtrar por tiempo si está seleccionado
        selectedTimeRange?.let { days ->
            val currentDateInMillis = System.currentTimeMillis()
            val daysInMillis = days * 24 * 60 * 60 * 1000

            filteredList = filteredList.filter { entry ->
                val entryMillis = convertFormattedTimestampToMillis(entry.timestamp)
                entryMillis != 0L && (currentDateInMillis - entryMillis) <= daysInMillis
            }
        }

        // Filtrar por calificación si está seleccionado
        selectedRating?.let { rating ->
            filteredList = filteredList.filter { entry ->
                entry.rating == rating // Filtra reseñas con calificación específica seleccionada
            }
        }

        // Actualizar el adaptador con la lista filtrada
        adapter.updateData(filteredList)
    }
}

