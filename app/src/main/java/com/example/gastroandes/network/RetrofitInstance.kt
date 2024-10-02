package com.example.gastroandes.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://restaurantservice-375afbe356dc.herokuapp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val usersRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://moviles-users-2476d1fe1bb5.herokuapp.com") // Emulador de Android apunta a localhost del host
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Nueva instancia para el servicio de analíticas
    private val analyticsRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://analyticservice-553a4e950222.herokuapp.com") // Apunta al servicio Flask local
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    val usersApi: ApiService by lazy {
        usersRetrofit.create(ApiService::class.java)
    }

    // API para el servicio de analíticas
    val analyticsApi: ApiService by lazy {
        analyticsRetrofit.create(ApiService::class.java)
    }
}
