package com.example.brigadeapp.domain.repository

import com.example.brigadeapp.domain.entity.Report
import com.example.brigadeapp.domain.entity.CachedReport
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    suspend fun submitReport(report: Report)
    suspend fun getLatestReports(limit: Int = 10): Result<List<CachedReport>>
    fun observeReports(limit: Int = 10): Flow<Result<List<CachedReport>>>
}
