package com.example.brigadeapp.domain.repository

import com.example.brigadeapp.data.repository.CprProgress
import com.example.brigadeapp.domain.entity.Lesson
import com.example.brigadeapp.domain.entity.QuizQuestion
import com.example.brigadeapp.domain.entity.TrainingModule
import kotlinx.coroutines.flow.Flow


interface TrainingRepository {

    fun observeCprProgress(): Flow<CprProgress>

    fun observeTrainingModules(): Flow<List<TrainingModule>>

    fun observeLessons(trainingId: String): Flow<List<Lesson>>

    fun observeQuizQuestions(trainingId: String): Flow<List<QuizQuestion>>


    suspend fun markLessonVisited(pageIndex: Int, totalLessons: Int)

    suspend fun submitQuiz(correct: Int, total: Int)

    suspend fun flushPendingUpdates(): Result<Unit>
}
