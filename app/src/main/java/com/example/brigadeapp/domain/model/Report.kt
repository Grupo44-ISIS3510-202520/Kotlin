package com.example.brigadeapp.domain.model

import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Report(
    val type: String,
    val place: String,
    val time: String?,
    val description: String,
    val followUp: Boolean,
    val imageUrl: String?,
    val audioUrl: String?,
    val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(System.currentTimeMillis()))
)