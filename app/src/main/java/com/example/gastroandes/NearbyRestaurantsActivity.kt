package com.example.gastroandes

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.example.gastroandes.viewModel.NearbyRestaurantsViewModel
import com.example.gastroandes.network.RetrofitInstance
import com.example.gastroandes.network.TimeData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.Intent
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NearbyRestaurantsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val viewModel: NearbyRestaurantsViewModel by viewModels {
        NearbyRestaurantsViewModelFactory(RetrofitInstance.api)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nearby_restaurants_v8)

        // Obtener el tiempo de inicio desde el intent
        val startTime = intent.getLongExtra("startTime", 0)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createFragment()
        setupObservers()

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
                else -> false
            }
        }
    }

    private fun createFragment() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        getDeviceLocation()
    }

    private fun getDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f),
                    3000,
                    null
                )

                // Pasar la ubicación del usuario al ViewModel
                viewModel.loadNearbyRestaurants(currentLatLng)

                // Calcular el tiempo de carga cuando la ubicación es obtenida
                val endTime = System.currentTimeMillis()
                val loadingTime = (endTime - intent.getLongExtra("startTime", 0)) / 1000.0  // Tiempo en segundos

                // Registrar el tiempo de carga
                Log.d("LoadingTime", "Tiempo de carga: $loadingTime segundos")

                // Enviar el tiempo de carga al servidor
                sendLoadingTimeToAnalytics(loadingTime)
            }
        }
    }

    private fun sendLoadingTimeToAnalytics(loadingTime: Double) {
        // Crear el objeto TimeData con el tiempo, la plataforma y el timestamp
        val timeData = TimeData(tiempo = loadingTime, plataforma = "Kotlin")

        // Hacer la llamada al servicio API para enviar los datos
        RetrofitInstance.analyticsApi.sendTime(timeData).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("Analytics", "Tiempo de carga enviado correctamente")
                } else {
                    Log.e("Analytics", "Error en el servidor: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("Analytics", "Fallo al enviar el tiempo de carga", t)
            }
        })
    }

    private fun setupObservers() {
        viewModel.markers.observe(this, Observer { markerOptionsList ->
            markerOptionsList.forEach { markerOptions ->
                map.addMarker(markerOptions)
            }
        })
    }
}
