package com.example.brigadeapp.data.repository

import android.content.Context
import android.util.Log
import com.example.brigadeapp.data.services.local.ReportLocalService
import com.example.brigadeapp.data.services.local.SyncPreferencesService
import com.example.brigadeapp.data.services.remote.ReportRemoteService
import com.example.brigadeapp.helpers.work.ReportSyncWorker
import com.example.brigadeapp.domain.entity.Report
import com.example.brigadeapp.domain.entity.CachedReport
import com.example.brigadeapp.domain.repository.ReportRepository
import com.example.brigadeapp.data.source.local.sensors.ConnectivityManagerObserver
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class ReportRepositoryImpl(
    private val context: Context,
    private val remoteService: ReportRemoteService,
    private val localService: ReportLocalService,
    private val syncPreferences: SyncPreferencesService
) : ReportRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            remoteService.observeReports(10).collect { result ->
                if (result.isSuccess) {
                    val reports = result.getOrNull() ?: emptyList()
                    val isOnline = ConnectivityManagerObserver(context).observe().first()
                    try {
                        localService.cacheReports(reports)
                        if (isOnline) {
                            syncPreferences.saveLastSyncTime(System.currentTimeMillis())
                        }
                    } catch (e: Exception) {
                        Log.e("ReportRepository", "Failed to cache reports in background", e)
                    }
                }
            }
        }
    }

    override suspend fun submitReport(report: Report) {

        val online = ConnectivityManagerObserver(context).observe().first()
        if (online) {
            try {
                val remoteResult = remoteService.saveReport(report)
                if (remoteResult.isSuccess) {
                    localService.saveReport(report.copy(synced = true))
                    return
                }
            } catch (e: Exception) {
                throw Exception("Error submitting report: ${e.message}")
            }
        }

        val offlineReport = report.copy(synced = false)
        localService.saveReport(offlineReport)

        val timeFormat = java.text.SimpleDateFormat("HHmmss", java.util.Locale.US)
        val timeString = timeFormat.format(java.util.Date())
        val tempReportId = "P_$timeString"
        localService.saveToCachedReports(offlineReport, tempReportId)
        
        ReportSyncWorker.enqueue(context)
    }

    override suspend fun getLatestReports(limit: Int): Result<List<CachedReport>> {
        val online = ConnectivityManagerObserver(context).observe().first()
        
        if (online) {
            try {
                val remoteResult = remoteService.getLatestReports(limit)
                if (remoteResult.isSuccess) {
                    val reports = remoteResult.getOrNull() ?: emptyList()
                    localService.cacheReports(reports)
                    return Result.success(reports)
                }
            } catch (e: Exception) {
                Log.e("ReportRepository", "Failed to fetch remote reports", e)
            }
        }

        return localService.getCachedReports(limit)
    }

    override fun observeReports(limit: Int): Flow<Result<List<CachedReport>>> {
        return localService.observeCachedReports(limit).map { localReports ->
            Result.success(localReports)
        }
    }

}