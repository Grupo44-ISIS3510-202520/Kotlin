package com.example.brigadeapp.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_reports")
data class CachedReport(
    @PrimaryKey val reportId: String,
    val type: String,
    val place: String,
    val description: String,
    val imageUrl: String?,
    val audioUrl: String?,
    val isFollowUp: Boolean,
    val timestamp: String,
    val elapsedTime: Long,
    val latitude: Double?,
    val longitude: Double?,
    val userId: String,
    val cachedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = true
)
