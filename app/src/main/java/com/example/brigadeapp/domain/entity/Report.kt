package com.example.brigadeapp.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    val timestamp: String = Instant.now().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    val elapsedTime: Long,
    val synced: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val userId: String = ""
)