package com.example.gastroandes
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
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

class ReviewActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReviewAdapter
    private var restaurantId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.restaurant_review)

        recyclerView = findViewById(R.id.reviewsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Guarda el restaurantId como variable de clase para usarlo en onResume
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
        // Llama a fetchReviews para recargar las reseñas cada vez que la actividad vuelva a primer plano
        fetchReviews(restaurantId)
    }

    private fun fetchReviews(restaurantId: Int) {
        // Check for network availability before making the network request
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No hay conexión a Internet. Intenta más tarde.", Toast.LENGTH_LONG).show()
            return // Exit the method if there's no connection
        }

        lifecycleScope.launch {
            try {
                val reviews = RetrofitInstance.reviewApi.getReviewsByRestaurant(restaurantId)
                Log.d("ReviewActivity", "Reseñas obtenidas: $reviews")
                adapter = ReviewAdapter(reviews)
                recyclerView.adapter = adapter
            } catch (e: Exception) {
                Log.e("ReviewActivity", "Error al obtener reseñas: ${e.message}")
                Toast.makeText(this@ReviewActivity, "Error al obtener reseñas", Toast.LENGTH_SHORT).show()
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
}

