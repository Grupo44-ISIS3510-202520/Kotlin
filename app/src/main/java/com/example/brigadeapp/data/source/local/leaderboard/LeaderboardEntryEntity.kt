package com.example.brigadeapp.data.source.local.leaderboard

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leaderboard_entries", primaryKeys = ["userId", "timeframe"])
data class LeaderboardEntryEntity(
    val userId: String,
    val displayName: String,
    val emailPrefix: String,
    val totalCompleted: Long,
    val weeklyCompleted: Long,
    val timeframe: String,
    val lastUpdatedMillis: Long
)
