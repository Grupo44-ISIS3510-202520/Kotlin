package com.example.brigadeapp.data.repository

import android.util.Log
import com.example.brigadeapp.domain.repository.BqAnalyticsRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class BqAnalyticsRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : BqAnalyticsRepository {

    companion object {
        private const val TAG = "BqAnalyticsRepo"

        // Firestore collection names
        private const val AVAILABILITY_COLLECTION = "availability_data"
        private const val TRAINING_COLLECTION = "feature_analysis_quiz"
    }


    override suspend fun logAvailabilityChange(
        available: Boolean,
        uid: String?,
        email: String?,
        role: String?,
        uniandesCode: String?
    ) {
        try {
            val data = hashMapOf(
                "available" to available,
                "uid" to (uid ?: "unknown"),
                "email" to (email ?: "unknown"),
                "timestamp" to System.currentTimeMillis(),
                "role" to role,
                "uniandesCode" to uniandesCode
            )

            // Auto-generate document ID; Firestore offline queue handles sync
            db.collection(AVAILABILITY_COLLECTION)
                .add(data)
                .await()

            Log.d(TAG, "BQ1: Logged availability change: available=$available, uid=$uid")
        } catch (e: Exception) {
            // Log error but don't throw - offline writes are queued automatically
            Log.w(TAG, "BQ1: Error logging availability (will retry when online): ${e.message}")
        }
    }


    override suspend fun logTrainingStarted(
        trainingId: String,
        title: String,
        source: String,
        uid: String?,
        email: String?
    ) {
        try {
            val data = hashMapOf(
                "eventType" to "training_started",
                "trainingId" to trainingId,
                "title" to title,
                "source" to source,
                "uid" to (uid ?: "unknown"),
                "email" to (email ?: "unknown"),
                "timestamp" to System.currentTimeMillis()
            )

            db.collection(TRAINING_COLLECTION)
                .add(data)
                .await()

            Log.d(TAG, "BQ2: Logged training started: $trainingId from $source")
        } catch (e: Exception) {
            Log.w(TAG, "BQ2: Error logging training start (will retry when online): ${e.message}")
        }
    }

    override suspend fun logTrainingQuizSubmitted(
        trainingId: String,
        score: Int,
        totalQuestions: Int,
        passed: Boolean,
        uid: String?,
        email: String?
    ) {
        try {
            val scorePercentage = if (totalQuestions > 0) {
                (score.toDouble() / totalQuestions * 100).toInt()
            } else 0

            val data = hashMapOf(
                "eventType" to "training_quiz_submitted",
                "trainingId" to trainingId,
                "score" to score,
                "totalQuestions" to totalQuestions,
                "passed" to passed,
                "scorePercentage" to scorePercentage,
                "uid" to (uid ?: "unknown"),
                "email" to (email ?: "unknown"),
                "timestamp" to System.currentTimeMillis()
            )

            db.collection(TRAINING_COLLECTION)
                .add(data)
                .await()

            Log.d(TAG, "BQ2: Logged quiz submission: $trainingId, score=$score/$totalQuestions, passed=$passed")
        } catch (e: Exception) {
            Log.w(TAG, "BQ2: Error logging quiz submission (will retry when online): ${e.message}")
        }
    }
}
