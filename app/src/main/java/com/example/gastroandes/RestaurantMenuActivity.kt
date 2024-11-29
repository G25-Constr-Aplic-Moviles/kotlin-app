package com.example.gastroandes

import RestaurantMenuAdapter
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView

import com.example.gastroandes.viewModel.RestaurantMenuViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class RestaurantMenuActivity : AppCompatActivity() {

    private lateinit var viewModel: RestaurantMenuViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var menuAdapter: RestaurantMenuAdapter
    private var restaurantId: Int = 0 // Variable para almacenar el ID del restaurante
    private var isAscending: Boolean = true // Orden ascendente inicial

    companion object {
        private const val EXTRA_RESTAURANT_NAME = "extra_restaurant_name"
        private const val EXTRA_RESTAURANT_ID = "RESTAURANT_ID"
        private const val EXTRA_RESTAURANT_IMAGE_URL = "extra_restaurant_image_url"

        fun newIntent(context: Context, restaurantName: String, restaurantImageUrl: String): Intent {
            return Intent(context, RestaurantMenuActivity::class.java).apply {
                putExtra(EXTRA_RESTAURANT_NAME, restaurantName)
                putExtra(EXTRA_RESTAURANT_IMAGE_URL, restaurantImageUrl)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.restaurant_menu_detail)

        val restaurantName = intent.getStringExtra(EXTRA_RESTAURANT_NAME) ?: "Restaurant Name"
        val restaurantImageUrl = intent.getStringExtra(EXTRA_RESTAURANT_IMAGE_URL) ?: ""
        restaurantId = intent.getIntExtra(EXTRA_RESTAURANT_ID, -1)

        // Actualiza el nombre del restaurante en la UI
        findViewById<TextView>(R.id.restaurantNameText).text = restaurantName

        // Cargar la imagen del restaurante desde la URL
        loadImageFromUrl(restaurantImageUrl) { bitmap ->
            findViewById<ImageView>(R.id.restaurantHeaderImage).setImageBitmap(bitmap)
        }

        // Inicializar el ViewModel
        viewModel = ViewModelProvider(this).get(RestaurantMenuViewModel::class.java)

        // Configurar el RecyclerView para los ítems del menú
        recyclerView = findViewById(R.id.menuRecyclerView)
        menuAdapter = RestaurantMenuAdapter()
        recyclerView.adapter = menuAdapter

        // Observa los ítems del menú y actualiza la UI cuando se reciban
        viewModel.menuItems.observe(this) { menuItems ->
            menuAdapter.submitList(menuItems)
        }

        // Cargar los ítems del menú y ordenarlos inicialmente
        viewModel.loadMenuItems(restaurantId)
        viewModel.sortMenuItemsByPrice(isAscending) // Ordenar ascendente inicialmente

        // Configurar el botón de filtro
        val filterButton = findViewById<ImageView>(R.id.menuButton)
        filterButton.setImageResource(if (isAscending) R.drawable.ic_sort_ascending else R.drawable.ic_sort_descending) // Imagen inicial

        filterButton.setOnClickListener {
            toggleSortOrder(filterButton)
        }

        // Botón de regreso
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun toggleSortOrder(filterButton: ImageView) {
        isAscending = !isAscending // Cambia el estado de orden
        viewModel.sortMenuItemsByPrice(isAscending) // Actualiza los datos en el ViewModel

        // Cambia la imagen del botón según el estado actual
        val imageRes = if (isAscending) R.drawable.ic_sort_ascending else R.drawable.ic_sort_descending
        filterButton.setImageResource(imageRes)
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
}
