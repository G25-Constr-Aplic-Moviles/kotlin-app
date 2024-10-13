package com.example.gastroandes.model

import java.time.LocalDateTime

data class User(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)

data class TokenUser(
    val id: String,
    val firstname: String,
    val lastName: String,
    val email: String
)