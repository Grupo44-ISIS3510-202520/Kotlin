package com.example.brigadeapp.domain.usecase

import com.example.brigadeapp.domain.entity.QuizQuestion
import com.example.brigadeapp.domain.repository.TrainingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTrainingQuizQuestions @Inject constructor(
    private val repository: TrainingRepository
) {
    operator fun invoke(trainingId: String): Flow<List<QuizQuestion>> {
        return repository.observeQuizQuestions(trainingId)
    }
}
