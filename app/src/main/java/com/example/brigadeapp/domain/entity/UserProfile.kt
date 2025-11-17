package com.example.brigadeapp.domain.entity

data class UserProfile(
    val uid: String,
    val email: String,
    val name: String,
    val lastName: String,
    val uniandesCode: String,
    val bloodGroup: String,
    val role: String
)
