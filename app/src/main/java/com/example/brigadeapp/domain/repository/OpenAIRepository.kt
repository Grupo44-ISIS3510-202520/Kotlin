package com.example.brigadeapp.domain.repository

interface OpenAIRepository {
    suspend fun request(prompt: String): String

    suspend fun cachedResponse(prompt: String): String?
}