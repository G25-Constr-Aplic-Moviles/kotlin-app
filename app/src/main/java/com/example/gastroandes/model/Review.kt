package com.example.gastroandes.model

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

data class Review(
    val content: String,
    val rating: Float,
    val restaurant_id: Int,
    val timestamp: String,
    val user_id: String,
){
    // Método para formatear la fecha
    @RequiresApi(Build.VERSION_CODES.O)
    fun getFormattedDateReview(): String {
        return try {
            val primaryFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH)
            val dateTime = LocalDateTime.parse(timestamp, primaryFormatter)
            dateTime.format(DateTimeFormatter.ofPattern("dd/MM"))
        } catch (e: DateTimeParseException) {
            Log.e("UserHistoryEntry", "Error al parsear la fecha con el formato primario: $timestamp", e)

            try {
                val alternateFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH)
                val dateTime = LocalDateTime.parse(timestamp, alternateFormatter)
                dateTime.format(DateTimeFormatter.ofPattern("dd/MM"))
            } catch (e2: DateTimeParseException) {
                Log.e("UserHistoryEntry", "Error al parsear la fecha con el formato alternativo: $timestamp", e2)
                ""
            }
        }
    }
}