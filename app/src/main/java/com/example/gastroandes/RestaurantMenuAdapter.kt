import android.util.Log
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.NumberFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gastroandes.DishDetailActivity
import com.example.gastroandes.R
import com.example.gastroandes.model.MenuItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class RestaurantMenuAdapter : ListAdapter<MenuItem, RestaurantMenuAdapter.DishViewHolder>(DishDiffCallback()) {

    companion object {
        private const val TAG = "RestaurantMenuAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.dish_card, parent, false)
        return DishViewHolder(view)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        Log.d(TAG, "Binding view holder for position: $position")
        holder.bind(getItem(position))
    }

    class DishViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dishName: TextView = itemView.findViewById(R.id.dishName)
        private val dishPrice: TextView = itemView.findViewById(R.id.dishPrice)
        private val dishImage: ImageView = itemView.findViewById(R.id.dishImage)

        fun bind(dish: MenuItem) {
            Log.d(TAG, "Binding data for dish: ${dish.name}")

            dishName.text = dish.name
            dishPrice.text = formatPrice(dish.price)

            // Cargar la imagen desde la URL usando corutinas
            loadImageFromUrl(dish.image_url) { bitmap ->
                if (bitmap != null) {
                    dishImage.setImageBitmap(bitmap)
                } else {
                    Log.e(TAG, "Failed to load image for dish: ${dish.name}")
                }
            }

            // Hacer que el item sea clickable para llevar a DishDetailActivity
            itemView.setOnClickListener {
                Log.d(TAG, "Clicked on dish: ${dish.name}")
                val intent = Intent(itemView.context, DishDetailActivity::class.java).apply {
                    putExtra("DISH_NAME", dish.name)
                    putExtra("DISH_DESCRIPTION", dish.description)
                    putExtra("DISH_PRICE", dish.price)
                    putExtra("DISH_IMAGE_URL", dish.image_url)
                }
                itemView.context.startActivity(intent)
            }
        }

        private fun formatPrice(price: Float): String {
            val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
            formatter.maximumFractionDigits = 0
            return formatter.format(price)
        }

        private fun loadImageFromUrl(url: String, callback: (Bitmap?) -> Unit) {
            (itemView.context as? LifecycleOwner)?.lifecycleScope?.launch(Dispatchers.IO) {
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
                    Log.e(TAG, "Error loading image from URL: $url", e)
                    withContext(Dispatchers.Main) {
                        callback(null)
                    }
                }
            }
        }
    }

    class DishDiffCallback : DiffUtil.ItemCallback<MenuItem>() {
        override fun areItemsTheSame(oldItem: MenuItem, newItem: MenuItem): Boolean {
            return oldItem.item_id == newItem.item_id
        }

        override fun areContentsTheSame(oldItem: MenuItem, newItem: MenuItem): Boolean {
            return oldItem == newItem
        }
    }

}
