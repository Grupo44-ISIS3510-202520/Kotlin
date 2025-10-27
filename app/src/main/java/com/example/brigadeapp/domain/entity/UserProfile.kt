package com.example.brigadeapp.domain.entity

data class UserProfile(
    val uid: String,
    val email: String,         // ← se guarda también en Firestore
    val name: String,
    val lastName: String,
    val uniandesCode: String,
    val bloodGroup: String,    // O+, A+, ...
    val role: String           // student, professor, administrative
)
