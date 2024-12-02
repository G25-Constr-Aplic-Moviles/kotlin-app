package com.example.gastroandes

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.NumberFormat
import android.net.ConnectivityManager
import androidx.lifecycle.lifecycleScope
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import com.example.gastroandes.model.HistoryEntry
import com.example.gastroandes.model.MenuItem
import com.example.gastroandes.model.Restaurante
import com.example.gastroandes.model.Review
import com.example.gastroandes.model.SessionManager
import com.example.gastroandes.network.RetrofitInstance
import com.example.gastroandes.viewModel.RestaurantDetailViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class RestaurantDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val viewModel: RestaurantDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.restaurante_v4)
        createFragment()


        // Encuentra el botón de añadir reseña
        val btnAddResenia = findViewById<ImageButton>(R.id.btn_add_resenia)
        val btnMenu = findViewById<Button>(R.id.menu_button)

        val restaurantId = intent.getIntExtra("RESTAURANTE_ID", 1)

        btnMenu.setOnClickListener {
            if (isNetworkAvailable()) {
                val restaurantName = viewModel.restaurant.value?.name ?: "Nombre del Restaurante"
                val restaurantId = intent.getIntExtra("RESTAURANTE_ID", 1)
                val restaurantImageUrl = viewModel.restaurant.value?.image_url ?: ""

                val startTime = System.currentTimeMillis()
                // Crea un Intent para iniciar RestaurantMenuActivity
                val intent = RestaurantMenuActivity.newIntent(this, restaurantName, restaurantImageUrl)
                intent.putExtra("RESTAURANT_ID", restaurantId)
                intent.putExtra("startTime", startTime)
                startActivity(intent)
            } else {
                Toast.makeText(
                    this,
                    "No hay conexión a Internet. Intenta más tarde.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }



        // Configura el listener para el botón
        btnAddResenia.setOnClickListener {
            if (isNetworkAvailable()) {
                // Crea un Intent para iniciar CreateReviewActivity
                val intent = Intent(this, CreateReviewActivity::class.java)
                // Agrega el ID del restaurante al Intent
                intent.putExtra("RESTAURANT_ID", restaurantId)
                startActivity(intent)
            } else {
                // Mostrar mensaje de error específico cuando no hay conexión a Internet
                Toast.makeText(
                    this,
                    "No hay conexión a Internet. Intenta más tarde.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // Configurar el listener para la barra de navegación
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    // Navegar a RestaurantListActivity
                    val intent = Intent(this, RestaurantListActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.history -> {
                    // Navegar a RestaurantListActivity
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

        // Obtén una referencia al TextView
        val experienceTextView = findViewById<TextView>(R.id.experience_text)

// Configura el listener para manejar el clic
        experienceTextView.setOnClickListener {
            if (isNetworkAvailable()) {
                // Crea un Intent para iniciar ReviewsActivity
                val intent = Intent(this, ReviewActivity::class.java)
                // Puedes agregar datos al Intent si lo necesitas
                intent.putExtra("RESTAURANTE_ID", restaurantId) // Agrega el ID del restaurante si es necesario
                startActivity(intent)
            } else {
                // Mostrar mensaje de error específico cuando no hay conexión a Internet
                Toast.makeText(
                    this,
                    "No hay conexión a Internet. Intenta más tarde.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    // Obtén una referencia al botón de regreso
    val btnBack = findViewById<ImageButton>(R.id.btn_back)
    btnBack.setOnClickListener {
        finish() // Cierra la actividad actual y regresa a la anterior
    }

        // Observa los datos del restaurante
        viewModel.restaurant.observe(this, Observer { restaurant ->
            updateUIWithRestaurant(restaurant)
            updateMapWithRestaurant(restaurant)
        })

        // Observa la imagen del restaurante
        viewModel.restaurantImage.observe(this, Observer { bitmap ->
            val restaurantImage = findViewById<ImageView>(R.id.restaurant_image)
            restaurantImage.setImageBitmap(bitmap)
        })

        // Observa errores
        viewModel.errorMessage.observe(this, Observer { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        })

        viewModel.fetchRestaurantDetails(restaurantId)

        viewModel.menuItems.observe(this, Observer { menuItems ->
            updateMenuItemsUI(menuItems)
        })

        viewModel.fetchMenuItems(restaurantId)


        val markAsVisitedButton = findViewById<Button>(R.id.markAsVisitedButton)
        markAsVisitedButton.setOnClickListener {
            if (isNetworkAvailable()) {
                val token = SessionManager.getAuthToken()

                lifecycleScope.launch {
                    try {
                        // 1. Obtener la información del usuario
                        val userInfo = RetrofitInstance.usersApi.getUserInfo("Bearer $token")
                        val userId = userInfo.id

                        // 2. Obtener el ID del restaurante y el timestamp actual
                        val timestamp = System.currentTimeMillis()

                        // 3. Crear el objeto HistoryEntry y hacer la petición POST
                        val historyEntry = HistoryEntry(
                            user_id = userId,
                            restaurant_id = restaurantId.toString(),
                            timestamp = timestamp
                        )

                        try {
                            RetrofitInstance.historyApi.addEntry(historyEntry)

                            Toast.makeText(this@RestaurantDetailActivity, "Marcado como visitado", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(this@RestaurantDetailActivity, "Error al marcar como visitado: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@RestaurantDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Mostrar mensaje de error específico cuando no hay conexión a Internet
                Toast.makeText(
                    this,
                    "No hay conexión a Internet. Intenta más tarde.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    }

    private fun createFragment() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }

    private fun updateUIWithRestaurant(restaurant: Restaurante) {
        val restaurantTitle = findViewById<TextView>(R.id.restaurant_title)
        restaurantTitle.text = restaurant.name

        val restaurantPrice = findViewById<TextView>(R.id.restaurant_cost)
        restaurantPrice.text = when (restaurant.price) {
            1 -> "$"
            2 -> "$$"
            3 -> "$$$"
            4 -> "$$$$"
            else -> "$"
        }
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

    private fun loadImageFromUrl(url: String, callback: (Bitmap?) -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input: InputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(input)
                withContext(Dispatchers.Main) {
                    callback(bitmap)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }

    private fun displayFirstReview(review: Review) {
        val experienceTextView = findViewById<TextView>(R.id.experience_text)
        experienceTextView.text = review.content // Suponiendo que `content` es la propiedad que contiene el texto de la reseña
    }

    private fun updateMenuItemsUI(menuItems: List<MenuItem>) {
        // Referencias a los elementos de la UI
        val dish1 = findViewById<CardView>(R.id.dish1)
        val dish2 = findViewById<CardView>(R.id.dish2)
        val dish3 = findViewById<CardView>(R.id.dish3)

        val name1 = findViewById<TextView>(R.id.name1)
        val price1 = findViewById<TextView>(R.id.price1)
        val image1 = findViewById<ImageView>(R.id.menu_item_image_1)

        val name2 = findViewById<TextView>(R.id.name2)
        val price2 = findViewById<TextView>(R.id.price2)
        val image2 = findViewById<ImageView>(R.id.menu_item_image_2)

        val name3 = findViewById<TextView>(R.id.name3)
        val price3 = findViewById<TextView>(R.id.price3)
        val image3 = findViewById<ImageView>(R.id.menu_item_image_3)

        // Manejo de ítems del menú
        if (menuItems.isNotEmpty()) {
            val item1 = menuItems[0]
            name1.text = item1.name
            price1.text = formatPrice(item1.price)
            loadImageFromUrl(item1.image_url) { bitmap ->
                image1.setImageBitmap(bitmap)
            }
            dish1.visibility = View.VISIBLE
            dish1.setOnClickListener {
                openDishDetailActivity(item1)
            }
        } else {
            dish1.visibility = View.GONE
        }

        if (menuItems.size >= 2) {
            val item2 = menuItems[1]
            name2.text = item2.name
            price2.text = formatPrice(item2.price)
            loadImageFromUrl(item2.image_url) { bitmap ->
                image2.setImageBitmap(bitmap)
            }
            dish2.visibility = View.VISIBLE
            dish2.setOnClickListener {
                openDishDetailActivity(item2)
            }
        } else {
            dish2.visibility = View.GONE
        }

        if (menuItems.size >= 3) {
            val item3 = menuItems[2]
            name3.text = item3.name
            price3.text = formatPrice(item3.price)
            loadImageFromUrl(item3.image_url) { bitmap ->
                image3.setImageBitmap(bitmap)
            }
            dish3.visibility = View.VISIBLE
            dish3.setOnClickListener {
                openDishDetailActivity(item3)
            }
        } else {
            dish3.visibility = View.GONE
        }
    }

    private fun openDishDetailActivity(dish: MenuItem) {
        // Guarda el plato seleccionado en SharedPreferences
        val sharedPreferences = getSharedPreferences("GastroAndesPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val dishJson = gson.toJson(dish)
        editor.putString("selected_dish", dishJson)
        editor.apply()

        // Abre DishDetailActivity
        val intent = Intent(this, DishDetailActivity::class.java)
        startActivity(intent)
    }


    private fun formatPrice(price: Float): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        formatter.maximumFractionDigits = 0
        return formatter.format(price)
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

    private fun addReview() {

        // Redirige a la pantalla de inicio de sesión
        val intent = Intent(this, CreateReviewActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun loadReviewData(review: Review) {
        val experienceTextView: TextView = findViewById(R.id.experience_text)

        // Asigna el contenido de la reseña al TextView
        experienceTextView.text = review.content
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

}
