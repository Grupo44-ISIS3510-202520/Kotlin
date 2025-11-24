package com.example.brigadeapp.data.source.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.brigadeapp.domain.entity.Report

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(report: Report): Long

    @Query("SELECT * FROM reports WHERE synced = 0")
    suspend fun getUnsynced(): List<Report>

    @Query("UPDATE reports SET synced = :synced WHERE id = :id")
    suspend fun markSynced(id: Long, synced: Boolean)
}