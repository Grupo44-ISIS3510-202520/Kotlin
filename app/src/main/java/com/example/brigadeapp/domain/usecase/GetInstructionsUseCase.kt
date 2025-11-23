package com.example.brigadeapp.domain.usecase

import com.example.brigadeapp.domain.repository.OpenAIRepository
import javax.inject.Inject

class GetInstructionsUseCase @Inject constructor(
    private val repo: OpenAIRepository
) {
    suspend operator fun invoke(prompt: String): List<String> {
        val response = repo.request(prompt)
        if (response.isBlank()) return emptyList()
        return response.split("\n").map { it.trim() }.filter { it.isNotBlank() }
    }
}
