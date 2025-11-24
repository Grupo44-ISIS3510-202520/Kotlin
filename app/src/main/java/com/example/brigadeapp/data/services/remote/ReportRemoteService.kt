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

			firestore.collection("reports-kotlin")
				.add(reportData)
				.await()

			Result.success(Unit)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}
}