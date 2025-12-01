package com.example.brigadeapp.data.services.remote

import android.content.Context
import com.example.brigadeapp.data.services.ReportService
import com.example.brigadeapp.domain.entity.Report
import com.example.brigadeapp.domain.entity.CachedReport
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
			firestore.collection("reports")
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

	suspend fun getLatestReports(limit: Int = 10): Result<List<CachedReport>> {
		return try {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                ?: return Result.failure(Exception("User not authenticated"))

            val snapshot = firestore.collection("reports")
				.whereEqualTo("userId", currentUserId)
				.orderBy("timestamp", Query.Direction.DESCENDING)
				.limit(limit.toLong())
				.get()
				.await()

			val reports = snapshot.documents.mapNotNull { doc ->
				try {
					CachedReport(
						reportId = doc.getString("reportId") ?: doc.id,
						type = doc.getString("type") ?: "",
						place = doc.getString("place") ?: "",
						description = doc.getString("description") ?: "",
						imageUrl = doc.getString("imageUrl"),
						audioUrl = doc.getString("audioUrl"),
						isFollowUp = doc.getBoolean("isFollowUp") ?: false,
						timestamp = doc.getString("timestamp") ?: "",
						elapsedTime = doc.getLong("elapsedTime") ?: 0L,
						latitude = doc.getDouble("latitude"),
						longitude = doc.getDouble("longitude"),
						userId = doc.getString("userId") ?: ""
					)
				} catch (e: Exception) {
					throw Exception("Error converting document to CachedReport", e)
				}
			}

			Result.success(reports)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	fun observeReports(limit: Int = 10): Flow<Result<List<CachedReport>>> = callbackFlow {
		val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
		if (currentUserId == null) {
			trySend(Result.failure(Exception("User not authenticated")))
			close()
			return@callbackFlow
		}

		val listenerRegistration = firestore.collection("reports")
			.whereEqualTo("userId", currentUserId)
			.orderBy("timestamp", Query.Direction.DESCENDING)
			.limit(limit.toLong())
			.addSnapshotListener { snapshot, error ->
				if (error != null) {
					trySend(Result.failure(error))
					return@addSnapshotListener
				}

				if (snapshot != null) {
					val reports = snapshot.documents.mapNotNull { doc ->
						try {
							CachedReport(
								reportId = doc.getString("reportId") ?: doc.id,
								type = doc.getString("type") ?: "",
								place = doc.getString("place") ?: "",
								description = doc.getString("description") ?: "",
								imageUrl = doc.getString("imageUrl"),
								audioUrl = doc.getString("audioUrl"),
								isFollowUp = doc.getBoolean("isFollowUp") ?: false,
								timestamp = doc.getString("timestamp") ?: "",
								elapsedTime = doc.getLong("elapsedTime") ?: 0L,
								latitude = doc.getDouble("latitude"),
								longitude = doc.getDouble("longitude"),
								userId = doc.getString("userId") ?: ""
							)
						} catch (e: Exception) {
							throw Exception("Error converting document to CachedReport", e)
						}
					}
					trySend(Result.success(reports))
				}
			}

		awaitClose { listenerRegistration.remove() }
	}
}