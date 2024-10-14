package com.example.gastroandes.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class UserHistoryEntry(
    val id: String,
    val restaurant_id: String,
    val timestamp: String,
    val user_id: String,
    var restaurantName: String? = null
) {
    // Método para formatear la fecha
    @RequiresApi(Build.VERSION_CODES.O)
    fun getFormattedDate(): String {
        val formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
        val dateTime = LocalDateTime.parse(timestamp, formatter)
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM"))
    }
}
