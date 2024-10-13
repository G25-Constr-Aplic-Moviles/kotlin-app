package com.example.gastroandes.network

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://restaurantservice-375afbe356dc.herokuapp.com/")
            .client(
                OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .connectionPool(ConnectionPool(0, 5, TimeUnit.MINUTES))
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    private val usersRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val analyticsRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://analyticservice-553a4e950222.herokuapp.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val historyRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://history-service-7d9c8283d538.herokuapp.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    val usersApi: ApiService by lazy {
        usersRetrofit.create(ApiService::class.java)
    }

    val analyticsApi: ApiService by lazy {
        analyticsRetrofit.create(ApiService::class.java)
    }

    val historyApi: ApiService by lazy {
        historyRetrofit.create(ApiService::class.java)
    }
}
