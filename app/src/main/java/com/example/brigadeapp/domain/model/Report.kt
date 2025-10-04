package com.example.brigadeapp.domain.model

data class Report(
    val type: String,
    val place: String,
    val time: String?,
    val description: String,
    val followUp: Boolean,
    val imageUri: String?,
    val audioUri: String?,
    val timestamp: Long = System.currentTimeMillis()
)
