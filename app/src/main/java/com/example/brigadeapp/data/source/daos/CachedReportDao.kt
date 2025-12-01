package com.example.brigadeapp.data.source.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.brigadeapp.domain.entity.CachedReport

@Dao
interface CachedReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reports: List<CachedReport>)

    @Query("SELECT * FROM cached_reports ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatest(limit: Int): List<CachedReport>

    @Query("DELETE FROM cached_reports")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM cached_reports")
    suspend fun count(): Int
}
