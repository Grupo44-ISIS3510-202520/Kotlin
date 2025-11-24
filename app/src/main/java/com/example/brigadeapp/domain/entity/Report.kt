package com.example.brigadeapp.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "reports")
data class Report(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val place: String,
    val time: String?,
    val description: String,
    val followUp: Boolean,
    val imageUrl: String?,
    val audioUrl: String?,
    val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(System.currentTimeMillis())),
    val elapsedTime: Long,
    val synced: Boolean = false
)