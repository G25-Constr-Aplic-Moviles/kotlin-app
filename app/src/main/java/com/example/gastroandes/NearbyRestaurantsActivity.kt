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
import com.example.gastroandes.model.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope

class NearbyRestaurantsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mapIsReady = false
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val viewModel: NearbyRestaurantsViewModel by viewModels {
        NearbyRestaurantsViewModelFactory(RetrofitInstance.api)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nearby_restaurants_v8)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createFragment()
        setupObservers()
        setupNavigation()
    }

    private fun setupNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = 0
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, RestaurantListActivity::class.java))
                    true
                }
                R.id.history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
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

    private fun logoutUser() {
        SessionManager.clearAuthToken()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun createFragment() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        mapIsReady = true
        initializeMapIfPermissionsGranted()
    }

    private fun initializeMapIfPermissionsGranted() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
            getDeviceLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }

    private fun getDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f),
                        3000,
                        null
                    )
                    viewModel.loadNearbyRestaurants(currentLatLng)
                    sendLoadingTimeToAnalytics()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (mapIsReady) {
                getDeviceLocation()
            }
        } else {
            Log.e("Permission", "Location permission was not granted")
        }
    }

    private fun sendLoadingTimeToAnalytics() {
        val startTime = intent.getLongExtra("startTime", 0)
        val endTime = System.currentTimeMillis()
        val loadingTime = (endTime - startTime) / 1000.0
        Log.d("LoadingTime", "Tiempo de carga: $loadingTime segundos")

        lifecycleScope.launch {
            try {
                val token = SessionManager.getAuthToken()
                val userInfo = RetrofitInstance.usersApi.getUserInfo("Bearer $token")
                val userId = userInfo.id
                val timeData = TimeData(tiempo = loadingTime, plataforma = "Kotlin", userID = userId)

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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupObservers() {
        viewModel.markers.observe(this, Observer { markerOptionsList ->
            markerOptionsList.forEach { markerOptions ->
                map.addMarker(markerOptions)
            }
        })
    }
}
