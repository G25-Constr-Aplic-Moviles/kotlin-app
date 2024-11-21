package com.example.gastroandes

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.gastroandes.model.Restaurante
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class RestaurantAdapter(private val context: Context, private val restaurantList: List<Restaurante>) : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.restaurant_item, parent, false)
        return RestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val restaurant = restaurantList[position]
        holder.restaurantName.text = restaurant.name
        holder.restaurantRating.text = restaurant.average_rating.toString()
        holder.restaurantReviews.text = "(${restaurant.total_reviews})"
        holder.restaurantPrices.text = when (restaurant.price) {
            1 -> "$"
            2 -> "$$"
            3 -> "$$$"
            4 -> "$$$$"
            else -> "$"
        }

        // Cargar la imagen del restaurante
        val imageFile = File(restaurant.local_image_path ?: "")
        if (imageFile.exists()) {
            // Cargar desde el archivo local si existe
            Glide.with(context)
                .load(imageFile)
                .into(holder.restaurantImage)
        } else {
            // Cargar desde la URL si no hay archivo local
            Glide.with(context)
                .load(restaurant.image_url)
                .into(holder.restaurantImage)
        }

        // Configurar el click listener para cada restaurante
        holder.itemView.setOnClickListener {
            if (isNetworkAvailable()) {
                val context = holder.itemView.context
                val intent = Intent(context, RestaurantDetailActivity::class.java)
                intent.putExtra("RESTAURANTE_ID", restaurant.restaurant_id)
                context.startActivity(intent)
            } else {
                // Mostrar mensaje de error específico cuando no hay conexión a Internet
                Toast.makeText(
                    holder.itemView.context,
                    "No hay conexión a Internet. Intenta más tarde.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return restaurantList.size
    }

    class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val restaurantImage: ImageView = itemView.findViewById(R.id.restaurantImage)
        val restaurantName: TextView = itemView.findViewById(R.id.restaurantName)
        val restaurantRating: TextView = itemView.findViewById(R.id.restaurantRating)
        val restaurantReviews: TextView = itemView.findViewById(R.id.restaurantReviews)
        val restaurantPrices: TextView = itemView.findViewById(R.id.restaurantPrices)
    }

    private suspend fun loadImageFromUrl(url: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input: InputStream = connection.inputStream
                BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}
