package com.example.brigadeapp.domain.entity

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Alert(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Timestamp? = null,
    val type: String = "info" // 'info', 'warning', 'emergency'
)
