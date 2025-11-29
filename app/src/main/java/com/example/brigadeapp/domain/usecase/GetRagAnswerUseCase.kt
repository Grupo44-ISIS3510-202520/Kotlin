package com.example.brigadeapp.domain.usecase

import com.example.brigadeapp.data.repository.RagRepository
import com.example.brigadeapp.domain.model.RagState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject


class GetRagAnswerUseCase @Inject constructor(
    private val repository: RagRepository
) {
    operator fun invoke(query: String): Flow<RagState> = flow {
        if (query.isBlank()) {
            emit(RagState.Error("Question cannot be empty"))
            return@flow
        }

        emit(RagState.Loading)

        val result = repository.getAnswer(query)

        if (result.isSuccess) {
            val (response, fromCache) = result.getOrThrow()
            emit(
                RagState.Success(
                    answer = response.answer,
                    sources = response.sources,
                    fromCache = fromCache
                )
            )
        } else {
            val error = result.exceptionOrNull()
            emit(RagState.Error(error?.message ?: "Error getting answer"))
        }
    }
}