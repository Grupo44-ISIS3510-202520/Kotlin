package com.example.brigadeapp.data.source.local.leaderboard

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LeaderboardDao {

    @Query("""
        SELECT * FROM leaderboard_entries
        WHERE timeframe = :timeframe
        ORDER BY 
            CASE 
                WHEN timeframe = 'ALL_TIME' THEN totalCompleted
                WHEN timeframe = 'LAST_7_DAYS' THEN weeklyCompleted
                ELSE totalCompleted
            END DESC
    """)
    suspend fun getEntriesForTimeframe(timeframe: String): List<LeaderboardEntryEntity>

    @Query("""
        SELECT MAX(lastUpdatedMillis) FROM leaderboard_entries
        WHERE timeframe = :timeframe
    """)
    suspend fun getLastUpdatedMillis(timeframe: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<LeaderboardEntryEntity>)

    @Query("DELETE FROM leaderboard_entries WHERE timeframe = :timeframe")
    suspend fun clearForTimeframe(timeframe: String)
}

