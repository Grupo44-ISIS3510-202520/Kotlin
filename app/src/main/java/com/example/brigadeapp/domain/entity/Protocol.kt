package com.example.brigadeapp.domain.entity

import com.google.firebase.Timestamp

data class Protocol(
    val name: String = "",
    val url: String = "",
    val version: Int = 0,
    val lastUpdate: Timestamp? = null
)
