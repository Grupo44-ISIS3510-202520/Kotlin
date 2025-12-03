package com.example.brigadeapp.data.source.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.brigadeapp.domain.entity.CachedReport
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reports: List<CachedReport>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: CachedReport)

    @Query("SELECT * FROM cached_reports ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatest(limit: Int): List<CachedReport>

    @Query("SELECT * FROM cached_reports ORDER BY timestamp DESC LIMIT :limit")
    fun observeLatest(limit: Int): Flow<List<CachedReport>>

    @Query("DELETE FROM cached_reports")
    suspend fun deleteAll()

    @Query("DELETE FROM cached_reports WHERE synced = 1")
    suspend fun deleteSynced()

    @Query("DELETE FROM cached_reports WHERE reportId = :reportId")
    suspend fun deleteById(reportId: String)

    @Query("SELECT COUNT(*) FROM cached_reports")
    suspend fun count(): Int
}
