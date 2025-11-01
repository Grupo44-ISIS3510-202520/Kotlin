package com.example.brigadeapp.domain.entity

data class AuthUser(
    val uid: String,
    val email: String?,
    val emailVerified: Boolean
)
