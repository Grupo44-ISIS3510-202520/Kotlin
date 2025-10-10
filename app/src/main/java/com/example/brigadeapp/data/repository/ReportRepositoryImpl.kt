package com.example.brigadeapp.data.repository

import android.util.Log
import com.example.brigadeapp.domain.model.Report
import com.example.brigadeapp.domain.repository.ReportRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import androidx.core.net.toUri

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

        try {
            """// Subir imagen si existe
            try {
                report.imageUri?.let { uriString ->
                    uploadFile(uriString, "images", "jpg")?.let { imageUrl ->
                        reportData["imageUrl"] = imageUrl
                    }
                }
            } catch (e: Exception) {
                Log.e("ReportRepository", "Error Foto", e)
                throw e
            }

            // Subir audio si existe
            try {
                report.audioUri?.let { uriString ->
                    uploadFile(uriString, "audios", "mp3")?.let { audioUrl ->
                        reportData["audioUrl"] = audioUrl
                    }
                }
            } catch (e: Exception) {
                Log.e("ReportRepository", "Error Audio", e)
                throw e
            }"""

            // Guardar en Firestore
            firestore.collection("reports-kotlin")
                .add(reportData)
                .await()

        } catch (e: Exception) {
            Log.e("ReportRepository", "Error subiendo reporte", e)
            throw e
        }
    }


    private suspend fun uploadFile(uriString: String, folder: String, extension: String): String? {
        return try {
            val uri = uriString.toUri()
            val ref = storage.reference.child("reports/$folder/${System.currentTimeMillis()}.$extension")

            Log.d("UploadFile", "Subiendo archivo a: ${ref.path}")
            ref.putFile(uri).await()

            val downloadUrl = ref.downloadUrl.await().toString()
            Log.d("UploadFile", "Archivo subido correctamente: $downloadUrl")

            downloadUrl
        } catch (e: Exception) {
            Log.e("UploadFile", "Error al subir archivo: $uriString", e)
            null
        }
    }
}