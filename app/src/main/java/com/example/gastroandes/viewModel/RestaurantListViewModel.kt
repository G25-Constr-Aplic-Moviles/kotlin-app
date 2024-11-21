package com.example.gastroandes.viewModel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.gastroandes.cache.RestaurantCache
import com.example.gastroandes.model.Restaurante
import com.example.gastroandes.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class RestaurantListViewModel : ViewModel() {

    private val _restaurants = MutableLiveData<List<Restaurante>?>()
    val restaurants: LiveData<List<Restaurante>?> get() = _restaurants

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun fetchRestaurantList(isNetworkAvailable: Boolean, context: Context) {
        val cachedRestaurants = RestaurantCache.getRestaurants()

        if (cachedRestaurants != null) {
            Log.d("RestaurantListViewModel", "Cargando restaurantes desde caché.")
            _restaurants.value = cachedRestaurants
        } else if (isNetworkAvailable) {
            Log.d("RestaurantListViewModel", "Conexión disponible. Cargando restaurantes desde la API.")
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val restaurantList = RetrofitInstance.api.getRestaurantes()

                    // Descargar y guardar imágenes localmente
                    restaurantList.forEach { restaurant ->
                        val fileName = "restaurant_${restaurant.restaurant_id}.jpg"
                        val imageFile = saveImageToCache(context, restaurant.image_url, fileName)
                        restaurant.local_image_path = imageFile?.absolutePath
                        restaurant.local_image_path?.let { Log.d("RestaurantImagePath", it) }
                    }


                    _restaurants.postValue(restaurantList)
                    RestaurantCache.putRestaurants(restaurantList) // Guardar en caché
                    Log.d("RestaurantListViewModel", "Restaurantes guardados en caché con imágenes.")
                } catch (e: Exception) {
                    _errorMessage.postValue("Error al obtener restaurantes: ${e.message}")
                }
            }
        } else {
            Log.d("RestaurantListViewModel", "No hay conexión a Internet y no hay datos en caché.")
            _errorMessage.postValue("No hay conexión a Internet y no hay datos en caché.")
        }
    }

    // Método para guardar imágenes en caché
    private fun saveImageToCache(context: Context, imageUrl: String, fileName: String): File? {
        return try {
            // Ruta al archivo en la caché
            val file = File(context.cacheDir, fileName)
            if (!file.exists()) {
                // Descargar imagen como Bitmap usando Glide
                val bitmap = Glide.with(context)
                    .asBitmap() // Indica que necesitas el resultado como un Bitmap
                    .load(imageUrl) // Carga desde la URL de la imagen
                    .submit() // Inicia la descarga
                    .get() // Obtiene el Bitmap de forma síncrona

                // Guardar el Bitmap como archivo JPG
                file.outputStream().use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream) // Calidad 85%
                    bitmap.compressToFile(file)
                }
            }
            file // Retorna el archivo creado o existente
        } catch (e: Exception) {
            e.printStackTrace()
            null // Retorna null si ocurre algún error
        }
    }


    // Extensión para convertir un Bitmap a un archivo
    private fun Bitmap.compressToFile(file: File) {
        file.outputStream().use { outputStream ->
            this.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        }
    }
}
