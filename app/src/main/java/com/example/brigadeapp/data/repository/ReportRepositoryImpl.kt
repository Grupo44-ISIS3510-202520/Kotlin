package com.example.brigadeapp.data.repository

import android.net.Uri
import com.example.brigadeapp.domain.model.Report
import com.example.brigadeapp.domain.repository.ReportRepository
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
            "followUp" to report.followUp,
            "timestamp" to report.timestamp
        )

        // Subir imagen si existe
        if (!report.imageUri.isNullOrEmpty()) {
            val imageRef = storage.reference
                .child("reports/images/${System.currentTimeMillis()}.jpg")
            val upload = imageRef.putFile(Uri.parse(report.imageUri)).await()
            val url = imageRef.downloadUrl.await().toString()
            reportData["imageUrl"] = url
        }

        // Subir audio si existe
        if (!report.audioUri.isNullOrEmpty()) {
            val audioRef = storage.reference
                .child("reports/audios/${System.currentTimeMillis()}.mp3")
            val upload = audioRef.putFile(Uri.parse(report.audioUri)).await()
            val url = audioRef.downloadUrl.await().toString()
            reportData["audioUrl"] = url
        }

        // Guardar en Firestore
        firestore.collection("reports")
            .add(reportData)
            .await()
    }
}