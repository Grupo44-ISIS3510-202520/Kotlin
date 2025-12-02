package com.example.brigadeapp.domain.entity

enum class Timeframe {
    ALL_TIME,
    LAST_7_DAYS
}

data class LeaderboardEntry(
    val userId: String,
    val displayName: String,
    val emailPrefix: String,
    val totalCompleted: Long,
    val weeklyCompleted: Long
)

data class LeaderboardSnapshot(
    val entries: List<LeaderboardEntry> = emptyList(),
    val lastUpdatedMillis: Long? = null
)
