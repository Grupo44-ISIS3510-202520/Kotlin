package com.example.brigadeapp.domain.repository

import com.example.brigadeapp.data.repository.CprProgress
import kotlinx.coroutines.flow.Flow

interface TrainingRepository {
    fun observeCprProgress(): Flow<CprProgress>
    suspend fun markLessonVisited(pageIndex: Int, totalLessons: Int)
    suspend fun submitQuiz(correct: Int, total: Int)
}
