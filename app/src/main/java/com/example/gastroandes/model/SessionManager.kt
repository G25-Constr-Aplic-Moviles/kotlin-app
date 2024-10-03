package com.example.gastroandes.model

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREF_NAME = "GastroAndesPrefs"
    private const val KEY_AUTH_TOKEN = "authToken"
    private lateinit var sharedPreferences: SharedPreferences

    // Inicializar el SessionManager con el contexto
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Guardar el token en SharedPreferences
    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    // Obtener el token desde SharedPreferences
    fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }

    // Limpiar el token de SharedPreferences
    fun clearAuthToken() {
        sharedPreferences.edit().remove(KEY_AUTH_TOKEN).apply()
    }
}
