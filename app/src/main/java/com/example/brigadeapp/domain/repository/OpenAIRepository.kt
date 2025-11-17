package com.example.brigadeapp.domain.repository

interface OpenAIRepository {
    suspend fun getInstructions(prompt: String): List<String>
}