package com.example.brigadeapp.domain.usecase

import com.example.brigadeapp.domain.repository.OpenAIRepository
import javax.inject.Inject

class GetCachedInstructionsUseCase @Inject constructor(
    private val repo: OpenAIRepository
) {
    suspend operator fun invoke(prompt: String): List<String>? {
        val cached = repo.cachedResponse(prompt) ?: return null
        if (cached.isBlank()) return null
        return cached.split("\n").map { it.trim() }.filter { it.isNotBlank() }
    }
}
