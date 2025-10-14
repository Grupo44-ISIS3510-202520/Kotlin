package com.example.brigadeapp.model.domain

import com.google.firebase.Timestamp

data class Protocol(
    val name: String = "",
    val url: String = "",
    val version: Int = 0,
    val lastUpdate: Timestamp? = null
)
