package com.example.brigadeapp.data.repository

import android.content.Context
import android.util.Log
import com.example.brigadeapp.data.services.local.ReportLocalService
import com.example.brigadeapp.data.services.remote.ReportRemoteService
import com.example.brigadeapp.helpers.work.ReportSyncWorker
import com.example.brigadeapp.domain.entity.Report
import com.example.brigadeapp.domain.repository.ReportRepository
import com.example.brigadeapp.data.source.local.sensors.ConnectivityManagerObserver
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.first


class ReportRepositoryImpl(
    private val context: Context,
    private val remoteService: ReportRemoteService,
    private val localService: ReportLocalService
) : ReportRepository {

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
                Log.e("ReportRepository", "remote save failed", e)
                throw Exception("Error submitting report: ${e.message}")
            }
        }

        localService.saveReport(report.copy(synced = false))
        ReportSyncWorker.enqueue(context)
    }

}