package com.example.brigadeapp.data.services.remote

import android.content.Context
import com.example.brigadeapp.data.services.ReportService
import com.example.brigadeapp.domain.entity.Report
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ReportRemoteService(
	private val firestore: FirebaseFirestore,
	private val storage: FirebaseStorage,
	private val context: Context
): ReportService {
	override suspend fun saveReport(report: Report): Result<Unit> {
		return try {
			// Get the last ID from reports-counter collection
			val counterDoc = firestore.collection("reports-counter")
				.document("lastId")
				.get()
				.await()
			
			val lastId = counterDoc.getLong("value") ?: 0L
			val newReportId = lastId + 1
			
			val reportData = mutableMapOf<String, Any?>(
				"reportId" to "K${newReportId.toString().padStart(2, '0')}",
				"type" to report.type,
				"place" to report.place,
				"description" to report.description,
				"imageUrl" to report.imageUrl,
				"audioUrl" to report.audioUrl,
				"isFollowUp" to report.followUp,
				"timestamp" to report.timestamp,
				"elapsedTime" to report.elapsedTime,
				"latitude" to report.latitude,
				"longitude" to report.longitude,
				"userId" to report.userId
			)

			// Save report to reports-kotlin collection
			firestore.collection("reports-kotlin")
				.document("K${newReportId.toString().padStart(2, '0')}")
				.set(reportData)
				.await()

			// Update the counter after successful save
			firestore.collection("reports-counter")
				.document("lastId")
				.update("value", newReportId)
				.await()

			Result.success(Unit)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}
}