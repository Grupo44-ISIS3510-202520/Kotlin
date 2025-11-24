package com.example.brigadeapp.domain.utils

import android.os.Bundle
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics

object AnalyticsLogger {

    private const val TAG = "AnalyticsLogger"

    private val analytics: FirebaseAnalytics by lazy {
        Firebase.analytics
    }

    fun logProtocolAccess(
        protocolId: String,
        protocolTitle: String,
        isOffline: Boolean
    ) {
        val bundle = Bundle().apply {
            putString("content_type", "protocol")
            putString("protocol_id", protocolId)
            putString("protocol_title", protocolTitle)
            putBoolean("is_offline", isOffline)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("protocol_access", bundle)
        Log.d(TAG, "Protocol accessed: $protocolTitle (offline: $isOffline)")
    }


    fun logAlertAccess(
        alertId: String,
        alertType: String,
        isOffline: Boolean
    ) {
        val bundle = Bundle().apply {
            putString("content_type", "alert")
            putString("alert_id", alertId)
            putString("alert_type", alertType)
            putBoolean("is_offline", isOffline)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("alert_access", bundle)
        Log.d(TAG, "Alert accessed: $alertType (offline: $isOffline)")
    }


    fun logConnectivityChange(isOnline: Boolean) {
        val bundle = Bundle().apply {
            putBoolean("is_online", isOnline)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("connectivity_change", bundle)
        Log.d(TAG, "Connectivity changed: ${if (isOnline) "ONLINE" else "OFFLINE"}")
    }


    fun logProtocolListViewed(
        protocolCount: Int,
        isOffline: Boolean
    ) {
        val bundle = Bundle().apply {
            putInt("protocol_count", protocolCount)
            putBoolean("is_offline", isOffline)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("protocol_list_viewed", bundle)
        Log.d(TAG, "Protocol list viewed: $protocolCount items (offline: $isOffline)")
    }


    fun logAlertListViewed(
        alertCount: Int,
        isOffline: Boolean
    ) {
        val bundle = Bundle().apply {
            putInt("alert_count", alertCount)
            putBoolean("is_offline", isOffline)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("alert_list_viewed", bundle)
        Log.d(TAG, "Alert list viewed: $alertCount items (offline: $isOffline)")
    }

    fun logOfflineAccessFailure(
        contentType: String,
        contentId: String,
        reason: String
    ) {
        val bundle = Bundle().apply {
            putString("content_type", contentType)
            putString("content_id", contentId)
            putString("failure_reason", reason)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("offline_access_failure", bundle)
        Log.w(TAG, "Offline access failure: $contentType ($reason)")
    }

    // BQ10: Track brigadist availability changes for weekly distribution analysis
    fun logAvailabilityChange(
        available: Boolean,
        uid: String?,
        email: String?
    ) {
        val bundle = Bundle().apply {
            putBoolean("available", available)
            putString("uid", uid ?: "unknown")
            putString("email", email ?: "unknown")
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("availability_change", bundle)
        Log.d(TAG, "Availability changed: ${if (available) "AVAILABLE" else "UNAVAILABLE"} (uid: $uid)")
    }

    // BQ Extra: Track when users start a training course
    fun logTrainingStarted(
        trainingId: String,
        title: String,
        source: String
    ) {
        val bundle = Bundle().apply {
            putString("training_id", trainingId)
            putString("training_title", title)
            putString("source", source)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("training_started", bundle)
        Log.d(TAG, "Training started: $title from $source")
    }

    // BQ Extra: Track quiz submissions and validate training completion
    fun logTrainingQuizSubmitted(
        trainingId: String,
        score: Int,
        totalQuestions: Int,
        passed: Boolean
    ) {
        val bundle = Bundle().apply {
            putString("training_id", trainingId)
            putInt("quiz_score", score)
            putInt("quiz_total", totalQuestions)
            putBoolean("quiz_passed", passed)
            putFloat("quiz_percentage", if (totalQuestions > 0) (score.toFloat() / totalQuestions) * 100 else 0f)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("training_quiz_submitted", bundle)
        Log.d(TAG, "Quiz submitted: $score/$totalQuestions (${if (passed) "PASSED" else "FAILED"}) for $trainingId")
    }
}