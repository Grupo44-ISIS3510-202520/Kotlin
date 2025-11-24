package com.example.brigadeapp.domain.repository


interface BqAnalyticsRepository {

    suspend fun logAvailabilityChange(
        available: Boolean,
        uid: String?,
        email: String?,
        role: String? = null,
        uniandesCode: String? = null
    )


    suspend fun logTrainingStarted(
        trainingId: String,
        title: String,
        source: String,
        uid: String?,
        email: String?
    )


    suspend fun logTrainingQuizSubmitted(
        trainingId: String,
        score: Int,
        totalQuestions: Int,
        passed: Boolean,
        uid: String?,
        email: String?
    )
}
