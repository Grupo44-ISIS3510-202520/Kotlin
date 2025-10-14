package com.example.brigadeapp.model.repository.implementation

import android.util.Log
import com.example.brigadeapp.model.domain.Report
import com.example.brigadeapp.model.repository.interfaces.ReportRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ReportRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ReportRepository {

    override suspend fun submitReport(report: Report) {
        val reportData = mutableMapOf(
            "type" to report.type,
            "place" to report.place,
            "time" to report.time,
            "description" to report.description,
            "imageUrl" to report.imageUrl,
            "audioUrl" to report.audioUrl,
            "followUp" to report.followUp,
            "timestamp" to report.timestamp,
            "elapsedTime" to report.elapsedTime
        )

        try {
            // Guardar en Firestore
            firestore.collection("reports-kotlin")
                .add(reportData)
                .await()

        } catch (e: Exception) {
            Log.e("ReportRepository", "Error subiendo reporte", e)
            throw e
        }
    }
}