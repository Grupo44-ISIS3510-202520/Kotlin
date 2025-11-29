package com.example.brigadeapp.data.source.local.leaderboard

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leaderboard_entries")
data class LeaderboardEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: String,
    val displayName: String,
    val totalCompleted: Long,
    val weeklyCompleted: Long,
    val timeframe: String,
    val lastUpdatedMillis: Long
)
